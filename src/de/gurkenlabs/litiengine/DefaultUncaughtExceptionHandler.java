package de.gurkenlabs.litiengine;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
  private static final Logger log = Logger.getLogger(DefaultUncaughtExceptionHandler.class.getName());

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    if (e instanceof ThreadDeath)
      return;
    
    try {
      log.addHandler(new FileHandler("crash.txt"));
    } catch (SecurityException | IOException e2) {
      log.log(Level.WARNING, "Could not create crash report file.", e);
    }
    
    log.log(Level.SEVERE, String.format("Game crashed! :(%n%s threw an exception:%n", t.getName()), e);
    System.exit(Game.EXIT_GAME_CRASHED);
  }
}
