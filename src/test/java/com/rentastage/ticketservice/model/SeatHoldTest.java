package com.rentastage.ticketservice.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SeatHoldTest {

  @Test
  public void testToString() {
    Seat[] holds = {
        Seat.newSeat().number(1).rowName("A").build(),
        Seat.newSeat().number(2).rowName("B").build()
    };
    String customerEmail = "a@bc.com";
    Arrays.stream(holds).forEach(seat -> seat.setStatus(ReservedStatus.ON_HOLD));
    SeatHold seatHold = SeatHold.newSeatHold().holds(Arrays.asList(holds)).customerEmail(customerEmail).build();

    String actual = seatHold.toString();
    assertThat(actual, allOf(startsWith("Hold|"), endsWith("|a@bc.com")));
    assertThat(actual.split("\\|").length, is(5));
  }

  @Test
  public void build() {
    Seat[] holds = {
        Seat.newSeat().number(1).rowName("A").build(),
        Seat.newSeat().number(2).rowName("B").build()
    };
    String customerEmail = "a@bc.com";
    Arrays.stream(holds).forEach(seat -> seat.setStatus(ReservedStatus.ON_HOLD));
    SeatHold seatHold = SeatHold.newSeatHold().holds(Arrays.asList(holds)).customerEmail(customerEmail).build();

    assertEquals("Incorrect Customer email!", customerEmail, seatHold.getCustomerEmail());
    assertNotNull("HeldAt cannot be null!", seatHold.getHeldAt());
    List<Seat> actualHolds = seatHold.getHolds();
    assertThat(actualHolds.size(), is(2));
    assertThat(actualHolds.get(0).getNumber(), is(1));
    assertThat(actualHolds.get(0).getRowName(), is("A"));
    assertThat(actualHolds.get(0).getStatus(), is(ReservedStatus.ON_HOLD));
    assertThat(actualHolds.get(1).getNumber(), is(2));
    assertThat(actualHolds.get(1).getRowName(), is("B"));
    assertThat(actualHolds.get(1).getStatus(), is(ReservedStatus.ON_HOLD));
  }

  @Test (expected = IllegalArgumentException.class)
  public void nullHolds() {
    SeatHold.newSeatHold().holds(null);
  }

  @Test (expected = IllegalArgumentException.class)
  public void emptyHolds() {
    SeatHold.newSeatHold().holds(new ArrayList<>());
  }

  @Test (expected = IllegalArgumentException.class)
  public void nullCustomerEmail() {
    SeatHold.newSeatHold().customerEmail(null);
  }
}