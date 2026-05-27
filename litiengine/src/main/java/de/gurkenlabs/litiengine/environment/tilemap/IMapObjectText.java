package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import java.awt.Color;
import java.awt.Font;

/**
 * Represents the text content and rendering attributes of a Tiled text map object.
 */
public interface IMapObjectText {
  /**
   * Gets the text string.
   *
   * @return the text
   */
  public String getText();

  /**
   * Gets the font used to render the text.
   *
   * @return the font
   */
  public Font getFont();

  /**
   * Returns whether the text wraps within the bounds of its map object.
   *
   * @return {@code true} if word wrapping is enabled
   */
  public boolean wrap();

  /**
   * Gets the text color.
   *
   * @return the color
   */
  public Color getColor();

  /**
   * Gets the horizontal alignment of the text.
   *
   * @return the horizontal alignment
   */
  public Align getAlign();

  /**
   * Gets the vertical alignment of the text.
   *
   * @return the vertical alignment
   */
  public Valign getValign();

  /**
   * Returns whether the text is rendered bold.
   *
   * @return {@code true} if bold
   */
  public boolean isBold();

  /**
   * Returns whether the text is rendered italic.
   *
   * @return {@code true} if italic
   */
  public boolean isItalic();

  /**
   * Returns whether the text is rendered with an underline.
   *
   * @return {@code true} if underlined
   */
  public boolean isUnderlined();

  /**
   * Returns whether the text is rendered with strike-through.
   *
   * @return {@code true} if strike-through is applied
   */
  public boolean isStrikeout();

  /**
   * Returns whether the text is rendered with kerning enabled.
   *
   * @return {@code true} if kerning is applied
   */
  public boolean useKerning();
}
