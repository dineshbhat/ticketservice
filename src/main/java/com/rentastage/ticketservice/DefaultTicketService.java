package com.rentastage.ticketservice;

import com.rentastage.ticketservice.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.shell.table.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.shell.table.CellMatchers.at;

/**
 * Tickets are being reserved for an event at this Venue
 * <p>
 * Implements ticket service as we do not want to allow access to seat objects
 * outside this class
 */
@Component
public class DefaultTicketService implements TicketService {

  private final Venue venue;

  //TODO: Make the following configurable
  private static final int NO_OF_ROWS = 10;
  private static final int NO_OF_SEATS_PER_ROW = 34;

  private final HashMap<Integer, SeatHold> seatHoldMap = new HashMap<>();
  private final HashMap<Integer, Reservation> reservationMap = new HashMap<>();

  //Store the seat layout linearly to make it easier to allocate/deallocate seats
  private final ArrayList<Seat> seatCache = new ArrayList<>();

  @Value("${ts.holdExpiresInMins:5}")
  private int holdExpiresInMins;

  @Autowired
  public DefaultTicketService(Venue venue) {
    this.venue = venue;
    Seat[][] seatLayout = venue.getSeatLayout();

    //Create and initialize the seatLayout
    char rowName = 'A';

    //TODO: use stream collect
    for (int rowIndex = 0; rowIndex < NO_OF_ROWS; rowIndex++, rowName++) {
      seatCache.addAll(Arrays.asList(seatLayout[rowIndex]).subList(0, NO_OF_SEATS_PER_ROW));
    }
  }

  @Override
  public int numSeatsAvailable() {
    synchronized (this) {
      return (int) seatCache.stream().filter(seat -> seat.getStatus() == ReservedStatus.UNRESERVED).count();
    }
  }

  @Override
  public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
    SeatHold seatHold;
    synchronized (this) {
      if (numSeatsAvailable() < numSeats) {
        throw new TicketServiceException(String.format("Cannot hold %d seats! %d seats available", numSeats, numSeatsAvailable()));
      }
      List<Seat> nextAvailableSeats = getNextAvailableSeats(numSeats);
      nextAvailableSeats.forEach(seat -> seat.setStatus(ReservedStatus.ON_HOLD));
      seatHold = SeatHold.newSeatHold().customerEmail(customerEmail).holds(nextAvailableSeats).build();
      seatHoldMap.put(seatHold.getId(), seatHold);
    }
    return seatHold;
  }

  @Override
  public String reserveSeats(int seatHoldId, String customerEmail) {
    synchronized (this) {
      //verify if seatHold Exists and then reserve
      SeatHold seatHold = seatHoldMap.get(seatHoldId);
      if (seatHold == null) {
        throw new TicketServiceException(String.format("The seat hold id %d is not available", seatHoldId));
      }

      if (!seatHold.getCustomerEmail().equalsIgnoreCase(customerEmail)) {
        throw new TicketServiceException(String.format("Customer email: %s, not found", customerEmail));
      }

      //update the status to reserved
      Reservation reservation = Reservation.newReservation()
          .reserves(seatHold.getHolds()).customerEmail(seatHold.getCustomerEmail()).build();
      seatHoldMap.remove(seatHoldId);
      reservationMap.put(reservation.getId(), reservation);
      return String.valueOf(reservation.getId());
    }
  }

  @Scheduled(fixedDelay = 60000)
  private void expireSeatHolds() {
    synchronized (this) {
      ArrayList<Integer> expiredIdList = new ArrayList<>();
      Date expiresIn = new Date(System.currentTimeMillis() - (60 * 1000 * holdExpiresInMins));
      seatHoldMap.forEach((key, value) -> {
        if (value.getHeldAt().before(expiresIn)) {
          value.getHolds().forEach(seat -> seat.setStatus(ReservedStatus.UNRESERVED));
          expiredIdList.add(key);
        }
      });
      //remove expired reserves from the map
      expiredIdList.forEach(seatHoldMap::remove);
    }
  }

  private void compactSeatAssignments() {
    synchronized (this) {
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
  }

  private List<Seat> getNextAvailableSeats(int numberOfSeats) {
    synchronized (this) {
      List<Seat> availableSeats = new ArrayList<>();
      compactSeatAssignments();
      Optional<Seat> firstSeat = seatCache.stream().filter(
          seat -> seat.getStatus() == ReservedStatus.UNRESERVED).findFirst();
      if (firstSeat.isPresent()) {
        int fromIndex = seatCache.indexOf(firstSeat.get());
        availableSeats = seatCache.subList(fromIndex, fromIndex + numberOfSeats);
      }
      return availableSeats;
    }
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
    return seatHoldMap.values().stream().map( Object::toString )
        .collect( Collectors.joining( "\n" ) );
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
