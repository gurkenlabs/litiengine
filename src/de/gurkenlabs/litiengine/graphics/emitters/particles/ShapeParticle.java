package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.graphics.RenderEngine;

public abstract class ShapeParticle extends Particle {

  public ShapeParticle(float width, float height, Color color, int ttl) {
    super(width, height, color, ttl);
  }

  protected abstract Shape getShape(final Point2D emitterOrigin);
  
  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    g.setColor(this.getColor());

    RenderEngine.renderShape(g, this.getShape(emitterOrigin));
  }
}
