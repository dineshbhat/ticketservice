package com.rentastage.ticketservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

//Defines all the commands that can be run in the shell
@ShellComponent(value = "Your Ticket Service!!")
public class Commands {

  private final
  TicketService venue;

  @Autowired
  public Commands(TicketService venue) {
    this.venue = venue;
  }

  @ShellMethod("Show all Seats")
  public String showSeats() {
    return venue.toString();
  }

  @ShellMethod(value = "Get the number of available seats")
  public int getAvailableSeats() {
    return venue.numSeatsAvailable();
  }

  @ShellMethod(value = "Find and hold Seats")
  public String findAndHoldSeats(
      @ShellOption() int numSeats,
      @ShellOption() String customerEmail
  ) {
    try {
      venue.findAndHoldSeats(numSeats, customerEmail);
    } catch (Exception e) {
      return e.getLocalizedMessage();
    }
    return venue.toString();
  }

  @ShellMethod(value = "Reserve seats")
  public String reserveSeats(
      @ShellOption() int seatHoldId,
      @ShellOption() String customerEmail
  ) {
    try {
      venue.reserveSeats(seatHoldId, customerEmail);
    } catch (Exception e) {
      return e.getLocalizedMessage();
    }
    return venue.toString();
  }
}
