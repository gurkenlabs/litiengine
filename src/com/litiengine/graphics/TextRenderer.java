package com.litiengine.graphics;

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
import java.util.ArrayList;
import java.util.List;

import com.litiengine.Game;
import com.litiengine.Valign;
import com.litiengine.util.MathUtilities;
import com.litiengine.Align;

public final class TextRenderer {
  private TextRenderer() {
    throw new UnsupportedOperationException();
  }

  /**
   * Draw text at the given coordinates. This variant of drawText() uses RenderingHints.VALUE_TEXT_ANTIALIAS_OFF as Anti-Aliasing method by
   * standard. For other Anti-Aliasing options, please use the drawText()-variant with five parameters.
   *
   * @param g    the Graphics2D object to draw on
   * @param text the String to be distributed over all generated lines
   * @param x    the min x coordinate
   * @param y    the min y coordinate
   */
  public static void render(final Graphics2D g, final String text, final double x, final double y) {
    render(g, text, x, y, true);
  }

  public static void render(final Graphics2D g, final String text, Point2D location) {
    render(g, text, location.getX(), location.getY());
  }

  /**
   * Draws text with the specified alignment.
   *
   * @param g                 the Graphics2D object to draw on
   * @param text              the String to be distributed over all generated lines
   * @param align             The horizontal alignment.
   * @param valign            The vertical alignment.
   * @param offsetX           The horizontal offset that is added to the alignment.
   * @param offsetY           The vertical offset that is added to the alignment.
   */
  public static void render(final Graphics2D g, final String text, Align align, Valign valign, double offsetX, double offsetY) {
    final Rectangle2D bounds = g.getClipBounds();
    render(g, text, bounds, align, valign, offsetX, offsetY, false);
  }

  public static void render(final Graphics2D g, final String text, Rectangle2D bounds, Align alignment, Valign verticalAlignment, boolean scaleFont) {
    render(g, text, bounds, alignment, verticalAlignment, 0, 0, scaleFont);
  }

  /**
   * Draws text within the given boundaries using the specified alignment and scales the font size, if desired.
   *
   * @param g                 the Graphics2D object to draw on
   * @param text              the String to be distributed over all generated lines
   * @param bounds            the Rectangle defining the boundaries used for alignment and scaling.
   * @param align             The horizontal alignment.
   * @param valign            The vertical alignment.
   * @param offsetX           The horizontal offset that is added to the alignment.
   * @param offsetY           The vertical offset that is added to the alignment.
   * @param scaleFont         if true, scale the font so that the text will fit inside the given rectangle. If not, use the Graphics context's previous font size.
   */
  public static void render(final Graphics2D g, final String text, Rectangle2D bounds, Align align, Valign valign, double offsetX,
      double offsetY, boolean scaleFont) {
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
    double locationX = bounds.getX() + align.getLocation(bounds.getWidth(), g.getFontMetrics().stringWidth(text)) + offsetX;
    double locationY =
        bounds.getY() + valign.getLocation(bounds.getHeight(), getHeight(g, text)) + g.getFontMetrics().getAscent() + offsetY;
    render(g, text, locationX, locationY);
    g.setFont(g.getFont().deriveFont(previousFontSize));
  }

  /**
   * Draw text at the given coordinates. This variant of drawText() uses a provided AntiAliasing parameter.
   *
   * @param g            the Graphics2D object to draw on
   * @param text         the String to be distributed over all generated lines
   * @param x            the min x coordinate
   * @param y            the min y coordinate
   * @param antiAliasing Configure whether or not to render the text with antialiasing.
   * @see RenderingHints
   */
  public static void render(final Graphics2D g, final String text, final double x, final double y, boolean antiAliasing) {
    if (text == null || text.isEmpty()) {
      return;
    }

    RenderingHints originalHints = g.getRenderingHints();

    if (antiAliasing) {
      enableTextAntiAliasing(g);
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
      enableTextAntiAliasing(g);
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
   * @param g         the Graphics2D object to draw on
   * @param text      the String to be distributed over all generated lines
   * @param x         the min x coordinate
   * @param y         the min y coordinate
   * @param lineWidth the max line width
   */
  public static void renderWithLinebreaks(final Graphics2D g, final String text, final double x, final double y, final double lineWidth) {
    renderWithLinebreaks(g, text, x, y, lineWidth, true);
  }

  public static void renderWithLinebreaks(final Graphics2D g, final String text, Point2D location, final double lineWidth) {
    renderWithLinebreaks(g, text, location.getX(), location.getY(), lineWidth);
  }

  /**
   * Draw text at the given coordinates with a maximum line width for automatic line breaks and a provided Anti-Aliasing parameter.
   *
   * @param g            the Graphics2D object to draw on
   * @param text         the String to be distributed over all generated lines
   * @param x            the min x coordinate
   * @param y            the min y coordinate
   * @param lineWidth    the max line width
   * @param antiAliasing Configure whether or not to render the text with antialiasing.
   * @see RenderingHints
   */
  public static void renderWithLinebreaks(final Graphics2D g, final String text, final double x, final double y, final double lineWidth,
      final boolean antiAliasing) {
    renderWithLinebreaks(g, text, Align.LEFT, Valign.TOP, x, y, lineWidth, 0.0, antiAliasing);
  }

  /**
   * Draw text at the given coordinates with a maximum line width for automatic line breaks and a provided Anti-Aliasing parameter.
   *
   * @param g            the Graphics2D object to draw on
   * @param text         the String to be distributed over all generated lines
   * @param align        The horizontal alignment.
   * @param valign       The vertical alignment.
   * @param x            the min x coordinate
   * @param y            the min y coordinate
   * @param height       the line height.
   * @param width        the line width
   * @param antiAliasing Configure whether or not to render the text with antialiasing.
   * @see RenderingHints
   */
  public static void renderWithLinebreaks(final Graphics2D g, final String text, Align align, Valign valign, final double x, final double y,
      final double width, final double height, final boolean antiAliasing) {
    if (text == null || text.isEmpty()) {
      return;
    }
    RenderingHints originalHints = g.getRenderingHints();

    if (antiAliasing) {
      enableTextAntiAliasing(g);
    }

    final FontRenderContext frc = g.getFontRenderContext();
    List<TextLayout> lines = new ArrayList<>();
    float textHeight = 0f;
    for (String s : text.split(System.lineSeparator())) {
      final AttributedString styledText = new AttributedString(s);
      styledText.addAttribute(TextAttribute.FONT, g.getFont());
      final AttributedCharacterIterator iterator = styledText.getIterator();
      final LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, frc);
      while (true) {
        TextLayout nextLayout = measurer.nextLayout((float) width);
        lines.add(nextLayout);
        textHeight += nextLayout.getAscent() + nextLayout.getDescent();
        if (measurer.getPosition() >= text.length()) {
          break;
        }
        textHeight += nextLayout.getLeading();
      }
    }
    float textY = (float) (y + valign.getLocation(height, textHeight));
    for (TextLayout layout : lines) {
      textY += layout.getAscent();
      layout.draw(g, (float) (x + align.getLocation(width, layout.getAdvance())), textY);
      textY += layout.getDescent() + layout.getLeading();
    }
    g.setRenderingHints(originalHints);
  }

  public static void renderWithLinebreaks(final Graphics2D g, final String text, Point2D location, final double lineWidth,
      final boolean antiAliasing) {
    renderWithLinebreaks(g, text, location.getX(), location.getY(), lineWidth, antiAliasing);
  }

  /**
   * Draw text at the given coordinates with an outline in the provided color. This variant of drawTextWithShadow() doesn't use Anti-Aliasing.
   * For other Anti-Aliasing options, please specify the boolean value that controls it.
   *
   * @param g            the Graphics2D object to draw on
   * @param text         the String to be distributed over all generated lines
   * @param x            the min x coordinate
   * @param y            the min y coordinate
   * @param outlineColor the outline color
   */
  public static void renderWithOutline(final Graphics2D g, final String text, final double x, final double y, final Color outlineColor) {
    renderWithOutline(g, text, x, y, outlineColor, false);
  }

  public static void renderWithOutline(final Graphics2D g, final String text, Point2D location, final Color outlineColor) {
    renderWithOutline(g, text, location.getX(), location.getY(), outlineColor);
  }

  public static void renderWithOutline(final Graphics2D g, final String text, final double x, final double y, final Color outlineColor,
      final boolean antiAliasing) {
    float stroke = (float) MathUtilities.clamp((g.getFont().getSize2D() * 1 / 5f) * Math.log(Game.world().camera().getRenderScale()), 1, 100);
    renderWithOutline(g, text, x, y, outlineColor, stroke, antiAliasing);
  }

  /**
   * Draw text at the given coordinates with an outline in the provided color and a provided Anti-Aliasing parameter.
   *
   * @param g            the Graphics2D object to draw on
   * @param text         the String to be distributed over all generated lines
   * @param x            the min x coordinate
   * @param y            the min y coordinate
   * @param outlineColor the outline color
   * @param stroke       the width of the outline
   * @param antiAliasing the Anti-Aliasing object (e.g. RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
   * @see RenderingHints
   */
  public static void renderWithOutline(final Graphics2D g, final String text, final double x, final double y, final Color outlineColor,
      final float stroke, final boolean antiAliasing) {
    renderWithOutline(g, text, x, y, 0.0, 0.0, outlineColor, stroke, Align.LEFT, Valign.TOP, antiAliasing);
  }

  /**
   * Draw text at the given coordinates with an outline in the provided color and a provided Anti-Aliasing parameter.
   *
   * @param g            the Graphics2D object to draw on
   * @param text         the String to be distributed over all generated lines
   * @param x            the min x coordinate
   * @param y            the min y coordinate
   * @param width        the width of the  bounding box in which the text will be aligned
   * @param height       the height of the  bounding box in which the text will be aligned
   * @param outlineColor the outline color
   * @param stroke       the thickness of the outline
   * @param align        The horizontal alignment.
   * @param valign       The vertical alignment.
   * @param antiAliasing Configure whether or not to render the text with antialiasing.
   * @see RenderingHints
   */
  public static void renderWithOutline(final Graphics2D g, final String text, final double x, final double y, double width, double height,
      final Color outlineColor, final float stroke, Align align, Valign valign, final boolean antiAliasing) {
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
    Rectangle2D bounds = glyphVector.getVisualBounds();
    // get the shape object
    AffineTransform at = new AffineTransform();
    at.translate(x + align.getLocation(width, bounds.getWidth()), y + valign.getLocation(height, bounds.getHeight()) + bounds.getHeight());
    Shape textShape = at.createTransformedShape(glyphVector.getOutline());

    // activate anti aliasing for text rendering (if you want it to look nice)

    if (antiAliasing) {
      enableTextAntiAliasing(g);
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

  public static void renderWithOutline(final Graphics2D g, final String text, Point2D location, final Color outlineColor,
      final boolean antiAliasing) {
    renderWithOutline(g, text, location.getX(), location.getY(), outlineColor, antiAliasing);
  }

  /**
   * Retrieve the bounds of some text if it was to be drawn on the specified Graphics2D
   *
   * @param g    The Graphics2D object to be drawn on
   * @param text The string to calculate the bounds of
   * @return The bounds of the specified String in the specified Graphics context.
   * @see java.awt.FontMetrics#getStringBounds(String str, Graphics context)
   */
  public static Rectangle2D getBounds(final Graphics2D g, final String text) {
    return g.getFontMetrics().getStringBounds(text, g);
  }

  /**
   * Retrieve the width of some text if it was to be drawn on the specified Graphics2D
   *
   * @param g    The Graphics2D object to be drawn on
   * @param text The string to retrieve the width of
   * @return The width of the specified text
   */
  public static double getWidth(final Graphics2D g, final String text) {
    return getBounds(g, text).getWidth();
  }

  /**
   * Retrieve the height of some text if it was to be drawn on the specified Graphics2D
   *
   * @param g    The Graphics2D object to be drawn on
   * @param text The string to retrieve the height of
   * @return The height of the specified text
   */
  public static double getHeight(final Graphics2D g, final String text) {
    return getBounds(g, text).getHeight();
  }

  public static void enableTextAntiAliasing(final Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
  }
}
