package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * Represents a particle in the shape of an ellipse. This class extends the {@code ShapeParticle} to provide specific behavior for elliptical
 * particles.
 */
public class EllipseParticle extends ShapeParticle {
  /**
   * Initializes a new instance of the {@code EllipseParticle} class with the specified dimensions.
   *
   * @param width  The width of the ellipse.
   * @param height The height of the ellipse.
   */
  public EllipseParticle(final float width, final float height) {
    super(width, height);
  }

  /**
   * Creates and returns the shape of the ellipse particle, transformed based on the emitter's origin and rotation.
   *
   * @param emitterOrigin The origin point of the emitter.
   * @return A {@code Shape} representing the transformed ellipse.
   */
  @Override
  protected Shape getShape(final Point2D emitterOrigin) {
    final AffineTransform rotate =
      AffineTransform.getRotateInstance(
        Math.toRadians(getAngle()),
        getAbsoluteX(emitterOrigin) + getWidth() * 0.5,
        getAbsoluteY(emitterOrigin) + getHeight() * 0.5);
    return rotate.createTransformedShape(
      new Ellipse2D.Float(
        getAbsoluteX(emitterOrigin),
        getAbsoluteY(emitterOrigin),
        getWidth(),
        getHeight()));
  }
}
