package com.litiengine.graphics;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public final class ShapeRenderer {
  public static final Stroke DEFAULT_STROKE = new BasicStroke(1);

  private ShapeRenderer() {
    throw new UnsupportedOperationException();
  }

  public static void render(final Graphics2D g, final Shape shape) {
    if (shape == null) {
      return;
    }
    g.fill(shape);
  }

  public static void render(final Graphics2D g, final Shape shape, double x, double y) {
    g.translate(x, y);
    render(g, shape);
    g.translate(-x, -y);
  }

  public static void render(final Graphics2D g, final Shape shape, Point2D location) {
    render(g, shape, location.getX(), location.getY());
  }

  public static void renderOutline(final Graphics2D g, final Shape shape) {
    renderOutline(g, shape, DEFAULT_STROKE);
  }

  public static void renderOutline(final Graphics2D g, final Shape shape, final float stroke) {
    renderOutline(g, shape, new BasicStroke(stroke));
  }

  public static void renderOutline(final Graphics2D g, final Shape shape, final Stroke stroke) {
    if (shape == null) {
      return;
    }
    final Stroke oldStroke = g.getStroke();
    g.setStroke(stroke);
    g.draw(shape);
    g.setStroke(oldStroke);
  }

  public static void renderTransformed(final Graphics2D g, final Shape shape, AffineTransform transform) {

    render(g, transform.createTransformedShape(shape));
  }

  public static void renderOutlineTransformed(final Graphics2D g, final Shape shape, AffineTransform transform) {
    renderOutlineTransformed(g, shape, transform, DEFAULT_STROKE);
  }

  public static void renderOutlineTransformed(final Graphics2D g, final Shape shape, AffineTransform transform, final float stroke) {
    renderOutlineTransformed(g, shape, transform, new BasicStroke(stroke));
  }

  public static void renderOutlineTransformed(final Graphics2D g, final Shape shape, AffineTransform transform, final Stroke stroke) {
    if (transform == null) {
      renderOutline(g, shape, stroke);
      return;
    }

    renderOutline(g, transform.createTransformedShape(shape), stroke);
  }
}
