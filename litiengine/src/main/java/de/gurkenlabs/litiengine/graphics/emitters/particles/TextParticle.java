package de.gurkenlabs.litiengine.graphics.emitters.particles;

import de.gurkenlabs.litiengine.graphics.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Represents a particle that renders text.
 */
public class TextParticle extends Particle {
  private Font font;
  private final String text;

  /**
   * Constructs a TextParticle with the specified text.
   *
   * @param text The text to be rendered by this particle.
   */
  public TextParticle(final String text) {
    super(1, 1);
    this.text = text;
  }

  /**
   * Gets the font used by this TextParticle.
   *
   * @return The font used by this TextParticle.
   */
  public Font getFont() {
    return this.font;
  }

  /**
   * Gets the text rendered by this TextParticle.
   *
   * @return The text rendered by this TextParticle.
   */
  public String getText() {
    return this.text;
  }

  /**
   * Renders the text particle.
   *
   * @param g             The graphics context.
   * @param emitterOrigin The origin point of the emitter.
   */
  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    if (getText() == null || getText().isEmpty()) {
      return;
    }

    final Font oldFont = g.getFont();
    if (getFont() != null) {
      g.setFont(getFont());
    }
    final FontMetrics fm = g.getFontMetrics();
    this.setWidth(fm.stringWidth(getText()));
    this.setHeight(g.getFont().getSize2D());

    final Point2D renderLocation = getRenderLocation(emitterOrigin);
    final AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(getAngle()), getWidth() * 0.5, getHeight() * 0.5);
    g.setFont(g.getFont().deriveFont(rotate));

    RenderingHints originalHints = g.getRenderingHints();
    g.setColor(new Color(getColor().getRed() / 255f, getColor().getGreen() / 255f, getColor().getBlue() / 255f, getOpacity()));

    if (isAntiAliased()) {
      TextRenderer.enableTextAntiAliasing(g);
    }

    g.drawString(text, (float) renderLocation.getX(), (float) renderLocation.getY());
    g.setFont(oldFont);
    g.setRenderingHints(originalHints);
  }

  /**
   * Sets the font for this TextParticle.
   *
   * @param font The font to set.
   */
  public void setFont(final Font font) {
    this.font = font;
  }

  /**
   * Gets the bounding box of the text particle.
   *
   * @param origin The origin point.
   * @return The bounding box of the text particle.
   */
  @Override
  public Rectangle2D getBoundingBox(final Point2D origin) {
    return new Rectangle2D.Double(origin.getX() + getX() - getWidth() / 2, origin.getY() + getY() - getHeight() * 1.5,
      getWidth(), getHeight());
  }
}
