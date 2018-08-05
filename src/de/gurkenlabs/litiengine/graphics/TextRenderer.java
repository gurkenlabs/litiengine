package de.gurkenlabs.litiengine.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
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
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public final class TextRenderer {
  private TextRenderer() {
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
    render(g, text, x, y, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  public static void render(final Graphics2D g, final String text, Point2D location) {
    render(g, text, location.getX(), location.getY());
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
   *          the Anti-Aliasing object (e.g. RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
   * @see RenderingHints
   */
  public static void render(final Graphics2D g, final String text, final double x, final double y, Object antiAliasing) {
    if (text == null || text.isEmpty()) {
      return;
    }

    RenderingHints originalHints = g.getRenderingHints();
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasing);

    g.drawString(text, (float) x, (float) y);
    g.setRenderingHints(originalHints);
  }

  public static void render(final Graphics2D g, final String text, Point2D location, Object antiAliasing) {
    render(g, text, location.getX(), location.getY(), antiAliasing);
  }

  public static void renderRotated(final Graphics2D g, final String text, final double x, final double y, final double angle) {
    // TODO: does this actually work as expected if we're rendering on the copy?
    final Graphics2D g2 = (Graphics2D) g.create();
    g2.rotate(Math.toRadians(angle), x, y);
    render(g2, text, x, y);
    g2.dispose();
  }

  public static void renderRotated(final Graphics2D g, final String text, Point2D location, final double angle) {
    renderRotated(g, text, location.getX(), location.getY(), angle);
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
    renderWithLinebreaks(g, text, x, y, lineWidth, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
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
   *          the Anti-Aliasing object (e.g. RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
   * @see RenderingHints
   */
  public static void renderWithLinebreaks(final Graphics2D g, final String text, final double x, final double y, final double lineWidth, final Object antiAliasing) {
    if (text == null || text.isEmpty()) {
      return;
    }
    RenderingHints originalHints = g.getRenderingHints();
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasing);

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

  public static void renderWithLinebreaks(final Graphics2D g, final String text, Point2D location, final double lineWidth, final Object antiAliasing) {
    renderWithLinebreaks(g, text, location.getX(), location.getY(), lineWidth, antiAliasing);
  }

  /**
   * Draw text at the given coordinates with an outline in the provided color. This variant of drawTextWithShadow() uses
   * RenderingHints.VALUE_ANTIALIAS_OFF as Anti-Aliasing method by standard. For other Anti-Aliasing options, please use the
   * drawTextWithShadow()-variant with six parameters.
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
   * @see RenderingHints
   */
  public static void renderWithOutline(final Graphics2D g, final String text, final double x, final double y, final Color outlineColor) {
    renderWithOutline(g, text, x, y, outlineColor, RenderingHints.VALUE_ANTIALIAS_OFF);
  }
  
  public static void renderWithOutline(final Graphics2D g, final String text, Point2D location, final Color outlineColor) {
    renderWithOutline(g, text, location.getX(), location.getY(), outlineColor);
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
   * @param antiAliasing
   *          the Anti-Aliasing object (e.g. RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
   * @see RenderingHints
   */
  public static void renderWithOutline(final Graphics2D g, final String text, final double x, final double y, final Color outlineColor, final Object antiAliasing) {
    if(text == null || text.isEmpty()) {
      return;
    }
    
    Color fillColor = g.getColor();
    BasicStroke outlineStroke = new BasicStroke(g.getFont().getSize() * 1 / 10f);

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
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasing);
    g.setRenderingHint(RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY);

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
  
  public static void renderWithOutline(final Graphics2D g, final String text, Point2D location, final Color outlineColor, final Object antiAliasing) {
    renderWithOutline(g, text, location.getX(), location.getY(), outlineColor, antiAliasing);
  }
}