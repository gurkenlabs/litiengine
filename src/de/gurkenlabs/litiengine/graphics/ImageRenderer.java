package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public final class ImageRenderer {

  private ImageRenderer() {
  }
  

  public static void renderImage(final Graphics2D g, final Image image, final double x, final double y) {
    if (image == null) {
      return;
    }

    final AffineTransform t = AffineTransform.getTranslateInstance(x, y);
    g.drawImage(image, t, null);
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
  public static void renderImage(final Graphics2D g, final Image image, final double x, final double y, final double angle) {
    if (image == null) {
      return;
    }

    if (angle == 0 || angle % 360 == 0) {
      renderImage(g, image, x, y);
      return;
    }

    final AffineTransform t = new AffineTransform();

    t.translate(x, y);
    t.rotate(Math.toRadians(angle), image.getWidth(null) * 0.5, image.getHeight(null) * 0.5);

    g.drawImage(image, t, null);
  }

  public static void renderScaledImage(final Graphics2D g, final Image image, final double x, final double y, final double scale) {
    renderScaledImage(g, image, x, y, scale, scale);
  }

  public static void renderScaledImage(final Graphics2D g, final Image image, final Point2D location, final double scale) {
    renderScaledImage(g, image, location.getX(), location.getY(), scale, scale);
  }

  public static void renderScaledImage(final Graphics2D g, final Image image, final Point2D location, final double scaleX, final double scaleY) {
    renderScaledImage(g, image, location.getX(), location.getY(), scaleX, scaleY);
  }

  public static void renderScaledImage(final Graphics2D g, final Image image, final double x, final double y, final double scaleX, final double scaleY) {
    if (image == null) {
      return;
    }

    if (scaleX == 1 && scaleY == 1) {
      renderImage(g, image, x, y);
      return;
    }

    final AffineTransform t = new AffineTransform();

    t.translate(x, y);
    t.scale(scaleX, scaleY);

    g.drawImage(image, t, null);
  }

  public static void renderImage(final Graphics2D g, final Image image, final Point2D renderLocation) {
    renderImage(g, image, renderLocation.getX(), renderLocation.getY());
  }

  public static void renderImage(final Graphics2D g, final Image image, final Point2D renderLocation, final double angle) {
    renderImage(g, image, renderLocation.getX(), renderLocation.getY(), angle);
  }

  public static void renderImage(final Graphics2D g, final Image image, final Point2D renderLocation, AffineTransform transform) {
    renderImage(g, image, renderLocation.getX(), renderLocation.getY(), transform);
  }

  public static void renderImage(final Graphics2D g, final Image image, double x, double y, AffineTransform transform) {
    if (transform == null) {
      renderImage(g, image, x, y);
      return;
    }

    AffineTransform t = new AffineTransform();
    t.translate(x, y);
    t.concatenate(transform);

    g.drawImage(image, t, null);
  }

  public static void renderImage(final Graphics2D g, final Image image, AffineTransform transform) {
    if (transform == null) {
      return;
    }

    g.drawImage(image, transform, null);
  }

}
