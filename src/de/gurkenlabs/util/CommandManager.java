package de.gurkenlabs.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandManager implements ICommandManager {
  private static final Logger log = Logger.getLogger(CommandManager.class.getName());
  private final Map<String, Function<String[], Boolean>> commandConsumers;
  private final ConsoleCommandListener commandListener;

  public CommandManager() {
    this.commandConsumers = new ConcurrentHashMap<>();
    this.commandListener = new ConsoleCommandListener(this);
  }

  @Override
  public void bind(final String command, final Function<String[], Boolean> commandConsumer) {
    if (this.commandConsumers.containsKey(command)) {
      throw new IllegalArgumentException("Cannot bind command " + command + " because it is already bound.");
    }

    this.commandConsumers.put(command, commandConsumer);
  }

  @Override
  public boolean executeCommand(final String command) {
    if (command == null || command.isEmpty()) {
      return false;
    }

    log.log(Level.FINE, "Command received: %s", command);
    final String[] arr = command.split(" ");
    if (arr.length == 0) {
      return false;
    }

    final String keyword = arr[0];
    if (!this.commandConsumers.containsKey(keyword)) {
      return false;
    }

    return this.commandConsumers.get(keyword).apply(arr);
  }

  @Override
  public void start() {
    this.commandListener.start();
  }

  @Override
  public void terminate() {
    this.commandListener.terminate();
  }
}
