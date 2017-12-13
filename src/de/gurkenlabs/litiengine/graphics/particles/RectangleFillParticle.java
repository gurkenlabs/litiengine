package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.graphics.RenderEngine;

/**
 * Represents a particle in 2D space.
 */
public class RectangleFillParticle extends Particle {

  public RectangleFillParticle(final float width, final float height, final Color color, final int ttl) {
    super(width, height, color, ttl);
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    g.setColor(this.getColor());

    RenderEngine.fillShape(g, new Rectangle2D.Float(this.getRelativeX(emitterOrigin.getX()), this.getRelativeY(emitterOrigin.getY()), (float) this.getWidth(), (float) this.getHeight()));
  }
}