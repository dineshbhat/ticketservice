package com.rentastage.ticketservice.model;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a hold on one or more seats
 */
public class Reservation {
  private final int id;

  private List<Seat> reserves;

  private final Date reservedAt;

  private final String customerEmail;

  private Reservation(Builder builder) {
    this.reserves = builder.holds;
    this.reservedAt = new Date();
    this.customerEmail = builder.customerEmail;
    id = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
  }

  public static Builder newReservation() {
    return new Builder();
  }

  public int getId() {
    return id;
  }

  public List<Seat> getReserves() {
    return reserves;
  }

  Date getHeldAt() {
    return reservedAt;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public static final class Builder {
    private List<Seat> holds;
    private String customerEmail;

    private Builder() {
    }

    public Reservation build() {
      return new Reservation(this);
    }

    public Builder holds(List<Seat> holds) {
      this.holds = holds;
      return this;
    }

    public Builder customerEmail(String customerEmail) {
      this.customerEmail = customerEmail;
      return this;
    }
  }

  @Override
  public String toString() {
    return String.format("Reserved|%d|%s|%s|%s", id, reserves, Utils.formatDate(reservedAt), customerEmail);
  }
}
