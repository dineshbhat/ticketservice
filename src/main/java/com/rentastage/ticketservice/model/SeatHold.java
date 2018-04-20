package com.rentastage.ticketservice.model;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.util.Assert.notEmpty;
import static org.springframework.util.Assert.notNull;

/**
 * Represents a hold on one or more seats
 */
public class SeatHold {
  private static final String DISPLAY_TEMPLATE = "Hold|%d|%s|%s|%s";
  private final int id;

  private final List<Seat> holds;

  private final Date heldAt;

  private final String customerEmail;

  private SeatHold(Builder builder) {
    this.holds = builder.holds;
    //Set the status to HOLD
    holds.forEach(seat -> seat.setStatus(ReservedStatus.ON_HOLD));
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

  public Date getHeldAt() {
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
      notNull(holds, "Holds cannot be null");
      notEmpty(holds, "Holds cannot be empty");
      this.holds = holds;
      return this;
    }

    public Builder customerEmail(String customerEmail) {
      notNull(customerEmail, "Customer Email cannot be null");
      this.customerEmail = customerEmail;
      return this;
    }
  }

  /**
   * This method is used for display of Seat Holds in  shell
   * @return
   */
  @Override
  public String toString() {
    return String.format(DISPLAY_TEMPLATE, id, holds, Utils.formatDate(heldAt), customerEmail);
  }

}
