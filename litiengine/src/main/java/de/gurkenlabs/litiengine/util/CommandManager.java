package de.gurkenlabs.litiengine.util;

import static java.lang.Thread.sleep;

import de.gurkenlabs.litiengine.ILaunchable;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the binding and execution of commands. Implements the ILaunchable interface to provide start and terminate functionality.
 */
public class CommandManager implements ILaunchable {
  private static final Logger log = Logger.getLogger(CommandManager.class.getName());
  private final Map<String, Predicate<String[]>> commandConsumers;
  private final ConsoleCommandProcessor commandListener;

  /**
   * Constructs a new CommandManager. Initializes the commandConsumers map and the commandListener.
   */
  public CommandManager() {
    this.commandConsumers = new ConcurrentHashMap<>();
    this.commandListener = new ConsoleCommandProcessor();
  }

  /**
   * Binds a command to a specific consumer. If the command is already bound, an IllegalArgumentException is thrown.
   *
   * @param command         the command to bind
   * @param commandConsumer the consumer to bind to the command
   * @throws IllegalArgumentException if the command is already bound
   */
  public void bind(final String command, final Predicate<String[]> commandConsumer) {
    if (this.commandConsumers.containsKey(command)) {
      throw new IllegalArgumentException(
        "Cannot bind command " + command + " because it is already bound.");
    }

    this.commandConsumers.put(command, commandConsumer);
  }

  /**
   * Executes a command by finding the corresponding consumer and passing the command arguments to it. If the command is null, empty, or not bound to
   * any consumer, it returns false.
   *
   * @param command the command to execute
   * @return true if the command was successfully executed, false otherwise
   */
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

  /**
   * A processor for handling console commands. Runs in a virtual thread and listens for commands from the console input.
   */
  private class ConsoleCommandProcessor {
    private Thread virtualThread;

    ConsoleCommandProcessor() {

    }

    /**
     * Starts the CommandManager by initiating the command listener.
     */
    public void start() {
      virtualThread = Thread.ofVirtual().start(this::run);
    }

    /**
     * Listens for commands from the console input and executes them. Runs in a loop until the thread is interrupted.
     */
    public void run() {
      final Scanner scanner = new Scanner(System.in);
      while (!Thread.currentThread().isInterrupted()) {
        String s;
        s = scanner.nextLine();

        CommandManager.this.executeCommand(s);

        try {
          sleep(10);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
      scanner.close();
    }

    /**
     * Terminates the CommandManager by interrupting the command listener thread.
     */
    public void terminate() {
      if (virtualThread != null) {
        virtualThread.interrupt();
      }
    }
  }
}
