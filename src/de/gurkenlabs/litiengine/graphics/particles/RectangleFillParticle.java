package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Represents a particle in 2D space.
 */
public class RectangleFillParticle extends Particle {

  public RectangleFillParticle(final float width, final float height, final Color color, final int ttl) {
    super(width, height, color, ttl);
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(emitterOrigin);
    g.setColor(this.getColor());
    g.fill(new Rectangle2D.Float((float) renderLocation.getX(), (float) renderLocation.getY(), (int) this.getWidth(), (int) this.getHeight()));
  }
}