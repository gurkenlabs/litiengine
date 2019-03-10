package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.util.Imaging;

public class RotationImageEffect extends ImageEffect {
  private final float angle;

  public RotationImageEffect(final int ttl, final float angle) {
    super(ttl, "RotationImageEffect");
    this.angle = angle;
  }

  @Override
  public BufferedImage apply(final BufferedImage image) {
    if (image == null) {
      return null;
    }

    final int size = Math.max(image.getWidth(), image.getHeight()) * 2;
    final BufferedImage img = Imaging.getCompatibleImage(size, size);
    final Graphics2D g = img.createGraphics();
    ImageRenderer.renderRotated(g, image, new Point2D.Double(0, 0), this.getAngle());
    g.dispose();

    return img;
  }

  public double getAngle() {
    return this.angle;
  }
}
