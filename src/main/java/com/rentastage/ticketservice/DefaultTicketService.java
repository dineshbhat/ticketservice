package com.rentastage.ticketservice;

import com.google.common.collect.ImmutableList;
import com.rentastage.ticketservice.model.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.shell.table.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static org.springframework.shell.table.CellMatchers.at;
import static org.springframework.util.Assert.notNull;

/**
 * Tickets are being reserved for an event at this Venue
 * <p>
 * Implements ticket service as we do not want to allow access to seat objects
 * outside this class
 */
@Component
public class DefaultTicketService implements TicketService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DefaultTicketService.class);
    private final Venue venue;

    Map<Integer, SeatHold> seatHoldMap = new ConcurrentHashMap<Integer, SeatHold>();
    Map<Integer, Reservation> reservationMap = new ConcurrentHashMap<>();

    //Store the seat layout linearly to make it easier to allocate/deallocate seats
    //Use a CopyOnWriteArrayList to support concurrency
    final List<Seat> seatCache;

    //Seat Index to quickly lookup seats. It is a umodifiable map
    Map<String, Seat> seatIndexMap;

    @Value("${ts.holdExpiresInMins : 5}")
    private int holdExpiresInMins = 5;

    @Autowired
    public DefaultTicketService(Venue venue) {
        notNull(venue, "Venue cannot be null");
        this.venue = venue;
        Seat[][] seatLayout = venue.getSeatLayout();

        //Create and initialize the seatLayout
        char rowName = 'A';

        ArrayList<Seat> tmpList = new ArrayList<>();
        //TODO: use stream collect
        for (int rowIndex = 0; rowIndex < venue.getNoOfRows(); rowIndex++, rowName++) {
            Seat[] row = seatLayout[rowIndex];
            tmpList.addAll(Arrays.asList(row).subList(0, venue.getNoOfSeatsPerRow()));
        }
        //convert to copyOnWriteList
        seatCache = new CopyOnWriteArrayList(tmpList);
        Map<String, Seat> tmpSeatMap = seatCache.stream().collect(Collectors.toMap(Seat::getId, seat -> seat));
        //create an unmodifiable map
        seatIndexMap = Collections.unmodifiableMap(tmpSeatMap);
    }

    @Override
    public int numSeatsAvailable() {
        return (int) seatCache.stream().filter(seat -> seat.getStatus() == ReservedStatus.UNRESERVED).count();
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        SeatHold seatHold;
        if (numSeatsAvailable() < numSeats) {
            throw new TicketServiceException(String.format("Cannot hold %d seats! %d seats available", numSeats, numSeatsAvailable()));
        }
        List<Seat> nextAvailableSeats = getNextAvailableSeats(numSeats);
        nextAvailableSeats.forEach(seat -> seat.setStatus(ReservedStatus.ON_HOLD));
        //Stores an immutable list. The seats cannot be modified
        seatHold = SeatHold.newSeatHold()
                .customerEmail(customerEmail)
                .holds(ImmutableList.copyOf(nextAvailableSeats)).build();
        seatHoldMap.put(seatHold.getId(), seatHold);
        return seatHold;
    }

    /**
     * Commit seats held for a specific customer
     *
     * @param seatHoldId the seat hold identifier
     * @param customerEmail the email address of the customer to which the seat hold is assigned
     * @return a reservation confirmation code
     */
    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        return reserveHeldSeats(seatHoldId, customerEmail, seatHoldMap, seatCache, reservationMap);
    }

    /**
     * Reserve seat held by the customer
     * Delegated to this method to facilitate unit testing
     * @param seatHoldId the seat hold identifier
     * @param customerEmail the email address of the customer holding the ticket
     * @param seatHoldMap the map of seat holds
     * @param seatCache seat cache containing the orignal list of seats
     * @param reservationMap the map of reservations to add the reservation to
     * @return
     */
    private String reserveHeldSeats(int seatHoldId, String customerEmail, Map<Integer, SeatHold> seatHoldMap, List<Seat> seatCache, Map<Integer, Reservation> reservationMap) {
        //verify if seatHold Exists and then reserve
        SeatHold seatHold = seatHoldMap.get(seatHoldId);
        if (seatHold == null) {
            throw new TicketServiceException(String.format("The seat hold id %d is not available", seatHoldId));
        }

        if (!seatHold.getCustomerEmail().equalsIgnoreCase(customerEmail)) {
            throw new TicketServiceException(String.format("Customer email: %s, not found", customerEmail));
        }

        //update the status to reserved
        //Extract all the hold seat copies and get the original seat list and
        //update the reserved status
        List<Seat> holds = seatHold.getHolds();
        List<Seat> tmpReserves = seatCache.stream()
                .filter(seat -> holds.stream().map(Seat::getId).anyMatch(id -> id.equals(seat.getId())))
                .collect(Collectors.toList());
        tmpReserves.stream().forEach(seat -> seat.setStatus(ReservedStatus.RESERVED));

        Reservation reservation = Reservation.newReservation()
                .reserves(ImmutableList.copyOf(tmpReserves)).customerEmail(seatHold.getCustomerEmail()).build();
        seatHoldMap.remove(seatHoldId);
        reservationMap.put(reservation.getId(), reservation);
        return String.valueOf(reservation.getId());
    }

    /**
     * Expire seat holds
     * TODO: Tie fixedDelay to expiry settings
     */
    @Scheduled(fixedDelay = 60000)
    void expireSeatHolds() {
        ArrayList<Integer> expiredIdList = new ArrayList<>();
        Date expiresIn = new Date(System.currentTimeMillis() - (60 * 1000 * holdExpiresInMins));
        seatHoldMap.forEach((key, value) -> {
            if (value.getHeldAt().before(expiresIn)) {
                value.getHolds().forEach(seat -> {
                    Seat heldSeat = seatIndexMap.get(seat.getId());
                    if (heldSeat != null) {
                        heldSeat.setStatus(ReservedStatus.RESERVED);
                    } else {
                        logger.warn(String.format("Held seat: %s,not found in seat cache", heldSeat));
                    }
                });
                expiredIdList.add(key);
            }
        });
        //remove expired holds from the map
        expiredIdList.forEach(seatHoldMap::remove);
    }

    void compactSeatAssignments() {
        if (numSeatsAvailable() > 0) {
            //find the first unreserved seat
            Optional<Seat> result =
                    seatCache.stream().filter(
                            seat -> seat.getStatus() == ReservedStatus.UNRESERVED).findFirst();
            //find the index of that seat in the seat cache
            result.ifPresent(firstUnreservedSeat -> {
                int index = seatCache.indexOf(firstUnreservedSeat);
                List<Seat> compactableList = seatCache.subList(index, seatCache.size() - 1);
                List<Seat> fragmentedSeats = compactableList.stream().filter(seat -> seat.getStatus() != ReservedStatus.UNRESERVED).collect(Collectors.toList());
                for (int i = 0; i < fragmentedSeats.size(); i++) {
                    //update the status of the compatable list with the status of the fragmented list
                    Seat seat = fragmentedSeats.get(i);
                    compactableList.get(index + i).setStatus(seat.getStatus());
                    seat.setStatus(ReservedStatus.UNRESERVED);
                }
            });
        }
    }

    /**
     * Get the next available seats. Before getting the available seats, the expired seats are cleaned up and the fragmented
     * seats are compacted
     *
     * @param numberOfSeats
     * @return
     */
    List<Seat> getNextAvailableSeats(int numberOfSeats) {
        List<Seat> availableSeats = new ArrayList<>();
        //need to expire ticket holds to get accurate results
        expireSeatHolds();
        compactSeatAssignments();
        Optional<Seat> firstSeat = seatCache.stream().filter(
                seat -> seat.getStatus() == ReservedStatus.UNRESERVED).findFirst();
        if (firstSeat.isPresent()) {
            int fromIndex = seatCache.indexOf(firstSeat.get());
            availableSeats = seatCache.subList(fromIndex, fromIndex + numberOfSeats);
        }
        return availableSeats;
    }


    public String toString() {
        String[][] data = new String[1][3];
        TableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);

        data[0][0] = getSeatLayoutView();
        tableBuilder.on(at(0, 0)).addAligner(SimpleHorizontalAligner.values()[0]);
        tableBuilder.on(at(0, 0)).addAligner(SimpleVerticalAligner.values()[0]);

        data[0][1] = getSeatHoldsView();
        tableBuilder.on(at(0, 1)).addAligner(SimpleHorizontalAligner.values()[0]);
        tableBuilder.on(at(0, 1)).addAligner(SimpleVerticalAligner.values()[0]);

        data[0][2] = getReservationView();
        tableBuilder.on(at(0, 2)).addAligner(SimpleHorizontalAligner.values()[0]);
        tableBuilder.on(at(0, 2)).addAligner(SimpleVerticalAligner.values()[0]);

        return tableBuilder.addFullBorder(BorderStyle.fancy_light).build().render(120);
    }

    private String getSeatHoldsView() {
        return seatHoldMap.values().stream().map(Object::toString)
                .collect(Collectors.joining("\n"));
    }

    private String getReservationView() {
        return reservationMap.values().stream().map(Object::toString)
                .collect(Collectors.joining("\n"));
    }

    private String getSeatLayoutView() {
        StringBuilder stringBuilder = new StringBuilder();
        //print stage, width 15
        stringBuilder.append("||||||||||||________________||||||||||\n");

        Arrays.stream(venue.getSeatLayout()).forEach(row -> {
            //Print row id
            stringBuilder.append(row[0].getRowName()).append(" ");
            Arrays.stream(row).forEach(seat -> {
                String symbol = "-";
                switch (seat.getStatus()) {
                    case ON_HOLD:
                        symbol = "h";
                        break;
                    case UNRESERVED:
                        symbol = "-";
                        break;
                    case RESERVED:
                        symbol = "R";
                        break;
                }
                stringBuilder.append(symbol);
            });
            stringBuilder.append(" ").append(row[0].getRowName()).append("\n");
        });
        stringBuilder.append("Number Of Seats Available: ").append(numSeatsAvailable());
        return stringBuilder.toString();
    }
}
