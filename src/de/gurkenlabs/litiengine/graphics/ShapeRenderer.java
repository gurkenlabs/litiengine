package de.gurkenlabs.litiengine.graphics;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public final class ShapeRenderer {
  public static final Stroke DEFAULT_STROKE = new BasicStroke(1);

  private ShapeRenderer() {
  }

  public static void render(final Graphics2D g, final Shape shape) {
    g.fill(shape);
  }

  public static void render(final Graphics2D g, final Shape shape, double x, double y) {
    AffineTransform oldTransform = g.getTransform();
    g.translate(x, y);
    render(g, shape);
    g.setTransform(oldTransform);
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
    final Stroke oldStroke = g.getStroke();
    g.setStroke(stroke);
    g.draw(shape);
    g.setStroke(oldStroke);
  }

  public static void renderTransformed(final Graphics2D g, final Shape shape, AffineTransform transform) {
    final AffineTransform oldTransForm = g.getTransform();
    g.transform(transform);
    render(g, shape);
    g.setTransform(oldTransForm);
  }

  public static void renderOutlineTransformed(final Graphics2D g, final Shape shape, AffineTransform transform) {
    renderOutlineTransformed(g, shape, transform, DEFAULT_STROKE);
  }

  public static void renderOutlineTransformed(final Graphics2D g, final Shape shape, AffineTransform transform, final Stroke stroke) {
    final AffineTransform oldTransForm = g.getTransform();
    g.transform(transform);
    renderOutline(g, shape, stroke);
    g.setTransform(oldTransForm);
  }
}
