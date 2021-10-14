package de.gurkenlabs.litiengine;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.configuration.ClientConfiguration;

import static de.gurkenlabs.litiengine.util.io.FileUtilities.humanReadableByteCount;

/**
 * Handles the uncaught exceptions that might occur while running a game or application with the LITIENGINE.
 * <p>
 * It provides proper logging of the exception in a {@code crash.txt} file in the game's root directory that can be
 * further used to report the issue if it's a generic one.
 * </p>
 * <p>
 * Depending on the configuration, the default behavior might force the game to exit upon an unexpected exception which
 * can be useful to detect problems in your game early.
 *
 * @see ClientConfiguration#exitOnError()
 */
public class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
  private static final Logger log = Logger.getLogger(DefaultUncaughtExceptionHandler.class.getName());

  private volatile boolean exitOnException;
  private volatile boolean dumpThreads;

  /**
   * Initializes a new instance of the {@code DefaultUncaughtExceptionHandler} class.
   *
   * @param exitOnException A flag indicating whether the game should exit when an unexpected exception occurs.
   *                        The game will still exit if it encounters an Error.
   */
  public DefaultUncaughtExceptionHandler(boolean exitOnException) {
    this(exitOnException, false);
  }

  /**
   * Initializes a new instance of the {@code DefaultUncaughtExceptionHandler} class.
   *
   * @param exitOnException A flag indicating whether the game should exit when an unexpected exception occurs.
   *                        The game will still exit if it encounters an Error
   * @param dumpThreads     A flag indicating whether the crash report should contain an additional thread dump.
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
      stream.println("\n" + getSystemInfo());
      if (dumpsThreads()) {
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
   * <p>
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
   * @param exit The flag that defines whether the game will exit upon an unhandled exception.
   */
  public void setExitOnException(boolean exit) {
    this.exitOnException = exit;
  }

  /**
   * Set whether the generated crash report will contain an additional thread dump
   *
   * @param dumpThreads The flag that defines whether crash report will contain a thread dump.
   */
  public void dumpThreads(boolean dumpThreads) {
    this.dumpThreads = dumpThreads;
  }

  protected static String dump() {
    StringBuilder text = new StringBuilder();
    ThreadMXBean threads = ManagementFactory.getThreadMXBean();
    ThreadInfo[] dumps = threads.getThreadInfo(threads.getAllThreadIds(), 255);
    text.append("====THREAD DUMP====\n\n");
    for (ThreadInfo dump : dumps) {
      text.append("\"").append(dump.getThreadName()).append("\"\n");
      Thread.State state = dump.getThreadState();
      text.append("\tState: ").append(state);
      String blockedBy = dump.getLockOwnerName();
      if (blockedBy != null) {
        text.append(" on ").append(blockedBy);
      }
      text.append("\n");
      StackTraceElement[] elements = dump.getStackTrace();
      for (StackTraceElement element : elements) {
        text.append("\t\tat ");
        text.append(element);
        text.append("\n");
      }
      text.append("\n\n");
    }
    return text.toString();
  }

  protected static String getSystemInfo() {
    StringBuilder text = new StringBuilder();
    text.append("====Runtime Information====\n");
    text.append("Operating System: ").append(System.getProperty("os.name")).append("\n");
    text.append("\tArchitecture: ").append(System.getProperty("os.arch")).append("\n");
    text.append("\tVersion: ").append(System.getProperty("os.version")).append("\n");
    text.append("Memory:\n");
    long heapSize = Runtime.getRuntime().totalMemory();
    long maxHeapSize = Runtime.getRuntime().maxMemory();
    long freeHeapSize = Runtime.getRuntime().freeMemory();
    text.append("\tMax heap size: ").append(humanReadableByteCount(maxHeapSize)).append("\n");
    text.append("\tCurrent heap size: ").append(humanReadableByteCount(heapSize)).append("\n");
    text.append("\tHeap used: ").append(humanReadableByteCount(heapSize - freeHeapSize)).append("\n");
    text.append("\tFree heap: ").append(humanReadableByteCount(freeHeapSize)).append("\n");
    text.append("Java Version: ").append(System.getProperty("java.runtime.name")).append(" ").append(System.getProperty("java.runtime.version"))
        .append(" \n");
    text.append("\tVendor: ").append(System.getProperty("java.vm.vendor")).append("\n");
    text.append("Uptime: ").append(Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime())).append("\n");
    GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] screens = g.getScreenDevices();
    text.append("Screens: ").append(screens.length).append("\n");
    for (int i = 0; i < screens.length; i++) {
      GraphicsDevice screen = screens[i];
      DisplayMode displayMode = screen.getDisplayMode();
      text.append("\tScreen ").append(i).append(": ").append(displayMode.getWidth()).append("x").append(displayMode.getHeight());
      if (displayMode.getRefreshRate() != DisplayMode.REFRESH_RATE_UNKNOWN) {
        text.append("@").append(displayMode.getRefreshRate()).append("hz");
      }
      text.append("\n");
    }
    return text.toString();
  }

}
