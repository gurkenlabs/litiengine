package de.gurkenlabs.litiengine.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.gui.GuiProperties;
import de.gurkenlabs.litiengine.util.MathUtilities;

public final class TextRenderer {
  private TextRenderer() {
    throw new UnsupportedOperationException();
  }

  /**
   * Draw text at the given coordinates. This variant of drawText() uses RenderingHints.VALUE_TEXT_ANTIALIAS_OFF as Anti-Aliasing method by
   * standard. For other Anti-Aliasing options, please use the drawText()-variant with five parameters.
   * 
   * @param g
   *          the Graphics2D object to draw on
   * @param text
   *          the String to be distributed over all generated lines
   * @param x
   *          the min x coordinate
   * @param y
   *          the min y coordinate
   */
  public static void render(final Graphics2D g, final String text, final double x, final double y) {
    render(g, text, x, y, GuiProperties.getDefaultAppearance().getTextAntialiasing());
  }

  public static void render(final Graphics2D g, final String text, Point2D location) {
    render(g, text, location.getX(), location.getY());
  }

  /**
   * Draws text with the specified alignment.
   * 
   * @param g
   *          the Graphics2D object to draw on
   * @param text
   *          the String to be distributed over all generated lines
   * @param alignment
   *          The horizontal alignment.
   * @param verticalAlignment
   *          The vertical alignment.
   */
  public static void render(final Graphics2D g, final String text, Align alignment, Valign verticalAlignment) {
    final Rectangle2D bounds = g.getClipBounds();
    render(g, text, bounds, alignment, verticalAlignment, false);
  }

  /**
   * Draws text within the given boundaries using the specified alignment and scales the font size, if desired.
   * 
   * @param g
   *          the Graphics2D object to draw on
   * @param text
   *          the String to be distributed over all generated lines
   * @param bounds
   *          the Rectangle defining the boundaries used for alignment and scaling.
   * @param alignment
   *          The horizontal alignment.
   * @param verticalAlignment
   *          The vertical alignment.
   * @param scaleFont
   *          if true, scale the font so that the text will fit inside the given rectangle. If not, use the Graphics context's previous font size.
   */
  public static void render(final Graphics2D g, final String text, Rectangle2D bounds, Align alignment, Valign verticalAlignment, boolean scaleFont) {
    if (bounds == null) {
      return;
    }
    float previousFontSize = g.getFont().getSize2D();
    if (scaleFont) {
      float currentFontSize = previousFontSize;
      while ((getWidth(g, text) > bounds.getWidth() || getHeight(g, text) > bounds.getHeight()) && currentFontSize > .1f) {
        currentFontSize -= .1f;
        g.setFont(g.getFont().deriveFont(currentFontSize));
      }
    }
    double locationX = bounds.getX() + alignment.getLocation(bounds.getWidth(), g.getFontMetrics().stringWidth(text));
    double locationY = bounds.getY() + verticalAlignment.getLocation(bounds.getHeight(), getHeight(g, text)) + g.getFontMetrics().getAscent();
    render(g, text, locationX, locationY);
    g.setFont(g.getFont().deriveFont(previousFontSize));
  }

  /**
   * Draw text at the given coordinates. This variant of drawText() uses a provided AntiAliasing parameter.
   * 
   * @param g
   *          the Graphics2D object to draw on
   * @param text
   *          the String to be distributed over all generated lines
   * @param x
   *          the min x coordinate
   * @param y
   *          the min y coordinate
   * @param antiAliasing
   *          Configure whether or not to render the text with antialiasing.
   * @see RenderingHints
   */
  public static void render(final Graphics2D g, final String text, final double x, final double y, boolean antiAliasing) {
    if (text == null || text.isEmpty()) {
      return;
    }

    RenderingHints originalHints = g.getRenderingHints();

    if (antiAliasing) {
      enableAntiAliasing(g);
    }

    g.drawString(text, (float) x, (float) y);
    g.setRenderingHints(originalHints);
  }

  public static void render(final Graphics2D g, final String text, Point2D location, boolean antiAliasing) {
    render(g, text, location.getX(), location.getY(), antiAliasing);
  }

  public static void renderRotated(final Graphics2D g, final String text, final double x, final double y, final double angle, boolean antiAliasing) {
    RenderingHints originalHints = g.getRenderingHints();

    if (antiAliasing) {
      enableAntiAliasing(g);
    }

    renderRotated(g, text, x, y, angle);
    g.setRenderingHints(originalHints);
  }

  public static void renderRotated(final Graphics2D g, final String text, final double x, final double y, final double angle) {
    AffineTransform oldTx = g.getTransform();
    g.rotate(Math.toRadians(angle), x, y);
    render(g, text, x, y);
    g.setTransform(oldTx);
  }

  public static void renderRotated(final Graphics2D g, final String text, Point2D location, final double angle) {
    renderRotated(g, text, location.getX(), location.getY(), angle);
  }

  public static void renderRotated(final Graphics2D g, final String text, Point2D location, final double angle, boolean antiAliasing) {
    renderRotated(g, text, location.getX(), location.getY(), angle, antiAliasing);
  }

  /**
   * Draw text at the given coordinates with a maximum line width for automatic line breaks. This variant of drawTextWithAutomaticLinebreaks() uses
   * RenderingHints.VALUE_TEXT_ANTIALIAS_OFF as Anti-Aliasing method by
   * standard. For other Anti-Aliasing options, please use the drawTextWithAutomaticLinebreaks()-variant with six parameters.
   * 
   * @param g
   *          the Graphics2D object to draw on
   * @param text
   *          the String to be distributed over all generated lines
   * @param x
   *          the min x coordinate
   * @param y
   *          the min y coordinate
   * @param lineWidth
   *          the max line width
   */
  public static void renderWithLinebreaks(final Graphics2D g, final String text, final double x, final double y, final double lineWidth) {
    renderWithLinebreaks(g, text, x, y, lineWidth, GuiProperties.getDefaultAppearance().getTextAntialiasing());
  }

  public static void renderWithLinebreaks(final Graphics2D g, final String text, Point2D location, final double lineWidth) {
    renderWithLinebreaks(g, text, location.getX(), location.getY(), lineWidth);
  }

  /**
   * Draw text at the given coordinates with a maximum line width for automatic line breaks and a provided Anti-Aliasing parameter.
   * 
   * @param g
   *          the Graphics2D object to draw on
   * @param text
   *          the String to be distributed over all generated lines
   * @param x
   *          the min x coordinate
   * @param y
   *          the min y coordinate
   * @param lineWidth
   *          the max line width
   * @param antiAliasing
   *          Configure whether or not to render the text with antialiasing.
   * @see RenderingHints
   */
  public static void renderWithLinebreaks(final Graphics2D g, final String text, final double x, final double y, final double lineWidth, final boolean antiAliasing) {
    if (text == null || text.isEmpty()) {
      return;
    }
    RenderingHints originalHints = g.getRenderingHints();

    if (antiAliasing) {
      enableAntiAliasing(g);
    }

    final FontRenderContext frc = g.getFontRenderContext();
    final AttributedString styledText = new AttributedString(text);
    styledText.addAttribute(TextAttribute.FONT, g.getFont());
    final AttributedCharacterIterator iterator = styledText.getIterator();
    final LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, frc);
    measurer.setPosition(0);
    float textY = (float) y;
    while (measurer.getPosition() < text.length()) {
      final TextLayout nextLayout = measurer.nextLayout((float) lineWidth);
      textY += nextLayout.getAscent();
      final float dx = (float) (nextLayout.isLeftToRight() ? 0 : lineWidth - nextLayout.getAdvance());
      nextLayout.draw(g, (float) (x + dx), textY);
      textY += nextLayout.getDescent() + nextLayout.getLeading();
    }
    g.setRenderingHints(originalHints);
  }

  public static void renderWithLinebreaks(final Graphics2D g, final String text, Point2D location, final double lineWidth, final boolean antiAliasing) {
    renderWithLinebreaks(g, text, location.getX(), location.getY(), lineWidth, antiAliasing);
  }

  /**
   * Draw text at the given coordinates with an outline in the provided color. This variant of drawTextWithShadow() doesn't use Anti-Aliasing.
   * For other Anti-Aliasing options, please specify the boolean value that controls it.
   * 
   * @param g
   *          the Graphics2D object to draw on
   * @param text
   *          the String to be distributed over all generated lines
   * @param x
   *          the min x coordinate
   * @param y
   *          the min y coordinate
   * @param outlineColor
   *          the outline color
   */
  public static void renderWithOutline(final Graphics2D g, final String text, final double x, final double y, final Color outlineColor) {
    renderWithOutline(g, text, x, y, outlineColor, false);
  }

  public static void renderWithOutline(final Graphics2D g, final String text, Point2D location, final Color outlineColor) {
    renderWithOutline(g, text, location.getX(), location.getY(), outlineColor);
  }

  public static void renderWithOutline(final Graphics2D g, final String text, final double x, final double y, final Color outlineColor, final boolean antiAliasing) {
    float stroke = (float) MathUtilities.clamp((g.getFont().getSize2D() * 1 / 5f) * Math.log(Game.world().camera().getRenderScale()), 1, 100);
    renderWithOutline(g, text, x, y, outlineColor, stroke, antiAliasing);
  }

  /**
   * Draw text at the given coordinates with an outline in the provided color and a provided Anti-Aliasing parameter.
   * 
   * @param g
   *          the Graphics2D object to draw on
   * @param text
   *          the String to be distributed over all generated lines
   * @param x
   *          the min x coordinate
   * @param y
   *          the min y coordinate
   * @param outlineColor
   *          the outline color
   * @param stroke
   *          the width of the outline
   * @param antiAliasing
   *          the Anti-Aliasing object (e.g. RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
   * @see RenderingHints
   */
  public static void renderWithOutline(final Graphics2D g, final String text, final double x, final double y, final Color outlineColor, final float stroke, final boolean antiAliasing) {
    if (text == null || text.isEmpty()) {
      return;
    }

    Color fillColor = g.getColor();
    BasicStroke outlineStroke = new BasicStroke(stroke);

    // remember original settings
    Color originalColor = g.getColor();
    Stroke originalStroke = g.getStroke();
    RenderingHints originalHints = g.getRenderingHints();

    // create a glyph vector from your text
    GlyphVector glyphVector = g.getFont().createGlyphVector(g.getFontRenderContext(), text);
    // get the shape object
    AffineTransform at = new AffineTransform();
    at.translate(x, y);
    Shape textShape = at.createTransformedShape(glyphVector.getOutline());

    // activate anti aliasing for text rendering (if you want it to look nice)

    if (antiAliasing) {
      enableAntiAliasing(g);
    }

    g.setColor(outlineColor);
    g.setStroke(outlineStroke);
    g.draw(textShape); // draw outline

    g.setColor(fillColor);
    g.fill(textShape); // fill the shape

    // reset to original settings after drawing
    g.setColor(originalColor);
    g.setStroke(originalStroke);
    g.setRenderingHints(originalHints);
  }

  public static void renderWithOutline(final Graphics2D g, final String text, Point2D location, final Color outlineColor, final boolean antiAliasing) {
    renderWithOutline(g, text, location.getX(), location.getY(), outlineColor, antiAliasing);
  }

  /**
   * Retrieve the bounds of some text if it was to be drawn on the specified Graphics2D
   * 
   * @param g
   *          The Graphics2D object to be drawn on
   * @param text
   *          The string to calculate the bounds of
   * 
   * @return The bounds of the specified String in the specified Graphics context.
   * 
   * @see java.awt.FontMetrics#getStringBounds(String str, Graphics context)
   */
  public static Rectangle2D getBounds(final Graphics2D g, final String text) {
    return g.getFontMetrics().getStringBounds(text, g);
  }

  /**
   * Retrieve the width of some text if it was to be drawn on the specified Graphics2D
   * 
   * @param g
   *          The Graphics2D object to be drawn on
   * @param text
   *          The string to retrieve the width of
   * @return
   *         The width of the specified text
   */
  public static double getWidth(final Graphics2D g, final String text) {
    return getBounds(g, text).getWidth();
  }

  /**
   * Retrieve the height of some text if it was to be drawn on the specified Graphics2D
   * 
   * @param g
   *          The Graphics2D object to be drawn on
   * @param text
   *          The string to retrieve the height of
   * @return
   *         The height of the specified text
   */
  public static double getHeight(final Graphics2D g, final String text) {
    return getBounds(g, text).getHeight();
  }

  private static void enableAntiAliasing(final Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
  }
}
