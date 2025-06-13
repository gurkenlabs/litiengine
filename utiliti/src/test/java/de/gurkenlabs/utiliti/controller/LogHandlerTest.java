package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    assertEquals(55, styledDocument.getLength());
    assertEquals(55, textPane.getCaretPosition());
  }

  @Test
  void flush() {
    JTextPane textPane = new JTextPane();
    LogHandler logHandler = new LogHandler(textPane);

    logHandler.publish(new LogRecord(Level.INFO, "Hello World"));
    logHandler.publish(new LogRecord(Level.INFO, "This is a test"));

    StyledDocument styledDocument = textPane.getStyledDocument();

    assertEquals(47, styledDocument.getLength());
    assertEquals(47, textPane.getCaretPosition());

    logHandler.flush();

    assertEquals(0, styledDocument.getLength());
    assertEquals(0, textPane.getCaretPosition());
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

    assertEquals(47, styledDocument.getLength());
    assertEquals(0, textPane.getCaretPosition());

    try {
      System.out.println("TextPane = " + logHandler.textPane);
      System.out.println("doc = " + logHandler.textPane.getStyledDocument());
      logHandler.scrollToLast();
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }

    assertEquals(47, styledDocument.getLength());
    assertEquals(47, textPane.getCaretPosition());
  }
}
