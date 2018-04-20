package com.rentastage.ticketservice.model;

import org.springframework.util.Assert;

import java.util.UUID;

/**
 * Represents a seat that can be reserved
 */
public class Seat {

  private static final String DISPLAY_TEMPLATE = "%s%d";
  /**
   * Unique Seat ID
   */
  private final String id;

  /**
   * Row Name
   */
  private final String rowName;

  /**
   * Seat Number
   */
  private final int number;

  /**
   * Status of Seat
   */
  private ReservedStatus status = ReservedStatus.UNRESERVED;

  private Seat(Builder builder) {
    this.rowName = builder.rowName;
    this.number = builder.number;
    id = UUID.randomUUID().toString();
  }

  public static Builder newSeat() {
    return new Builder();
  }

  public String getId() {
    return id;
  }

  public String getRowName() {
    return rowName;
  }


  public int getNumber() {
    return number;
  }

  public ReservedStatus getStatus() {
    return status;
  }

  public void setStatus(ReservedStatus status) {
    this.status = status;
  }


  public static final class Builder {
    private String rowName;
    private int number;

    private Builder() {
    }

    public Seat build() {
      return new Seat(this);
    }

    public Builder rowName(String rowName) {
      Assert.notNull(rowName, "Row Name cannot be null");
      this.rowName = rowName;
      return this;
    }

    public Builder number(int number) {
      Assert.state(number > 0, "Row Number must be > 1");
      this.number = number;
      return this;
    }
  }

  /**
   * This method is used to display seat data in the shell
   * @return
   */
  @Override
  public String toString() {
    return String.format(DISPLAY_TEMPLATE, rowName, number);
  }
}
