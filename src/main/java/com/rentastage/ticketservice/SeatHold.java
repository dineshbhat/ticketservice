package com.rentastage.ticketservice;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a hold on one or more seats
 */
public class SeatHold {
  private final int id;

  private List<Seat> holds;

  private final Date heldAt;

  private final String customerEmail;

  private SeatHold(Builder builder) {
    this.holds = builder.holds;
    this.heldAt = new Date();
    this.customerEmail = builder.customerEmail;
    id = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
  }

  public static Builder newSeatHold() {
    return new Builder();
  }

  public int getId() {
    return id;
  }

  public List<Seat> getHolds() {
    return holds;
  }

  Date getHeldAt() {
    return heldAt;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public static final class Builder {
    private List<Seat> holds;
    private String customerEmail;

    private Builder() {
    }

    public SeatHold build() {
      return new SeatHold(this);
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
    return String.format("Hold|%d|%s|%s|%s", id, holds, Utils.formatDate(heldAt), customerEmail);
  }

}
