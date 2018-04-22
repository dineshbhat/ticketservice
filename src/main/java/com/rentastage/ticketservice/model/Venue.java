package com.rentastage.ticketservice.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Tickets are being reserved for an event at this Venue
 * Immutable once created. Only Seat status can be changed
 */
@Component
public class Venue {
  //TODO: Make the following configurable

  @Value("${ts.noOfRows ?: 10}")
  private int noOfRows = 10;

  @Value("${ts.noOfSeatsPerRow ?: 34}")
  private int noOfSeatsPerRow = 34;

  private final Seat[][] seatLayout;

  public Venue() {
    this.seatLayout = createSeatLayout(noOfRows, noOfSeatsPerRow);
  }

  /**
   * Create seat layout of rows and seats. Row names are alphabetized
   *
   * @param noOfRows
   * @param noOfSeatsPerRow
   * @return seat layout, a 2d array of seats
   */
  static Seat[][] createSeatLayout(int noOfRows, int noOfSeatsPerRow) {
    Seat[][] tmpLayout = new Seat[noOfRows][noOfSeatsPerRow];

    //Create and initialize the seatLayout
    //Limitations: Rows are limited to ascii character range, need a better row generator scheme
    char rowName = 'A';

    for (int rowIndex = 0; rowIndex < noOfRows; rowIndex++, rowName++) {
      for (int colIndex = 0; colIndex < noOfSeatsPerRow; colIndex++) {
        tmpLayout[rowIndex][colIndex] = Seat.newSeat()
            .rowName(String.valueOf(rowName))
            .number(colIndex + 1)
            .build();
      }
    }
    return tmpLayout;
  }

  public int getNoOfRows() {
    return noOfRows;
  }

  public int getNoOfSeatsPerRow() {
    return noOfSeatsPerRow;
  }

  public Seat[][] getSeatLayout() {
    return seatLayout;
  }
}
