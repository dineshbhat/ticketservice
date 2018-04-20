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

  private final Seat[][] seatLayout;

  public Venue() {
    this.seatLayout = createSeatLayout();
  }

  /**
   * Create seat layout of rows and seats. Row names are alphabetized
   *
   * @return seat layout, a 2d array of seats
   */
  static Seat[][] createSeatLayout() {
    Seat[][] tmpLayout = new Seat[NO_OF_ROWS][NO_OF_SEATS_PER_ROW];

    //Create and initialize the seatLayout
    //Limitations: Rows are limited to ascii character range, need a better row generator scheme
    char rowName = 'A';

    for (int rowIndex = 0; rowIndex < NO_OF_ROWS; rowIndex++, rowName++) {
      for (int colIndex = 0; colIndex < NO_OF_SEATS_PER_ROW; colIndex++) {
        tmpLayout[rowIndex][colIndex] = Seat.newSeat()
            .rowName(String.valueOf(rowName))
            .number(colIndex + 1)
            .build();
      }
    }
    return tmpLayout;
  }

  public Seat[][] getSeatLayout() {
    return seatLayout;
  }
}
