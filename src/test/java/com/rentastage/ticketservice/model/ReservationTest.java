package com.rentastage.ticketservice.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ReservationTest {

  @Test
  public void newReservation() {
  }

  @Test
  public void testToString() {
    Seat[] holds = {
        Seat.newSeat().number(1).rowName("A").build(),
        Seat.newSeat().number(2).rowName("B").build()
    };
    String customerEmail = "a@bc.com";
    Reservation seatHold = Reservation.newReservation().reserves(Arrays.asList(holds)).customerEmail(customerEmail).build();

    String actual = seatHold.toString();
    assertThat(actual, allOf(startsWith("Reserved|"), endsWith("|a@bc.com")));
    assertThat(actual.split("\\|").length, is(5));
  }

  @Test
  public void build() {
    Seat[] holds = {
        Seat.newSeat().number(1).rowName("A").build(),
        Seat.newSeat().number(2).rowName("B").build()
    };
    String customerEmail = "a@bc.com";
    Reservation reservation = Reservation.newReservation().reserves(Arrays.asList(holds)).customerEmail(customerEmail).build();

    assertEquals("Incorrect Customer email!", customerEmail, reservation.getCustomerEmail());
    assertNotNull("HeldAt cannot be null!", reservation.getHeldAt());

    List<Seat> actualReserves = reservation.getReserves();
    assertThat(actualReserves.size(), is(2));
    assertThat(actualReserves.get(0).getNumber(), is(1));
    assertThat(actualReserves.get(0).getRowName(), is("A"));
    assertThat(actualReserves.get(0).getStatus(), is(ReservedStatus.RESERVED));
    assertThat(actualReserves.get(1).getNumber(), is(2));
    assertThat(actualReserves.get(1).getRowName(), is("B"));
    assertThat(actualReserves.get(1).getStatus(), is(ReservedStatus.RESERVED));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullReserves() {
    Reservation.newReservation().reserves(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyHolds() {
    Reservation.newReservation().reserves(new ArrayList<>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullCustomerEmail() {
    Reservation.newReservation().customerEmail(null);
  }
}