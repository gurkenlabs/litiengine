package de.gurkenlabs.util.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ConsoleCommandListener extends Thread implements ICommandListener {

  private final List<ICommandManager> commandManagers;
  private boolean gameIsRunning = true;

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
    while (this.gameIsRunning) {

      final BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
      String s;
      try {
        s = bufferRead.readLine();

        this.commandManagers.forEach(manager -> manager.executeCommand(s));

      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void terminate() {
    this.gameIsRunning = false;
  }
}