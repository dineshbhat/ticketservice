package com.rentastage.ticketservice;

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
public class Venue implements TicketService {
  private static final int NO_OF_ROWS = 10;
  private static final int NO_OF_SEATS_PER_ROW = 34;

  Seat[][] getSeatLayout() {
    return seatLayout;
  }

  private Seat[][] seatLayout = null;

  private HashMap<Integer, SeatHold> seatHoldMap = new HashMap<>();
  private HashMap<Integer, Reservation> reservationMap = new HashMap<>();

  //Store the seat layout linearly to make it easier to allocate/deallocate seats
  private ArrayList<Seat> seatCache = new ArrayList<>();

  private static final int HOLD_EXPIRED_IN_MINS = 5;

  private static int noOfSeatsAvailable = NO_OF_SEATS_PER_ROW * NO_OF_ROWS;


  public Venue() {
    this.seatLayout = new Seat[NO_OF_ROWS][NO_OF_SEATS_PER_ROW];

    //Create and initialize the seatLayout
    char rowName = 'A';

    for (int rowIndex = 0; rowIndex < NO_OF_ROWS; rowIndex++, rowName++) {
      for (int colIndex = 0; colIndex < NO_OF_SEATS_PER_ROW; colIndex++) {
        seatLayout[rowIndex][colIndex] = Seat.newSeat()
            .rowName(String.valueOf(rowName))
            .number(colIndex + 1)
            .build();
        seatCache.add(seatLayout[rowIndex][colIndex]);
      }
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
    SeatHold seatHold = null;
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
    Reservation reservation = null;

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
      seatHold.getHolds().forEach(seat -> seat.setStatus(ReservedStatus.RESERVED));
      reservation = Reservation.newReservation()
          .holds(seatHold.getHolds()).customerEmail(seatHold.getCustomerEmail()).build();
      seatHoldMap.remove(seatHoldId);
      reservationMap.put(reservation.getId(), reservation);
      return String.valueOf(reservation.getId());
    }
  }

  private void expireSeatHolds() {
    synchronized (this) {
      ArrayList<Integer> expiredIdList = new ArrayList<>();
      Date expiresIn = new Date(System.currentTimeMillis() - (60 * 1000 * HOLD_EXPIRED_IN_MINS));
      seatHoldMap.forEach((key, value) -> {
        if (value.getHeldAt().before(expiresIn)) {
          value.getHolds().forEach(seat -> {
            seat.setStatus(ReservedStatus.UNRESERVED);
          });
          expiredIdList.add(key);
        }
        ;
      });
      //remove expired holds from the map
      expiredIdList.forEach(id -> seatHoldMap.remove(id));
    }
  }

  private void compactSeatAssignments() {
    synchronized (this) {
      if (numSeatsAvailable() > 0) {
        //find the first unreserved seat
        Optional<Seat> result =
            seatCache.stream().filter(
                seat -> seat.getStatus() == ReservedStatus.UNRESERVED).findFirst();
        //find the index of that seat in the seatcache
        if (result.isPresent()) {
          Seat firstUnreservedSeat = result.get();
          int index = seatCache.indexOf(firstUnreservedSeat);
          List<Seat> compactableList = seatCache.subList(index, seatCache.size() - 1);
          List<Seat> fragmentedSeats = compactableList.stream().filter(seat -> seat.getStatus() != ReservedStatus.UNRESERVED).collect(Collectors.toList());
          for (int i = 0; i < fragmentedSeats.size(); i++) {
            //update the status of the compatable list with the status of the fragmented list
            Seat seat = fragmentedSeats.get(i);
            compactableList.get(index + i).setStatus(seat.getStatus());
            seat.setStatus(ReservedStatus.UNRESERVED);
          }
        }
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

    Arrays.stream(seatLayout).forEach(row -> {
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
