package com.rentastage.ticketservice.model;

import java.util.UUID;

/**
 * Represents a seat that can be reserved
 */
public class Seat {

  /**
   * Unique Seat ID
   */
  private final String id;

  /**
   * Row Name
   */
  private String rowName;

  /**
   * Seat Number
   */
  private int number;

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
      this.rowName = rowName;
      return this;
    }

    public Builder number(int number) {
      this.number = number;
      return this;
    }
  }

  @Override
  public String toString() {
    return String.format("%s%d", rowName, number);
  }
}
