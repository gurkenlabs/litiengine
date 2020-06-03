package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Point2D;

public class TriangleParticle extends ShapeParticle {

  public TriangleParticle(final float width, final float height, final Color color, final int ttl) {
    super(width, height, color, ttl);
  }

  @Override
  protected Shape getShape(Point2D emitterOrigin) {
    float x = this.getAbsoluteX(emitterOrigin);
    float y = this.getAbsoluteY(emitterOrigin);
    final int[] xArr = { (int) x, (int) (x + this.getWidth() / 2), (int) (x + this.getWidth()) };
    final int[] yArr = { (int) y, (int) (y - this.getHeight()), (int) y };
    return new Polygon(xArr, yArr, 3);
  }
}