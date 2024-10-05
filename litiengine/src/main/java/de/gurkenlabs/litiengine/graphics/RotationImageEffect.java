package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.util.Imaging;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class RotationImageEffect extends ImageEffect {
  private final float angle;

  /**
   * Initializes a new instance of the {@code RotationImageEffect}.
   *
   * @param ttl
   *          The time to live of this effect.
   * @param angle
   *          The angle by which this effect rotates the base image.
   */
  public RotationImageEffect(final int ttl, final float angle) {
    super(ttl, "RotationImageEffect_" + angle);
    this.angle = angle;
  }

  /**
   * Applies a rotation transformation to the provided {@link BufferedImage}.
   *
   * This method rotates the given image by a specified angle, calculates the
   * new dimensions needed to accommodate the rotated image without cropping,
   * and creates a new compatible {@link BufferedImage} with those dimensions.
   *
   * @param image the {@link BufferedImage} to be rotated.
   *              If {@code null}, the method returns {@code null}.
   *
   * @return a new {@link BufferedImage} representing the rotated image,
   *         or {@code null} if the input image was {@code null}.
   *
   */
  @Override
  public BufferedImage apply(final BufferedImage image) {
    if (image == null) {
      return null;
    }

    // Calculate the new dimensions after rotation
    double radians = Math.toRadians(angle);
    int width = image.getWidth();
    int height = image.getHeight();

    // Calculate the new width and height
    int newWidth = (int) Math.abs(width * Math.cos(radians)) + (int) Math.abs(height * Math.sin(radians));
    int newHeight = (int) Math.abs(height * Math.cos(radians)) + (int) Math.abs(width * Math.sin(radians));

    final BufferedImage rotatedImage = Imaging.getCompatibleImage(newWidth, newHeight);
    final Graphics2D g = rotatedImage.createGraphics();
    ImageRenderer.renderRotated(g, image, new Point2D.Double(0, 0), this.getAngle());
    g.dispose();

    return rotatedImage;
  }

  public double getAngle() {
    return this.angle;
  }
}
