package com.rentastage.ticketservice;

/**
 * Represents an exception when a seat is not available
 */
public class TicketServiceException extends RuntimeException {
  public TicketServiceException() {
  }

  public TicketServiceException(String message) {
    super(message);
  }

  public TicketServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public TicketServiceException(Throwable cause) {
    super(cause);
  }

  public TicketServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
