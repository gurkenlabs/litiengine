package de.gurkenlabs.litiengine;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.configuration.ClientConfiguration;

/**
 * Handles the uncaught exceptions that might occur while running a game or application with the LITIengine.
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

  /**
   * Initializes a new instance of the {@code DefaultUncaughtExceptionHandler} class.
   *
   * @param exitOnException
   *          A flag indicating whether the game should exit when an unexpected error occurs.
   */
  public DefaultUncaughtExceptionHandler(boolean exitOnException) {
    this.exitOnException = exitOnException;
  }

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    if (e instanceof ThreadDeath)
      return;

    try (PrintStream stream = new PrintStream("crash.txt")) {
      stream.print(new Date() + " ");
      stream.println(t.getName() + " threw an exception:");
      e.printStackTrace(stream);
    } catch (FileNotFoundException e2) {
      log.log(Level.WARNING, "Could not create crash report file.", e);
    }

    log.log(Level.SEVERE, "Game crashed! :(", e);

    if (this.exitOnException()) {
      System.exit(Game.EXIT_GAME_CRASHED);
    }
  }

  /**
   * Indicates whether this hander currently exits the game upon an unhandled exception.
   * 
   * @return True if the game will exit upon an unhandled exception; otherwise false.
   */
  public boolean exitOnException() {
    return this.exitOnException;
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
}
