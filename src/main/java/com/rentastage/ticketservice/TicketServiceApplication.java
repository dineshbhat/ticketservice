package com.rentastage.ticketservice;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;

@SpringBootApplication
public class TicketServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(TicketServiceApplication.class, args);
  }

  @Bean
  public PromptProvider myPromptProvider() {
    return () -> new AttributedString("Rent-A-Stage:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
  }
}
