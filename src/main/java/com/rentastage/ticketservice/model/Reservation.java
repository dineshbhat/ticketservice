package com.rentastage.ticketservice.model;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.util.Assert.notEmpty;
import static org.springframework.util.Assert.notNull;

/**
 * Represents a hold on one or more seats
 */
public class Reservation {
  private final int id;

  private final List<Seat> reserves;

  private final Date reservedAt;

  private final String customerEmail;

  private Reservation(Builder builder) {
    this.reserves = builder.reserves;
    //Set the status to RESERVED
    reserves.forEach(seat -> seat.setStatus(ReservedStatus.RESERVED));
    this.reservedAt = new Date();
    this.customerEmail = builder.customerEmail;
    id = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
  }

  public static Builder newReservation() {
    return new Builder();
  }

  public List<Seat> getReserves() {
    return reserves;
  }

  public int getId() {
    return id;
  }

  Date getHeldAt() {
    return reservedAt;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public static final class Builder {
    private List<Seat> reserves;
    private String customerEmail;

    private Builder() {
    }

    public Reservation build() {
      return new Reservation(this);
    }

    public Builder reserves(List<Seat> reserves) {
      notNull(reserves, "Holds cannot be null");
      notEmpty(reserves, "Holds cannot be empty");
      this.reserves = reserves;
      return this;
    }

    public Builder customerEmail(String customerEmail) {
      notNull(customerEmail, "Customer Email cannot be null");
      this.customerEmail = customerEmail;
      return this;
    }
  }

  @Override
  public String toString() {
    return String.format("Reserved|%d|%s|%s|%s", id, reserves, Utils.formatDate(reservedAt), customerEmail);
  }
}
