package de.gurkenlabs.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Logger;

public class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
  private static final Logger log = Logger.getLogger(DefaultUncaughtExceptionHandler.class.getName());

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    String stacktrace = sw.toString();
    log.severe(stacktrace);
  }
}
