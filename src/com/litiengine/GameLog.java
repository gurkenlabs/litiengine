package com.litiengine;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * The {@code GameLog} class provides a general purpose logger for games.
 * It prevents the necessity to create custom logger instances and provides a simplified way to quickly log events.
 *
 * @see Game#log()
 */
final class GameLog {
  private static final String LOGGING_CONFIG_FILE = "logging.properties";
  private static final Logger log = Logger.getLogger(GameLog.class.getName());

  GameLog() {
  }

  Logger log() {
    return log;
  }

  void init() {
    LogManager.getLogManager().reset();
    if (new File(LOGGING_CONFIG_FILE).exists()) {
      System.setProperty("java.util.logging.config.file", LOGGING_CONFIG_FILE);

      try {
        LogManager.getLogManager().readConfiguration();
      } catch (final Exception e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    } else {
      try {
        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleFormatter());

        final FileHandler fileHandler = new FileHandler("game.log", 50000, 1, true);
        fileHandler.setLevel(Level.WARNING);
        fileHandler.setFormatter(new SimpleFormatter());

        final Logger logger = Logger.getLogger("");
        logger.addHandler(consoleHandler);
        logger.addHandler(fileHandler);
      } catch (final Exception e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }
}
