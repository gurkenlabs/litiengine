package de.gurkenlabs.utiliti.view.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Locale;
import java.util.logging.Level;
import javax.swing.Icon;

/**
 * Renders a compact pill badge for a logging level (e.g. [INFO], [WARN], [ERROR]).
 */
public final class LevelBadgeIcon implements Icon {
  private final String text;
  private final Color bgColor;
  private final Color fgColor;
  private final int width;
  private final int height;
  private final Font font;

  public LevelBadgeIcon(Level level) {
    String name = level == null ? "INFO" : level.getName().toUpperCase(Locale.ROOT);
    switch (name) {
      case "SEVERE", "ERROR", "FATAL" -> {
        this.text = "ERROR";
        this.bgColor = new Color(58, 22, 22);
        this.fgColor = new Color(245, 95, 95);
      }
      case "WARNING", "WARN" -> {
        this.text = "WARN";
        this.bgColor = new Color(56, 42, 16);
        this.fgColor = new Color(240, 180, 50);
      }
      case "CONFIG", "FINE", "FINER", "FINEST", "DEBUG" -> {
        this.text = "DEBUG";
        this.bgColor = new Color(20, 42, 56);
        this.fgColor = new Color(70, 180, 240);
      }
      default -> {
        this.text = "INFO";
        this.bgColor = new Color(24, 48, 34);
        this.fgColor = new Color(130, 210, 130);
      }
    }

    this.font = new Font(Font.SANS_SERIF, Font.BOLD, 10);
    // Standard compact width for badges
    this.height = 16;
    this.width = switch (this.text) {
      case "DEBUG" -> 44;
      case "ERROR" -> 44;
      case "WARN" -> 38;
      default -> 36;
    };
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      g2.setColor(this.bgColor);
      g2.fillRoundRect(x, y + 1, this.width, this.height - 2, 4, 4);

      g2.setFont(this.font);
      g2.setColor(this.fgColor);
      FontMetrics fm = g2.getFontMetrics();
      int tx = x + (this.width - fm.stringWidth(this.text)) / 2;
      int ty = y + (this.height - fm.getHeight()) / 2 + fm.getAscent();
      g2.drawString(this.text, tx, ty);
    } finally {
      g2.dispose();
    }
  }

  @Override
  public int getIconWidth() {
    return this.width;
  }

  @Override
  public int getIconHeight() {
    return this.height;
  }
}
