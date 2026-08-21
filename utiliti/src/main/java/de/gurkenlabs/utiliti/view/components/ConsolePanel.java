package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.controller.LogHandler;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

public class ConsolePanel extends JPanel {
  private final JTextPane textPane;
  private final LogHandler logHandler;
  private final JScrollPane scrollPane;

  public ConsolePanel() {
    super(new BorderLayout());
    this.textPane = new ConsoleTextPane();
    this.textPane.setEditable(false);
    this.textPane.setOpaque(false);
    this.textPane.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    this.textPane.setBackground(Style.assetExplorerBackground());

    this.scrollPane = new JScrollPane(this.textPane);
    this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
    this.scrollPane.setOpaque(false);
    this.scrollPane.getViewport().setOpaque(false);
    this.scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    this.scrollPane.getViewport().setBackground(Style.assetExplorerBackground());
    this.add(this.scrollPane, BorderLayout.CENTER);

    this.logHandler = new LogHandler(this.textPane);
    Logger.getLogger("").addHandler(this.logHandler);

    applyBackground();
  }

  public LogHandler getLogHandler() {
    return this.logHandler;
  }

  public JTextPane getTextPane() {
    return this.textPane;
  }

  public JScrollPane getScrollPane() {
    return this.scrollPane;
  }

  @Override
  public void updateUI() {
    super.updateUI();
    if (this.textPane != null) {
      applyBackground();
    }
  }

  private void applyBackground() {
    Color background = Style.assetExplorerBackground();
    applyBackground(this, background);
  }

  private static void applyBackground(Component component, Color background) {
    component.setBackground(background);
    if (component instanceof Container container) {
      for (Component child : container.getComponents()) {
        applyBackground(child, background);
      }
    }
  }

  private static final class ConsoleTextPane extends JTextPane {
    public ConsoleTextPane() {
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
          }
          int pos = viewToModel2D(e.getPoint());
          if (pos < 0) {
            return;
          }
          StyledDocument doc = getStyledDocument();
          Element elem = doc.getCharacterElement(pos);
          AttributeSet attr = elem.getAttributes();

          if (e.getClickCount() == 2) {
            Object logTextObj = attr.getAttribute("LOG_ENTRY_TEXT");
            String textToCopy = null;
            if (logTextObj instanceof String s && !s.isBlank()) {
              textToCopy = s;
            } else {
              Element paragraph = doc.getParagraphElement(pos);
              if (paragraph != null) {
                try {
                  int start = paragraph.getStartOffset();
                  int len = paragraph.getEndOffset() - start;
                  textToCopy = doc.getText(start, len).trim();
                } catch (BadLocationException ignored) {
                }
              }
            }
            if (textToCopy != null && !textToCopy.isBlank()) {
              Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textToCopy), null);
            }
            return;
          }

          if (e.getClickCount() == 1) {
            Object linkObj = attr.getAttribute("LINK_FILE_PATH");
            if (linkObj instanceof String pathStr) {
              try {
                Path p = pathStr.startsWith("file:///") ? Path.of(URI.create(pathStr)) : Path.of(pathStr);
                if (Files.exists(p)) {
                  if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(p.toFile());
                  }
                } else if (p.getParent() != null && Files.exists(p.getParent())) {
                  if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(p.getParent().toFile());
                  }
                }
              } catch (Exception ignored) {
              }
            }
          }
        }
      });

      addMouseMotionListener(new MouseAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
          int pos = viewToModel2D(e.getPoint());
          if (pos >= 0) {
            StyledDocument doc = getStyledDocument();
            Element elem = doc.getCharacterElement(pos);
            AttributeSet attr = elem.getAttributes();
            Object linkObj = attr.getAttribute("LINK_FILE_PATH");
            Object isBadge = attr.getAttribute("IS_LEVEL_BADGE");
            if (linkObj instanceof String || isBadge != null) {
              setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              if (isBadge != null) {
                setToolTipText("Double-click to copy text");
              } else {
                setToolTipText(null);
              }
              return;
            }
          }
          setToolTipText(null);
          setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        }
      });
    }
  }
}
