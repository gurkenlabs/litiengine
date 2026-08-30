package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class LogHandler extends java.util.logging.Handler {
  public record LogEntry(String level, String message, long timestamp) {}
  private final List<LogEntry> recentLogs = new CopyOnWriteArrayList<>();
  private static final int MAX_RECENT_LOGS = 100;

  final JTextPane textPane;
  private final AtomicInteger warningCount = new AtomicInteger();
  private final AtomicInteger errorCount = new AtomicInteger();
  private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();
  private volatile String latestErrorStack;
  private static final Pattern PATH_PATTERN = Pattern.compile("(?:file:///[^\\s\\n\\r\"]+|[A-Za-z]:\\\\[^\\s\\n\\r\"]+|screenshots\\\\[^\\s\\n\\r\"]+|screenshots/[^\\s\\n\\r\"]+)");

  public LogHandler(final JTextPane textPane) {
    this.textPane = textPane;
  }

  public List<LogEntry> getRecentLogs() {
    return new ArrayList<>(recentLogs);
  }

  @Override
  public void publish(final LogRecord rec) {
    if (rec == null || isExpectedMcpTransportNoise(rec)) {
      return;
    }

    Level configuredThreshold = LoggingManager.parseLogLevel(Editor.preferences().getLogLevel());
    if (rec.getLevel().intValue() < configuredThreshold.intValue()) {
      return;
    }

    if (configuredThreshold.intValue() > Level.FINE.intValue()
        && LoggingManager.isTomcatBoilerplate(rec.getLoggerName(), rec.getMessage())) {
      return;
    }

    StyledDocument doc = textPane.getStyledDocument();

    SimpleAttributeSet timestampStyle = new SimpleAttributeSet();
    StyleConstants.setForeground(timestampStyle, new Color(130, 140, 160));
    StyleConstants.setFontSize(timestampStyle, 11);
    StyleConstants.setFontFamily(timestampStyle, Style.FONTNAME_CONSOLE);

    SimpleAttributeSet badgeStyle = new SimpleAttributeSet();
    StyleConstants.setIcon(badgeStyle, new de.gurkenlabs.utiliti.view.components.LevelBadgeIcon(rec.getLevel()));

    SimpleAttributeSet spaceStyle = new SimpleAttributeSet();
    StyleConstants.setFontSize(spaceStyle, 11);
    StyleConstants.setFontFamily(spaceStyle, Style.FONTNAME_CONSOLE);

    SimpleAttributeSet text = new SimpleAttributeSet();
    StyleConstants.setForeground(text, Style.text());
    StyleConstants.setFontSize(text, 11);
    StyleConstants.setFontFamily(text, Style.FONTNAME_CONSOLE);

    SimpleAttributeSet linkStyle = new SimpleAttributeSet();
    StyleConstants.setForeground(linkStyle, new Color(80, 170, 255));
    StyleConstants.setUnderline(linkStyle, true);
    StyleConstants.setFontSize(linkStyle, 11);
    StyleConstants.setFontFamily(linkStyle, Style.FONTNAME_CONSOLE);

    String message;
    if (rec.getParameters() != null && rec.getParameters().length > 0) {
      try {
        message = MessageFormat.format(rec.getMessage(), rec.getParameters());
      } catch (Exception e) {
        message = rec.getMessage();
      }
    } else {
      message = rec.getMessage();
    }

    String errorStack = null;
    if (rec.getLevel().intValue() >= Level.SEVERE.intValue() && rec.getThrown() != null) {
      StringWriter writer = new StringWriter();
      rec.getThrown().printStackTrace(new PrintWriter(writer));
      errorStack = writer.toString();
      message = errorStack;
    }

    String cleanMessage = message != null ? message.replaceAll("\\u001B\\[[;\\d]*[a-zA-Z]|\\u001B\\]8;;.*?\\u001B\\\\", "") : "";

    if (configuredThreshold.intValue() > Level.FINE.intValue()
        && cleanMessage.contains("Client initialize request - Protocol:")) {
      cleanMessage = LoggingManager.formatClientInitializeRequest(cleanMessage);
    }
    final String formattedMessage = cleanMessage;

    recentLogs.add(new LogEntry(rec.getLevel().getName(), formattedMessage, System.currentTimeMillis()));
    if (recentLogs.size() > MAX_RECENT_LOGS) {
      recentLogs.remove(0);
    }

    java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm:ss");
    String timeStr = timeFormat.format(new java.util.Date(rec.getMillis())) + "  ";

    Runnable insertTask = () -> {
      try {
        SimpleAttributeSet rowBadgeStyle = new SimpleAttributeSet(badgeStyle);
        rowBadgeStyle.addAttribute("IS_LEVEL_BADGE", Boolean.TRUE);
        rowBadgeStyle.addAttribute("LOG_ENTRY_TEXT", formattedMessage);

        doc.insertString(doc.getLength(), timeStr, timestampStyle);
        doc.insertString(doc.getLength(), " ", rowBadgeStyle);
        doc.insertString(doc.getLength(), "  ", spaceStyle);

        Matcher matcher = PATH_PATTERN.matcher(formattedMessage);
        int lastEnd = 0;
        while (matcher.find()) {
          if (matcher.start() > lastEnd) {
            doc.insertString(doc.getLength(), formattedMessage.substring(lastEnd, matcher.start()), text);
          }
          String matchedPath = matcher.group();
          SimpleAttributeSet matchLinkStyle = new SimpleAttributeSet(linkStyle);
          matchLinkStyle.addAttribute("LINK_FILE_PATH", matchedPath);
          doc.insertString(doc.getLength(), matchedPath, matchLinkStyle);
          lastEnd = matcher.end();
        }
        if (lastEnd < formattedMessage.length()) {
          doc.insertString(doc.getLength(), formattedMessage.substring(lastEnd), text);
        }
        doc.insertString(doc.getLength(), "\n", text);
      } catch (BadLocationException e) {
        // if an exception occurs while logging, just ignore it
      }

      textPane.setCaretPosition(doc.getLength());
    };

    if (SwingUtilities.isEventDispatchThread()) {
      insertTask.run();
    } else {
      SwingUtilities.invokeLater(insertTask);
    }

    if (rec.getLevel().intValue() >= Level.SEVERE.intValue()) {
      this.errorCount.incrementAndGet();
      if (errorStack != null) {
        this.latestErrorStack = errorStack;
      }
    } else if (rec.getLevel().intValue() >= Level.WARNING.intValue()) {
      this.warningCount.incrementAndGet();
    } else {
      return;
    }
    notifyChangeListeners();
  }

  @Override
  public void flush() {
    Runnable flushTask = () -> {
      StyledDocument doc = textPane.getStyledDocument();
      try {
        doc.remove(0, doc.getLength());
      } catch (BadLocationException e) {
        // if an exception occurs while logging, just ignore it
      }

      textPane.setCaretPosition(doc.getLength());
    };

    if (SwingUtilities.isEventDispatchThread()) {
      flushTask.run();
    } else {
      SwingUtilities.invokeLater(flushTask);
    }

    this.recentLogs.clear();
    this.warningCount.set(0);
    this.errorCount.set(0);
    this.latestErrorStack = null;
    notifyChangeListeners();
  }

  @Override
  public void close() {
    // nothing to close here -> writing to a control
  }

  public void scrollToLast() {
    Runnable scrollTask = () -> {
      StyledDocument doc = textPane.getStyledDocument();
      Rectangle2D bounds;
      try {
        bounds = textPane.modelToView2D(textPane.getCaretPosition());
        if (bounds != null) {
          textPane.scrollRectToVisible(bounds.getBounds());
        }
      } catch (BadLocationException e) {
        // if an exception occurs while logging, just ignore it
      }
      textPane.setCaretPosition(doc.getLength());
    };

    if (SwingUtilities.isEventDispatchThread()) {
      scrollTask.run();
    } else {
      SwingUtilities.invokeLater(scrollTask);
    }
  }

  public int getWarningCount() {
    return this.warningCount.get();
  }

  public int getErrorCount() {
    return this.errorCount.get();
  }

  public String getLatestErrorStack() {
    return this.latestErrorStack;
  }

  public void addChangeListener(Runnable listener) {
    this.changeListeners.add(listener);
  }

  private static boolean isExpectedMcpTransportNoise(LogRecord record) {
    String loggerName = record.getLoggerName();
    String message = record.getMessage();
    if (loggerName == null
        || !loggerName.startsWith("io.modelcontextprotocol.")
        || message == null) {
      return false;
    }

    boolean unsupportedCancellation =
        message.contains("No handler registered for notification method:")
            && message.contains("notifications/cancelled");
    boolean disconnectedStream =
        message.contains("Failed to send message to session")
            && (message.contains("Client disconnected")
                || message.contains("Stream closed")
                || message.contains("Stream unavailable"));
    boolean staleKeepAlive =
        message.contains("Failed to send keep-alive ping to session")
            && (message.contains("Stream closed")
                || message.contains("Stream unavailable"));
    boolean completedAsyncRequest =
        message.contains("Failed to complete async context for session")
            && message.contains("asyncComplete()")
            && message.contains("COMPLETING");
    return unsupportedCancellation
        || disconnectedStream
        || staleKeepAlive
        || completedAsyncRequest;
  }

  private void notifyChangeListeners() {
    this.changeListeners.forEach(Runnable::run);
  }

  private Color getColor(Level level) {
    if (level == Level.SEVERE) {
      return Color.RED;
    } else if (level == Level.WARNING) {
      return Color.ORANGE;
    }

    return this.textPane.getForeground();
  }
}
