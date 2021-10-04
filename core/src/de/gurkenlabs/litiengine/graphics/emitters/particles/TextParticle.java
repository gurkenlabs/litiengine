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

public class TextParticle extends Particle {
  private Font font;
  private final String text;

  public TextParticle(final String text) {
    super(1, 1);
    this.text = text;
  }

  public Font getFont() {
    return this.font;
  }

  public String getText() {
    return this.text;
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    if (this.getText() == null || this.getText().isEmpty()) {
      return;
    }

    final Font oldFont = g.getFont();
    if (this.getFont() != null) {
      g.setFont(this.getFont());
    }
    final FontMetrics fm = g.getFontMetrics();
    this.setWidth(fm.stringWidth(this.getText()));
    this.setHeight(g.getFont().getSize2D());

    final Point2D renderLocation = this.getRenderLocation(emitterOrigin);
    final AffineTransform rotate =
        AffineTransform.getRotateInstance(
            Math.toRadians(this.getAngle()), this.getWidth() * 0.5, this.getHeight() * 0.5);
    g.setFont(g.getFont().deriveFont(rotate));

    RenderingHints originalHints = g.getRenderingHints();
    g.setColor(
        new Color(
            this.getColor().getRed() / 255f,
            this.getColor().getGreen() / 255f,
            this.getColor().getBlue() / 255f,
            this.getOpacity()));

    if (this.isAntiAliased()) {
      TextRenderer.enableTextAntiAliasing(g);
    }

    g.drawString(text, (float) renderLocation.getX(), (float) renderLocation.getY());
    g.setFont(oldFont);
    g.setRenderingHints(originalHints);
  }

  public void setFont(final Font font) {
    this.font = font;
  }

  @Override
  public Rectangle2D getBoundingBox(final Point2D origin) {
    return new Rectangle2D.Double(
        origin.getX() + this.getX() - this.getWidth() / 2,
        origin.getY() + this.getY() - this.getHeight() * 1.5,
        this.getWidth(),
        this.getHeight());
  }
}
