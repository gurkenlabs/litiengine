package de.gurkenlabs.utiliti.swing;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.github.weisj.darklaf.components.text.NumberedTextComponent;

import de.gurkenlabs.utiliti.Style;

@SuppressWarnings("serial")
public class ConsoleComponent extends NumberedTextComponent {

  public ConsoleComponent() {
    super(new JTextPane());
    this.textComponent.setEditable(false);
    Logger.getLogger("").addHandler(new LogHandler((JTextPane) this.textComponent));
  }

  private class LogHandler extends java.util.logging.Handler {
    private final JTextPane textPane;

    public LogHandler(final JTextPane textPane) {
      this.textPane = textPane;
    }

    @Override
    public void publish(final LogRecord record) {
      StyledDocument doc = textPane.getStyledDocument();
      SimpleAttributeSet keyWord = new SimpleAttributeSet();
      StyleConstants.setForeground(keyWord, getColor(record.getLevel()));
      StyleConstants.setBold(keyWord, true);
      StyleConstants.setFontSize(keyWord, 12);
      StyleConstants.setFontFamily(keyWord, Style.FONTNAME_CONSOLE);

      SimpleAttributeSet text = new SimpleAttributeSet();
      StyleConstants.setForeground(text, getColor(record.getLevel()));
      StyleConstants.setFontFamily(text, Style.FONTNAME_CONSOLE);

      String message = "";
      if (record.getParameters() != null) {
        message = MessageFormat.format(record.getMessage(), record.getParameters());
      } else {
        message = record.getMessage();
      }

      if (record.getLevel() == Level.SEVERE && record.getThrown() != null) {
        StringWriter writer = new StringWriter();
        record.getThrown().printStackTrace(new PrintWriter(writer));
        message = writer.toString();
      }

      try {
        doc.insertString(doc.getLength(), String.format("%1$-10s", record.getLevel()), keyWord);
        doc.insertString(doc.getLength(), message, text);
        doc.insertString(doc.getLength(), "\n", text);
      } catch (BadLocationException e) {
        // if an exception occurs while logging, just ignore it
      }

      textPane.setCaretPosition(doc.getLength());
    }

    @Override
    public void flush() {
      // nothing to flush here -> writing to a control
    }

    @Override
    public void close() {
      // nothing to close here -> writing to a control
    }

    private Color getColor(Level level) {
      if (level == Level.SEVERE) {
        return Color.RED;
      } else if (level == Level.WARNING) {
        return Color.ORANGE;
      }

      return ConsoleComponent.this.getForeground();
    }
  }
}
