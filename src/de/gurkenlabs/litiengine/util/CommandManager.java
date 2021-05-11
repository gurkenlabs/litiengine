package de.gurkenlabs.litiengine.util;

import de.gurkenlabs.litiengine.ILaunchable;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandManager implements ILaunchable {
  private static final Logger log = Logger.getLogger(CommandManager.class.getName());
  private final Map<String, Predicate<String[]>> commandConsumers;
  private final ConsoleCommandProcessor commandListener;

  public CommandManager() {
    this.commandConsumers = new ConcurrentHashMap<>();
    this.commandListener = new ConsoleCommandProcessor();
  }

  public void bind(final String command, final Predicate<String[]> commandConsumer) {
    if (this.commandConsumers.containsKey(command)) {
      throw new IllegalArgumentException(
          "Cannot bind command " + command + " because it is already bound.");
    }

    this.commandConsumers.put(command, commandConsumer);
  }

  public boolean executeCommand(final String command) {
    if (command == null || command.isEmpty()) {
      return false;
    }

    log.log(Level.FINE, "Command received: {0}", command);
    final String[] arr = command.split(" ");
    if (arr.length == 0) {
      return false;
    }

    final String keyword = arr[0];
    if (!this.commandConsumers.containsKey(keyword)) {
      return false;
    }

    return this.commandConsumers.get(keyword).test(arr);
  }

  @Override
  public void start() {
    this.commandListener.start();
  }

  @Override
  public void terminate() {
    this.commandListener.terminate();
  }

  private class ConsoleCommandProcessor extends Thread implements ILaunchable {
    ConsoleCommandProcessor() {
      this.setDaemon(true);
    }

    @Override
    public void run() {
      final Scanner scanner = new Scanner(System.in);
      while (!interrupted()) {
        String s;
        s = scanner.nextLine();

        CommandManager.this.executeCommand(s);

        try {
          sleep(10);
        } catch (InterruptedException e) {
          interrupt();
          break;
        }
      }
      scanner.close();
    }

    @Override
    public void terminate() {
      interrupt();
    }
  }
}
