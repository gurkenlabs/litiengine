package de.gurkenlabs.litiengine;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.configuration.ClientConfiguration;

/**
 * Handles the uncaught exceptions that might occur while running a game or application with the LITIENGINE.
 * <p>
 * It provides proper logging of the exception in a {@code crash.txt} file in the game's root directory that can be
 * further used to report the issue if it's a generic one.
 * </p>
 * 
 * Depending on the configuration, the default behavior might force the game to exit upon an unexpected exception which
 * can be useful to detect problems in your game early.
 * 
 * @see ClientConfiguration#exitOnError()
 */
public class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
  private static final Logger log = Logger.getLogger(DefaultUncaughtExceptionHandler.class.getName());

  private boolean exitOnException;
  private boolean dumpThreads;

  /**
   * Initializes a new instance of the {@code DefaultUncaughtExceptionHandler} class.
   *
   * @param exitOnException
   *          A flag indicating whether the game should exit when an unexpected exception occurs.
   *          The game will still exit if it encounters an Error.
   */
  public DefaultUncaughtExceptionHandler(boolean exitOnException) {
    this(exitOnException, false);
  }
  
  /**
   * Initializes a new instance of the {@code DefaultUncaughtExceptionHandler} class.
   *
   * @param exitOnException
   *          A flag indicating whether the game should exit when an unexpected exception occurs.
   *          The game will still exit if it encounters an Error
   * @param dumpThreads
   *          A flag indicating whether the crash report should contain an additional thread dump.
   */
  public DefaultUncaughtExceptionHandler(boolean exitOnException, boolean dumpThreads) {
    this.exitOnException = exitOnException;
    this.dumpThreads = dumpThreads;
  }

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    if (e instanceof ThreadDeath)
      return;

    try (PrintStream stream = new PrintStream("crash.txt")) {
      stream.print(new Date() + " ");
      stream.println(t.getName() + " threw an exception:");
      e.printStackTrace(stream);
      if(dumpsThreads()) {
        stream.println();
        stream.println(dump());
      }
    } catch (FileNotFoundException e2) {
      log.log(Level.WARNING, "Could not create crash report file.", e);
    }

    log.log(Level.SEVERE, "Game crashed! :(", e);

    if (this.exitOnException() || e instanceof Error) {
      System.exit(Game.EXIT_GAME_CRASHED);
    }
  }

  /**
   * Indicates whether this hander currently exits the game upon an unhandled exception.
   * 
   * Note that this handler will still exit if it encounters an unhandled Error.
   * 
   * @return True if the game will exit upon an unhandled exception; otherwise false.
   */
  public boolean exitOnException() {
    return this.exitOnException;
  }
  
  /**
   * @return true if the generated crash report will contain a thread dump
   */
  public boolean dumpsThreads() {
    return this.dumpThreads;
  }

  /**
   * Set whether the game will exit upon an unhandled exception.
   * 
   * @param exit
   *          The flag that defines whether the game will exit upon an unhandled exception.
   */
  public void setExitOnException(boolean exit) {
    this.exitOnException = exit;
  }
  
  /**
   * Set whether the generated crash report will contain an additonal thread dump
   * 
   * @param exit
   *          The flag that defines whether the game will exit upon an unhandled exception.
   */
  public void dumpThreads(boolean dumpThreads) {
    this.dumpThreads = dumpThreads;
  }
  
  protected static String dump() {
    StringBuilder text = new StringBuilder();
    ThreadMXBean threads = ManagementFactory.getThreadMXBean();
    ThreadInfo[] dumps = threads.getThreadInfo(threads.getAllThreadIds(), 255);
    text.append("====THREAD DUMP====\n\n");
    for(ThreadInfo dump : dumps) {
      text.append("\"" + dump.getThreadName() + "\"\n");
      Thread.State state = dump.getThreadState();
      text.append("\tState: " + state);
      String blockedBy = dump.getLockOwnerName();
      if(blockedBy != null) {
        text.append(" on " + blockedBy);
      }
      text.append("\n");
      StackTraceElement[] elements = dump.getStackTrace();
      for(StackTraceElement element : elements) {
        text.append("\t\tat ");
        text.append(element);
        text.append("\n");
      }
      text.append("\n\n");
    }
    return text.toString();
  }
  
}
