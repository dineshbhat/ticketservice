package com.rentastage.ticketservice.model;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Tests Venue
 */
public class VenueTest {

  private void assertSeatLayout(Seat[][] seatLayout) {
    char rowName = 'A';

    for (Seat[] row : seatLayout) {
      final int[] seatNumber = {1};
      char finalRowName = rowName;
      Arrays.stream(row).forEach(seat -> {
        assertEquals("Row Name is incorrect", String.valueOf(finalRowName), seat.getRowName());
        assertEquals("Seat Number is incorrect", seatNumber[0]++, seat.getNumber());
        assertEquals("Seat Status is incorrect", ReservedStatus.UNRESERVED, seat.getStatus());
      });
      rowName++;
    }
  }

  @Test
  public void getSeatLayout() {
    Venue venue = new Venue();
    assertSeatLayout(venue.getSeatLayout());
  }
}