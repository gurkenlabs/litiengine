package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Represents an icon rendered using a specific font and text.
 */
public class FontIcon {
  /**
   * The font used to render the icon.
   */
  private final Font font;

  /**
   * The text or unicode character to be rendered as the icon.
   */
  private final String text;

  /**
   * Constructs a FontIcon with the specified font and character.
   *
   * @param font the font used to render the icon
   * @param text the character to be rendered as the icon
   */
  public FontIcon(final Font font, final char text) {
    this.font = font;
    this.text = String.valueOf(text);
  }

  /**
   * Constructs a FontIcon with the specified font and unicode string.
   *
   * @param font    the font used to render the icon
   * @param unicode the unicode string to be rendered as the icon
   */
  public FontIcon(final Font font, final String unicode) {
    this.font = font;
    this.text = unicode;
  }

  /**
   * Returns the font used to render the icon.
   *
   * @return the font used to render the icon
   */
  public Font getFont() {
    return this.font;
  }

  /**
   * Returns the text or unicode character to be rendered as the icon.
   *
   * @return the text or unicode character to be rendered as the icon
   */
  public String getText() {
    return this.text;
  }

  /**
   * Renders the icon using the specified graphics context, color, font size, and position.
   *
   * @param g        the graphics context to use for rendering
   * @param color    the color to render the icon
   * @param fontSize the size of the font to render the icon
   * @param x        the x-coordinate where the icon should be rendered
   * @param y        the y-coordinate where the icon should be rendered
   * @param bold     true if the font should be bold, false otherwise
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
      g.setFont(getFont().deriveFont(Font.BOLD, fontSize));
    } else {
      g.setFont(getFont().deriveFont(fontSize));
    }
    TextRenderer.render(g, getText(), x, y);
    g.setColor(oldColor);
    g.setFont(oldFont);
  }
}
