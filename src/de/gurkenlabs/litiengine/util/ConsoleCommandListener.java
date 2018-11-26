package de.gurkenlabs.litiengine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleCommandListener extends Thread implements ICommandListener {
  private final List<ICommandManager> commandManagers;

  public ConsoleCommandListener(final ICommandManager... commandManagers) {
    this.setDaemon(true);
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
    final Scanner scanner = new Scanner(System.in);
    while (!interrupted()) {
      String s;
      s = scanner.nextLine();

      this.commandManagers.forEach(manager -> manager.executeCommand(s));
    }
    scanner.close();
  }

  @Override
  public void terminate() {
    interrupt();
  }
}