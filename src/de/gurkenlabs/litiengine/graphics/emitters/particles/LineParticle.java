package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class LineParticle extends ShapeParticle {

  public LineParticle(final float width, final float height, final Color color) {
    super(width, height, color);
  }

  @Override
  protected Shape getShape(Point2D emitterOrigin) {
    float x = this.getAbsoluteX(emitterOrigin);
    float y = this.getAbsoluteY(emitterOrigin);
    final AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(this.getAngle()), this.getAbsoluteX(emitterOrigin) + this.getWidth() * 0.5, this.getAbsoluteY(emitterOrigin) + this.getHeight() * 0.5);
    return rotate.createTransformedShape(new Line2D.Double(x, y, x + this.getWidth(), y + this.getHeight()));
  }
}
