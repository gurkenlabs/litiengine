package de.gurkenlabs.litiengine;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
  private static final Logger log = Logger.getLogger(DefaultUncaughtExceptionHandler.class.getName());

  private boolean exitOnException;

  public DefaultUncaughtExceptionHandler() {
    this(true);
  }

  public DefaultUncaughtExceptionHandler(boolean exitOnException) {
    this.exitOnException = exitOnException;
  }

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    if (e instanceof ThreadDeath)
      return;

    try (PrintStream stream = new PrintStream("crash.txt")) {
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

  public boolean exitOnException() {
    return this.exitOnException;
  }

  public void setExitOnException(boolean exit) {
    this.exitOnException = exit;
  }
}
