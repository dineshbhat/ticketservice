package com.rentastage.ticketservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;

@Configuration
public class TicketServiceConfig {
  private final Shell shell;

  @Autowired
  public TicketServiceConfig(Shell shell) {
    this.shell = shell;
  }

  @Bean
  public org.springframework.boot.CommandLineRunner commandLineRunner(ConfigurableEnvironment environment) {
    return new TicketServiceCLR(shell, environment);
  }

  @Bean
  public ExitCodeExceptionMapper exitCodeExceptionMapper() {
    return exception -> {
      Throwable e = exception;
      while (e != null && !(e instanceof ExitRequest)) {
        e = e.getCause();
      }
      return e == null ? 1 : ((ExitRequest) e).status();
    };
  }

}

/**
 * Example TicketServiceCLR that shows how overall shell behavior can be customized. In
 * this particular example, any program (process) arguments are assumed to be shell
 * commands that need to be executed (and the shell then quits).
 */
@Order(InteractiveShellApplicationRunner.PRECEDENCE - 2)
class TicketServiceCLR implements org.springframework.boot.CommandLineRunner {

  private Shell shell;

  private final ConfigurableEnvironment environment;

  public TicketServiceCLR(Shell shell, ConfigurableEnvironment environment) {
    this.shell = shell;
    this.environment = environment;
  }

  @Override
  public void run(String... args) throws Exception {
    //do nothing, we do not support running commands from the command line
  }
}

