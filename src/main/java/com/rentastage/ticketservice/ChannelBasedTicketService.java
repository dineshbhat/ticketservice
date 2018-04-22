package com.rentastage.ticketservice;

import com.rentastage.ticketservice.model.SeatHold;

public class ChannelBasedTicketService implements TicketService {

    @Override
    public int numSeatsAvailable() {
        return 0;
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        return null;
    }

    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        return null;
    }
}
