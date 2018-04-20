package com.rentastage.ticketservice;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

  @Test
  public void formatDate() {
    String formatedDate = Utils.formatDate(new Date(0));
    assertEquals("12/31/69 07:00", formatedDate);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullData(){
    Utils.formatDate(null);
  }
}