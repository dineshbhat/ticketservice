package com.rentastage.ticketservice.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SeatTest {

  @Test
  public void newSeat() {
    Seat seat = Seat.newSeat().rowName("A").number(1).build();
    assertEquals("Incorrect row name", "A", seat.getRowName());
    assertEquals("Incorrect set number", 1, seat.getNumber());
    assertNotNull("Id cannot be null", seat.getId());
    assertEquals("Incorrect Reserved Status",ReservedStatus.UNRESERVED, seat.getStatus());
  }

  @Test
  public void TestToString() {
    Seat seat = Seat.newSeat().rowName("A").number(1).build();
    assertEquals("Unexpected ToString output", "A1", seat.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullRowName() {
    Seat.newSeat().rowName(null);
  }

  @Test(expected = IllegalStateException.class)
  public void numberGreaterThan0() {
    Seat.newSeat().number(0);
  }
}