package de.gurkenlabs.litiengine.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleCommandListener extends Thread implements ICommandListener {
  private static final Logger log = Logger.getLogger(ConsoleCommandListener.class.getName());

  private final List<ICommandManager> commandManagers;

  public ConsoleCommandListener(final ICommandManager... commandManagers) {
    this.commandManagers = new ArrayList<>();

    if (commandManagers != null && commandManagers.length > 0) {
      for (final ICommandManager manager : commandManagers) {
        if (!this.commandManagers.contains(manager)) {
          this.commandManagers.add(manager);
        }
      }
    }
  }

  @Override
  public void register(final ICommandManager manager) {
    if (!this.commandManagers.contains(manager)) {
      this.commandManagers.add(manager);
    }
  }

  @Override
  public void run() {
    while (!interrupted()) {

      final BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
      String s;
      try {
        s = bufferRead.readLine();

        this.commandManagers.forEach(manager -> manager.executeCommand(s));

      } catch (final IOException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }

      try {
        Thread.sleep(500);
      } catch (final InterruptedException e) {
        break;
      }
    }
  }

  @Override
  public void terminate() {
    interrupt();
  }
}