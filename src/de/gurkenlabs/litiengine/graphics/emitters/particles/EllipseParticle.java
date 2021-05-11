package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class EllipseParticle extends ShapeParticle {

  public EllipseParticle(final float width, final float height) {
    super(width, height);
  }

  @Override
  protected Shape getShape(final Point2D emitterOrigin) {
    final AffineTransform rotate =
        AffineTransform.getRotateInstance(
            Math.toRadians(this.getAngle()),
            this.getAbsoluteX(emitterOrigin) + this.getWidth() * 0.5,
            this.getAbsoluteY(emitterOrigin) + this.getHeight() * 0.5);
    return rotate.createTransformedShape(
        new Ellipse2D.Float(
            this.getAbsoluteX(emitterOrigin),
            this.getAbsoluteY(emitterOrigin),
            this.getWidth(),
            this.getHeight()));
  }
}
