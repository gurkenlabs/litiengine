package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class LogHandler extends java.util.logging.Handler {
  final JTextPane textPane;

  public LogHandler(final JTextPane textPane) {
    this.textPane = textPane;
  }

  @Override
  public void publish(final LogRecord rec) {
    StyledDocument doc = textPane.getStyledDocument();
    SimpleAttributeSet keyWord = new SimpleAttributeSet();
    StyleConstants.setForeground(keyWord, getColor(rec.getLevel()));
    StyleConstants.setBold(keyWord, true);
    StyleConstants.setFontSize(keyWord, 12);
    StyleConstants.setFontFamily(keyWord, Style.FONTNAME_CONSOLE);

    SimpleAttributeSet text = new SimpleAttributeSet();
    StyleConstants.setForeground(text, getColor(rec.getLevel()));
    StyleConstants.setFontFamily(text, Style.FONTNAME_CONSOLE);

    String message;
    if (rec.getParameters() != null) {
      message = MessageFormat.format(rec.getMessage(), rec.getParameters());
    } else {
      message = rec.getMessage();
    }

    if (rec.getLevel() == Level.SEVERE && rec.getThrown() != null) {
      StringWriter writer = new StringWriter();
      rec.getThrown().printStackTrace(new PrintWriter(writer));
      message = writer.toString();
    }

    try {
      doc.insertString(doc.getLength(), String.format("%1$-10s", rec.getLevel()), keyWord);
      doc.insertString(doc.getLength(), message, text);
      doc.insertString(doc.getLength(), "\n", text);
    } catch (BadLocationException e) {
      // if an exception occurs while logging, just ignore it
    }

    textPane.setCaretPosition(doc.getLength());
  }

  @Override
  public void flush() {
    StyledDocument doc = textPane.getStyledDocument();
    try {
      doc.remove(0, doc.getLength());
    } catch (BadLocationException e) {
      // if an exception occurs while logging, just ignore it
    }

    textPane.setCaretPosition(doc.getLength());
  }

  @Override
  public void close() {
    // nothing to close here -> writing to a control
  }

  public void scrollToLast() {
    StyledDocument doc = textPane.getStyledDocument();
    Rectangle2D bounds;
    try {
      bounds = textPane.modelToView2D(textPane.getCaretPosition());
      textPane.scrollRectToVisible(bounds.getBounds());
    } catch (BadLocationException e) {
      // if an exception occurs while logging, just ignore it
    }
    textPane.setCaretPosition(doc.getLength());
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
