package com.rentastage.ticketservice;

import com.rentastage.ticketservice.model.ReservedStatus;
import com.rentastage.ticketservice.model.SeatHold;
import com.rentastage.ticketservice.model.Venue;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DefaultTicketServiceTest {

  Venue venue = new Venue();

  DefaultTicketService defaultTicketService = new DefaultTicketService(venue);

  @Test
  public void verifyInitialization() {
    assertThat(defaultTicketService.seatCache.size(), is(venue.getNoOfRows() * venue.getNoOfSeatsPerRow()));
  }

  @Test
  public void numSeatsAvailable() {
    int noOfSeats = venue.getNoOfRows() * venue.getNoOfSeatsPerRow();

    assertThat(defaultTicketService.numSeatsAvailable(), is(noOfSeats));

    int holdSeats = 10;
    defaultTicketService.findAndHoldSeats(holdSeats, "a@b.com");

    assertThat("Number of Seats Avaialable should reduce by 10", defaultTicketService.numSeatsAvailable(), is(noOfSeats - holdSeats));
  }

  @Test
  public void findAndHoldSeats() {
    int holdSeats = 10;
    SeatHold seatHold = defaultTicketService.findAndHoldSeats(holdSeats, "a@b.com");
    assertThat("seat cache should be updated with ON_HOLD seats",
        (int) defaultTicketService.seatCache.stream().filter(seat -> seat.getStatus() == ReservedStatus.ON_HOLD).count(),
        is(holdSeats));
    assertThat("seat cache should should not have any reserved seats",
        (int) defaultTicketService.seatCache.stream().filter(seat -> seat.getStatus() == ReservedStatus.RESERVED).count(),
        is(0));
    assertThat("seat hold map should have one record", defaultTicketService.seatHoldMap.size(), is(1));
    assertThat(String.format("%d seats should be on hold in the Seat Hold Record", holdSeats),
        seatHold.getHolds().size(), is(holdSeats));

    //When no seats available
    holdSeats = defaultTicketService.numSeatsAvailable();
    seatHold = defaultTicketService.findAndHoldSeats(holdSeats, "a@b.com");
    assertThat("seat hold map should have 2 records", defaultTicketService.seatHoldMap.size(), is(2));
    assertThat(String.format("%d seats should be on hold in the Seat Hold Record", holdSeats),
        seatHold.getHolds().size(), is(holdSeats));

    assertThat("All seats should be booked", defaultTicketService.numSeatsAvailable(), is(0));
    try {
      seatHold = defaultTicketService.findAndHoldSeats(1, "a@b.com");
      fail("Expected TicketService Exception");
    } catch (TicketServiceException e) {
      //Expected
      ;
    }
  }


  @Test(expected = IllegalArgumentException.class)
  public void invalidNumSeatdFindAndHoldSeat() {
    defaultTicketService.findAndHoldSeats(0, "abx@d.com");
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidEmailFindAndHoldSeat() {
    defaultTicketService.findAndHoldSeats(0, null);
  }

  @Test
  public void reserveSeats() {
    int reservedSeats = 10;
    String customerEmail = "a@b.com";
    SeatHold seatHold = defaultTicketService.findAndHoldSeats(reservedSeats, customerEmail);

    String reservationId = defaultTicketService.reserveSeats(seatHold.getId(), customerEmail);
    assertThat(String.format("seat cache should should have %d reserved seats", reservedSeats),
        (int) defaultTicketService.seatCache.stream().filter(seat -> seat.getStatus() == ReservedStatus.ON_HOLD).count(),
        is(0));
    assertThat(String.format("seat cache should should have %d reserved seats", reservedSeats),
        (int) defaultTicketService.seatCache.stream().filter(seat -> seat.getStatus() == ReservedStatus.RESERVED).count(),
        is(reservedSeats));

    assertThat(defaultTicketService.reservationMap.size(), is(1));
  }

  @Test(expected = TicketServiceException.class)
  public void invalidSeatHoldIdReserveSeats() {
    defaultTicketService.reserveSeats(1, "test");
  }

  @Test(expected = TicketServiceException.class)
  public void invalidEmailIdReserveSeats() {
    int reservedSeats = 10;
    String customerEmail = "a@b.com";
    SeatHold seatHold = defaultTicketService.findAndHoldSeats(reservedSeats, customerEmail);
    defaultTicketService.reserveSeats(seatHold.getId(), "test");
  }

  @Test
  public void testToString() {
  }
}