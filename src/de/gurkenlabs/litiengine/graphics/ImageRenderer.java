package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * This static implementation renders an <code>Image</code> to a given <code>Graphics2D</code> object at the specified screen coordinates.
 * This class be very useful when composing a GUI that contains images which are rendered at a certain location on the screen.
 * 
 * @see Image
 * @see Graphics2D
 */
public final class ImageRenderer {

  private ImageRenderer() {
    throw new UnsupportedOperationException();
  }

  public static void render(final Graphics2D g, final Image image, final double x, final double y) {
    if (image == null) {
      return;
    }

    final AffineTransform t = AffineTransform.getTranslateInstance(x, y);
    g.drawImage(image, t, null);
  }

  public static void render(final Graphics2D g, final Image image, final Point2D renderLocation) {
    render(g, image, renderLocation.getX(), renderLocation.getY());
  }

  /***
   * Note that rotating an image with 90/180/270 degree is way more performant.
   * than rotating with in other degrees.
   * 
   * @param g
   *          The graphics object to draw on.
   * @param image
   *          The image to be drawn
   * @param x
   *          The x-coordinate of the image.
   * @param y
   *          The y-coordinate of the image
   * @param angle
   *          The angle by which the image will be rotated.s
   */
  public static void renderRotated(final Graphics2D g, final Image image, final double x, final double y, final double angle) {
    if (image == null) {
      return;
    }

    if (angle == 0 || angle % 360 == 0) {
      render(g, image, x, y);
      return;
    }

    final AffineTransform t = new AffineTransform();

    t.translate(x, y);
    t.rotate(Math.toRadians(angle), image.getWidth(null) * 0.5, image.getHeight(null) * 0.5);

    g.drawImage(image, t, null);
  }

  public static void renderRotated(final Graphics2D g, final Image image, final Point2D renderLocation, final double angle) {
    renderRotated(g, image, renderLocation.getX(), renderLocation.getY(), angle);
  }

  public static void renderScaled(final Graphics2D g, final Image image, final double x, final double y, final double scale) {
    renderScaled(g, image, x, y, scale, scale);
  }

  public static void renderScaled(final Graphics2D g, final Image image, final Point2D location, final double scale) {
    renderScaled(g, image, location.getX(), location.getY(), scale, scale);
  }

  public static void renderScaled(final Graphics2D g, final Image image, final Point2D location, final double scaleX, final double scaleY) {
    renderScaled(g, image, location.getX(), location.getY(), scaleX, scaleY);
  }

  public static void renderScaled(final Graphics2D g, final Image image, final double x, final double y, final double scaleX, final double scaleY) {
    if (image == null) {
      return;
    }

    if (scaleX == 1 && scaleY == 1) {
      render(g, image, x, y);
      return;
    }

    final AffineTransform t = new AffineTransform();

    t.translate(x, y);
    t.scale(scaleX, scaleY);

    g.drawImage(image, t, null);
  }

  public static void renderTransformed(final Graphics2D g, final Image image, final Point2D renderLocation, AffineTransform transform) {
    renderTransformed(g, image, renderLocation.getX(), renderLocation.getY(), transform);
  }

  public static void renderTransformed(final Graphics2D g, final Image image, double x, double y, AffineTransform transform) {
    if (transform == null) {
      render(g, image, x, y);
      return;
    }

    AffineTransform t = new AffineTransform();
    t.translate(x, y);
    t.concatenate(transform);

    g.drawImage(image, t, null);
  }

  public static void renderTransformed(final Graphics2D g, final Image image, AffineTransform transform) {
    if (transform == null) {
      return;
    }

    g.drawImage(image, transform, null);
  }
}