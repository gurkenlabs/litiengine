package de.gurkenlabs.utiliti.controller;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages application log verbosity, configures JDK logging levels, and formats
 * user-friendly log messages for utiLITI and MCP components.
 */
public final class LoggingManager {
  private static final Logger log = Logger.getLogger(LoggingManager.class.getName());
  private static final Pattern CLIENT_INFO_PATTERN = Pattern.compile(
      "Protocol:\\s*([^,\\s]+).*?Implementation\\[name=([^,\\s\\]]+).*?version=([^,\\s\\]]+)");

  private static volatile Level currentLevel = Level.INFO;

  private LoggingManager() {}

  /**
   * Applies the specified log level to JUL loggers and handler thresholds.
   *
   * @param levelName the log level name (INFO, FINE, WARNING, SEVERE, OFF, ALL)
   */
  public static synchronized void applyLogLevel(String levelName) {
    Level targetLevel = parseLogLevel(levelName);
    currentLevel = targetLevel;

    Logger rootLogger = Logger.getLogger("");
    rootLogger.setLevel(targetLevel);

    for (Handler handler : rootLogger.getHandlers()) {
      handler.setLevel(targetLevel);
    }

    // Configure Tomcat web server loggers:
    // Suppress container startup noise at INFO level; allow when verbosity is FINE or higher.
    Level tomcatLevel = targetLevel.intValue() <= Level.FINE.intValue() ? targetLevel : Level.WARNING;
    Logger.getLogger("org.apache.catalina").setLevel(tomcatLevel);
    Logger.getLogger("org.apache.coyote").setLevel(tomcatLevel);
    Logger.getLogger("org.apache.tomcat").setLevel(tomcatLevel);

    // Configure MCP Java SDK loggers
    Logger.getLogger("io.modelcontextprotocol").setLevel(targetLevel);

    // Configure utiLITI application & engine loggers
    Logger.getLogger("de.gurkenlabs.utiliti").setLevel(targetLevel);
    Logger.getLogger("de.gurkenlabs.litiengine").setLevel(targetLevel);

    log.log(Level.FINE, "Applied logging verbosity level: {0}", targetLevel.getName());
  }

  public static Level getCurrentLevel() {
    return currentLevel;
  }

  public static Level parseLogLevel(String levelName) {
    if (levelName == null || levelName.isBlank()) {
      return Level.INFO;
    }
    String normalized = levelName.trim().toUpperCase(java.util.Locale.ROOT);
    return switch (normalized) {
      case "OFF" -> Level.OFF;
      case "SEVERE", "ERROR" -> Level.SEVERE;
      case "WARNING", "WARN" -> Level.WARNING;
      case "INFO" -> Level.INFO;
      case "FINE", "DEBUG" -> Level.FINE;
      case "FINER", "TRACE" -> Level.FINER;
      case "FINEST" -> Level.FINEST;
      case "ALL" -> Level.ALL;
      default -> Level.INFO;
    };
  }

  /**
   * Checks if a log record is Tomcat internal container startup noise.
   */
  public static boolean isTomcatBoilerplate(String loggerName, String message) {
    if (message == null) {
      return false;
    }
    boolean isTomcatLogger = loggerName != null
        && (loggerName.startsWith("org.apache.catalina")
            || loggerName.startsWith("org.apache.coyote")
            || loggerName.startsWith("org.apache.tomcat"));
    if (isTomcatLogger) {
      return message.contains("Initializing ProtocolHandler")
          || message.contains("Starting service")
          || message.contains("Starting Servlet engine:")
          || message.contains("Starting ProtocolHandler")
          || message.contains("Command line argument:")
          || message.contains("Apache Tomcat Native library");
    }
    return false;
  }

  /**
   * Formats raw technical MCP client initialize request strings into clean, user-friendly logs.
   */
  public static String formatClientInitializeRequest(String rawMessage) {
    if (rawMessage == null) {
      return rawMessage;
    }
    Matcher matcher = CLIENT_INFO_PATTERN.matcher(rawMessage);
    if (matcher.find()) {
      String protocol = matcher.group(1);
      String clientName = matcher.group(2);
      String clientVersion = matcher.group(3);
      try {
        de.gurkenlabs.utiliti.mcp.McpServer.instance().registerClient(clientName, clientVersion);
      } catch (Throwable e) {
        log.log(Level.FINE, "Could not auto-register client from log", e);
      }
      return "MCP Client connected: " + clientName + " (v" + clientVersion + ", protocol " + protocol + ")";
    }
    return "MCP Client connected";
  }
}
