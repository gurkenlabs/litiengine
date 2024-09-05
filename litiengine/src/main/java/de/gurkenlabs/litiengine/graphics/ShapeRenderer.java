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
    throw new UnsupportedOperationException();
  }

  /**
   * Renders the specified {@code Shape} to the given {@code Graphics2D} object.
   *
   * @param g     The graphics object to draw on.
   * @param shape The shape to be drawn.
   */
  public static void render(final Graphics2D g, final Shape shape) {
    if (shape == null) {
      return;
    }
    g.fill(shape);
  }

  /**
   * Renders the specified {@code Shape} to the given {@code Graphics2D} object at the specified coordinates.
   *
   * @param g     The graphics object to draw on.
   * @param shape The shape to be drawn.
   * @param x     The x-coordinate of the shape.
   * @param y     The y-coordinate of the shape.
   */
  public static void render(final Graphics2D g, final Shape shape, double x, double y) {
    g.translate(x, y);
    render(g, shape);
    g.translate(-x, -y);
  }

  /**
   * Renders the specified {@code Shape} to the given {@code Graphics2D} object at the specified location.
   *
   * @param g        The graphics object to draw on.
   * @param shape    The shape to be drawn.
   * @param location The location where the shape will be drawn.
   */
  public static void render(final Graphics2D g, final Shape shape, Point2D location) {
    render(g, shape, location.getX(), location.getY());
  }

  /**
   * Renders the outline of the specified {@code Shape} to the given {@code Graphics2D} object.
   *
   * @param g     The graphics object to draw on.
   * @param shape The shape whose outline is to be drawn.
   */
  public static void renderOutline(final Graphics2D g, final Shape shape) {
    renderOutline(g, shape, DEFAULT_STROKE);
  }

  /**
   * Renders the outline of the specified {@code Shape} to the given {@code Graphics2D} object with the specified stroke width.
   *
   * @param g      The graphics object to draw on.
   * @param shape  The shape whose outline is to be drawn.
   * @param stroke The stroke width for the outline.
   */
  public static void renderOutline(final Graphics2D g, final Shape shape, final float stroke) {
    renderOutline(g, shape, new BasicStroke(stroke));
  }

  /**
   * Renders the outline of the specified {@code Shape} to the given {@code Graphics2D} object with the specified stroke.
   *
   * @param g      The graphics object to draw on.
   * @param shape  The shape whose outline is to be drawn.
   * @param stroke The stroke for the outline.
   */
  public static void renderOutline(final Graphics2D g, final Shape shape, final Stroke stroke) {
    if (shape == null) {
      return;
    }
    final Stroke oldStroke = g.getStroke();
    g.setStroke(stroke);
    g.draw(shape);
    g.setStroke(oldStroke);
  }

  /**
   * Renders the specified {@code Shape} to the given {@code Graphics2D} object with a custom affine transformation.
   *
   * @param g         The graphics object to draw on.
   * @param shape     The shape to be drawn.
   * @param transform The affine transformation to be applied to the shape.
   */
  public static void renderTransformed(
    final Graphics2D g, final Shape shape, AffineTransform transform) {

    render(g, transform.createTransformedShape(shape));
  }

  /**
   * Renders the outline of the specified {@code Shape} to the given {@code Graphics2D} object with a custom affine transformation.
   *
   * @param g         The graphics object to draw on.
   * @param shape     The shape whose outline is to be drawn.
   * @param transform The affine transformation to be applied to the shape.
   */
  public static void renderOutlineTransformed(
    final Graphics2D g, final Shape shape, AffineTransform transform) {
    renderOutlineTransformed(g, shape, transform, DEFAULT_STROKE);
  }

  /**
   * Renders the outline of the specified {@code Shape} to the given {@code Graphics2D} object with a custom affine transformation and stroke width.
   *
   * @param g         The graphics object to draw on.
   * @param shape     The shape whose outline is to be drawn.
   * @param transform The affine transformation to be applied to the shape.
   * @param stroke    The stroke width for the outline.
   */
  public static void renderOutlineTransformed(
    final Graphics2D g, final Shape shape, AffineTransform transform, final float stroke) {
    renderOutlineTransformed(g, shape, transform, new BasicStroke(stroke));
  }

  /**
   * Renders the outline of the specified {@code Shape} to the given {@code Graphics2D} object with a custom affine transformation and stroke.
   *
   * @param g         The graphics object to draw on.
   * @param shape     The shape whose outline is to be drawn.
   * @param transform The affine transformation to be applied to the shape.
   * @param stroke    The stroke for the outline.
   */
  public static void renderOutlineTransformed(
    final Graphics2D g, final Shape shape, AffineTransform transform, final Stroke stroke) {
    if (transform == null) {
      renderOutline(g, shape, stroke);
      return;
    }

    renderOutline(g, transform.createTransformedShape(shape), stroke);
  }
}
