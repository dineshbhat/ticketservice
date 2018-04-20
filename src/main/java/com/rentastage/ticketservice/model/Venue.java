package com.rentastage.ticketservice.model;

import org.springframework.stereotype.Component;

/**
 * Tickets are being reserved for an event at this Venue
 */
@Component
public class Venue {
  //TODO: Make the following configurable
  private static final int NO_OF_ROWS = 10;
  private static final int NO_OF_SEATS_PER_ROW = 34;

  private Seat[][] seatLayout;

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
      }
    }
  }

    public Seat[][] getSeatLayout() {
      return seatLayout;
  }
}
