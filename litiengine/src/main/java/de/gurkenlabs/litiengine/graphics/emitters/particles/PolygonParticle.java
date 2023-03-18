package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class PolygonParticle extends ShapeParticle {
  private int sides;

  public PolygonParticle(float width, float height, int sides) {
    super(width, height);
    this.sides = sides;
  }

  @Override
  protected Shape getShape(Point2D emitterOrigin) {
    Path2D path = new Path2D.Double();
    double x = this.getAbsoluteX(emitterOrigin) + this.getWidth() / 2;
    double y = this.getAbsoluteY(emitterOrigin) + this.getHeight() / 2;
    double theta = 2 * Math.PI / this.sides;
    path.moveTo(x + this.getWidth(), y + 0);
    for (int i = 0; i < this.sides; i++) {
      path.lineTo(
          x + this.getWidth() * Math.cos(theta * i),
          y + this.getHeight() * Math.sin(theta * i));
    }
    path.closePath();
    final AffineTransform rotate =
        AffineTransform.getRotateInstance(
            Math.toRadians(this.getAngle()),
            this.getAbsoluteX(emitterOrigin) + this.getWidth() * 0.5,
            this.getAbsoluteY(emitterOrigin) + this.getHeight() * 0.5);
    return rotate.createTransformedShape(path);
  }
}
