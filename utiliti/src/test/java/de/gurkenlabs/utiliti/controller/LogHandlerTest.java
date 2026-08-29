package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class LogHandlerTest {

  @BeforeEach
  public void assertOnSwingThread() {
    assertTrue(SwingUtilities.isEventDispatchThread());
  }

  @Test
  void publish() {
    JTextPane textPane = new JTextPane();
    LogHandler logHandler = new LogHandler(textPane);

    StyledDocument styledDocument = textPane.getStyledDocument();

    assertEquals(0, styledDocument.getLength());
    assertEquals(0, textPane.getCaretPosition());

    logHandler.publish(new LogRecord(Level.INFO, "Hello World"));
    logHandler.publish(new LogRecord(Level.SEVERE, "This is a severe test!"));

    assertTrue(styledDocument.getLength() > 0);
    assertEquals(styledDocument.getLength(), textPane.getCaretPosition());
  }

  @Test
  void flush() {
    JTextPane textPane = new JTextPane();
    LogHandler logHandler = new LogHandler(textPane);

    logHandler.publish(new LogRecord(Level.INFO, "Hello World"));
    logHandler.publish(new LogRecord(Level.INFO, "This is a test"));

    StyledDocument styledDocument = textPane.getStyledDocument();

    assertTrue(styledDocument.getLength() > 0);
    assertEquals(styledDocument.getLength(), textPane.getCaretPosition());

    logHandler.flush();

    assertEquals(0, styledDocument.getLength());
    assertEquals(0, textPane.getCaretPosition());
    assertEquals(0, logHandler.getWarningCount());
    assertEquals(0, logHandler.getErrorCount());
    assertNull(logHandler.getLatestErrorStack());
    assertTrue(logHandler.getRecentLogs().isEmpty());
  }

  @Test
  void tracksWarningsAndErrors() {
    LogHandler logHandler = new LogHandler(new JTextPane());
    int[] changes = {0};
    logHandler.addChangeListener(() -> changes[0]++);

    logHandler.publish(new LogRecord(Level.WARNING, "Warning"));
    LogRecord error = new LogRecord(Level.SEVERE, "Error");
    error.setThrown(new IllegalStateException("Broken"));
    logHandler.publish(error);

    assertEquals(1, logHandler.getWarningCount());
    assertEquals(1, logHandler.getErrorCount());
    assertTrue(logHandler.getLatestErrorStack().contains("IllegalStateException: Broken"));
    assertEquals(2, changes[0]);

    logHandler.flush();

    assertEquals(0, logHandler.getWarningCount());
    assertEquals(0, logHandler.getErrorCount());
    assertNull(logHandler.getLatestErrorStack());
    assertEquals(3, changes[0]);
  }

  @Test
  void ignoresExpectedMcpDisconnectAndCancellationNoise() {
    JTextPane textPane = new JTextPane();
    LogHandler logHandler = new LogHandler(textPane);
    String[] expectedNoise = {
      "No handler registered for notification method: notifications/cancelled",
      "Failed to send message to session 1: Client disconnected",
      "Failed to send keep-alive ping to session 1: Stream unavailable",
      "Failed to complete async context for session 1: Calling [asyncComplete()] is not valid for a request with Async state [COMPLETING]"
    };

    for (String message : expectedNoise) {
      LogRecord record = new LogRecord(Level.WARNING, message);
      record.setLoggerName(
          "io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider");
      logHandler.publish(record);
    }

    assertEquals(0, textPane.getDocument().getLength());
    assertEquals(0, logHandler.getWarningCount());
    assertTrue(logHandler.getRecentLogs().isEmpty());

    LogRecord actionableWarning = new LogRecord(Level.WARNING, "Tool input validation failed");
    actionableWarning.setLoggerName("io.modelcontextprotocol.server.McpAsyncServer");
    logHandler.publish(actionableWarning);
    assertEquals(1, logHandler.getWarningCount());
  }

  @Test
  void filtersTomcatBoilerplateAtInfoLevel() {
    JTextPane textPane = new JTextPane();
    LogHandler logHandler = new LogHandler(textPane);

    LogRecord tomcatRecord = new LogRecord(Level.INFO, "Initializing ProtocolHandler [\"http-nio-127.0.0.1-8080\"]");
    tomcatRecord.setLoggerName("org.apache.coyote.http11.Http11NioProtocol");
    logHandler.publish(tomcatRecord);

    assertEquals(0, textPane.getDocument().getLength());
    assertTrue(logHandler.getRecentLogs().isEmpty());
  }

  @Test
  void formatsMcpClientInitializeRequest() {
    JTextPane textPane = new JTextPane();
    LogHandler logHandler = new LogHandler(textPane);

    LogRecord clientRecord = new LogRecord(
        Level.INFO,
        "Client initialize request - Protocol: 2025-11-25, Capabilities: ClientCapabilities[experimental=null, roots=RootCapabilities[listChanged=null]], Info: Implementation[name=opencode, title=null, version=local]");
    clientRecord.setLoggerName("io.modelcontextprotocol.server.McpServerSessionHandler");
    logHandler.publish(clientRecord);

    assertEquals(1, logHandler.getRecentLogs().size());
    String formatted = logHandler.getRecentLogs().get(0).message();
    assertEquals("MCP Client connected: opencode (vlocal, protocol 2025-11-25)", formatted);
  }

  @Test
  void scrollToLast() {
    JTextPane textPane = new JTextPane();
    textPane.setBounds(5, 5, 200, 10);
    LogHandler logHandler = new LogHandler(textPane);

    logHandler.publish(new LogRecord(Level.INFO, "Hello World"));
    logHandler.publish(new LogRecord(Level.INFO, "This is a test"));

    StyledDocument styledDocument = textPane.getStyledDocument();
    textPane.setCaretPosition(0);

    int length = styledDocument.getLength();
    assertTrue(length > 0);
    assertEquals(0, textPane.getCaretPosition());

    logHandler.scrollToLast();

    assertEquals(length, styledDocument.getLength());
    assertEquals(length, textPane.getCaretPosition());
  }
}
