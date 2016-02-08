package de.gurkenlabs.core;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Logger;

public class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler{
  private static final Logger log = Logger.getLogger(DefaultUncaughtExceptionHandler.class.getName());
  
  @Override
  public void uncaughtException(Thread t, Throwable e) {
    log.severe(e.getMessage());
  }
}
