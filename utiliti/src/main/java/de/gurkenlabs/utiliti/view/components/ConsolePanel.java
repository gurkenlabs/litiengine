package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.components.text.NumberedTextComponent;
import de.gurkenlabs.utiliti.controller.LogHandler;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

public class ConsolePanel extends NumberedTextComponent {
  private final LogHandler logHandler;

  public ConsolePanel() {
    super(new ConsoleTextPane());
    this.textComponent.setEditable(false);
    applyBackground();
    this.logHandler = new LogHandler((JTextPane) this.textComponent);
    Logger.getLogger("").addHandler(this.logHandler);
  }

  public LogHandler getLogHandler() {
    return this.logHandler;
  }

  @Override
  public void updateUI() {
    super.updateUI();
    if (this.textComponent != null) {
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
          if (SwingUtilities.isLeftMouseButton(e)) {
            int pos = viewToModel2D(e.getPoint());
            if (pos >= 0) {
              StyledDocument doc = getStyledDocument();
              Element elem = doc.getCharacterElement(pos);
              AttributeSet attr = elem.getAttributes();
              Object linkObj = attr.getAttribute("LINK_FILE_PATH");
              if (linkObj instanceof String pathStr) {
                try {
                  Path p;
                  if (pathStr.startsWith("file:///")) {
                    p = Path.of(URI.create(pathStr));
                  } else {
                    p = Path.of(pathStr);
                  }
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
            if (attr.getAttribute("LINK_FILE_PATH") != null) {
              setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              return;
            }
          }
          setCursor(Cursor.getDefaultCursor());
        }
      });
    }

    @Override
    public void updateUI() {
      super.updateUI();
      setBackground(Style.assetExplorerBackground());
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      graphics.setColor(Style.assetExplorerBackground());
      graphics.fillRect(0, 0, getWidth(), getHeight());
      super.paintComponent(graphics);
    }
  }
}
