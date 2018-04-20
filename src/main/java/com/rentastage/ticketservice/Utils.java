package com.rentastage.ticketservice;

import java.text.SimpleDateFormat;
import java.util.Date;


class Utils {
  public static String formatDate(Date date){
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy hh:mm");
    return simpleDateFormat.format(date);
  }
}
