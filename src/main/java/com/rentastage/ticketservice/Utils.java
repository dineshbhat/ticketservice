package com.rentastage.ticketservice;

import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.util.Date;


class Utils {
  public static String formatDate(Date date){
    Assert.notNull(date, "Date cannot be null");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy hh:mm");
    return simpleDateFormat.format(date);
  }
}
