package com.rentastage.ticketservice.model;

import com.rentastage.ticketservice.model.Utils;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

  @Test
  public void formatDate() {
    String formattedDate = Utils.formatDate(new Date(0));
    assertEquals("12/31/69 07:00", formattedDate);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullData(){
    Utils.formatDate(null);
  }
}