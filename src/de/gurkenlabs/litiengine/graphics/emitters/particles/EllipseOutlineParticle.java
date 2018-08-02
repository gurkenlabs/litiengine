package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class EllipseOutlineParticle extends OutlineParticle {
  public EllipseOutlineParticle(final float width, final float height, final Color color, final int ttl) {
    super(width, height, color, ttl);
  }

  @Override
  protected Shape getShape(Point2D emitterOrigin) {
    return new Ellipse2D.Float(this.getAbsoluteX(emitterOrigin), this.getAbsoluteY(emitterOrigin), this.getWidth(), this.getHeight());
  }
}