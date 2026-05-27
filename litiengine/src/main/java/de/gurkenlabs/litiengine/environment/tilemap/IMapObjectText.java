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
  String getText();

  /**
   * Gets the font used to render the text.
   *
   * @return the font
   */
  Font getFont();

  /**
   * Returns whether the text wraps within the bounds of its map object.
   *
   * @return {@code true} if word wrapping is enabled
   */
  boolean wrap();

  /**
   * Gets the text color.
   *
   * @return the color
   */
  Color getColor();

  /**
   * Gets the horizontal alignment of the text.
   *
   * @return the horizontal alignment
   */
  Align getAlign();

  /**
   * Gets the vertical alignment of the text.
   *
   * @return the vertical alignment
   */
  Valign getValign();

  /**
   * Returns whether the text is rendered bold.
   *
   * @return {@code true} if bold
   */
  boolean isBold();

  /**
   * Returns whether the text is rendered italic.
   *
   * @return {@code true} if italic
   */
  boolean isItalic();

  /**
   * Returns whether the text is rendered with an underline.
   *
   * @return {@code true} if underlined
   */
  boolean isUnderlined();

  /**
   * Returns whether the text is rendered with strike-through.
   *
   * @return {@code true} if strike-through is applied
   */
  boolean isStrikeout();

  /**
   * Returns whether the text is rendered with kerning enabled.
   *
   * @return {@code true} if kerning is applied
   */
  boolean useKerning();
}
