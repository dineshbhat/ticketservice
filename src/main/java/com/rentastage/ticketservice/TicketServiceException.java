package com.rentastage.ticketservice;

/**
 * Represents an exception when a seat is not available
 */
class TicketServiceException extends RuntimeException {
  public TicketServiceException(String message) {
    super(message);
  }
}
