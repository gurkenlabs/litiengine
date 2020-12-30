package de.gurkenlabs.utiliti.swing;

import de.gurkenlabs.utiliti.Style;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogHandler extends java.util.logging.Handler {
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
        Rectangle2D bounds = null;
        try {
            bounds = textPane.modelToView(textPane.getCaretPosition());
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
