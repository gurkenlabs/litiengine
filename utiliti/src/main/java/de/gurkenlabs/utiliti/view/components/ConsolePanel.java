package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.components.text.NumberedTextComponent;
import de.gurkenlabs.utiliti.controller.LogHandler;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.util.logging.Logger;
import javax.swing.JTextPane;

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
