package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** A fonticon is an class that represents a single character of an icon font. */
public class FontIcon {
  /** The font. */
  private final Font font;

  /** The text. */
  private final String text;

  /**
   * Instantiates a new icon.
   *
   * @param font the font
   * @param text the text
   */
  public FontIcon(final Font font, final char text) {
    this.font = font;
    this.text = String.valueOf(text);
  }

  public FontIcon(final Font font, final String unicode) {
    this.font = font;
    this.text = unicode;
  }

  /**
   * Gets the font.
   *
   * @return the font
   */
  public Font getFont() {
    return this.font;
  }

  /**
   * Gets the text.
   *
   * @return the text
   */
  public String getText() {
    return this.text;
  }

  /**
   * Render.
   *
   * @param g the g
   * @param color the color
   * @param fontSize the font size
   * @param x the x
   * @param y the y
   * @param bold the bold
   */
  public void render(
      final Graphics2D g,
      final Color color,
      final float fontSize,
      final double x,
      final double y,
      final boolean bold) {
    final Color oldColor = g.getColor();
    final Font oldFont = g.getFont();
    g.setColor(color);
    if (bold) {
      g.setFont(this.getFont().deriveFont(Font.BOLD, fontSize));
    } else {
      g.setFont(this.getFont().deriveFont(fontSize));
    }
    TextRenderer.render(g, this.getText(), x, y);
    g.setColor(oldColor);
    g.setFont(oldFont);
  }
}
