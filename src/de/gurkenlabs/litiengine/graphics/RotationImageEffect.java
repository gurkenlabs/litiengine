package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class RotationImageEffect extends ImageEffect {
  private final float angle;

  public RotationImageEffect(int ttl, float angle) {
    super(ttl);
    this.angle = angle;
  }

  @Override
  public BufferedImage apply(BufferedImage image) {
    int size = Math.max(image.getWidth(), image.getHeight()) * 2;
    final BufferedImage img = RenderEngine.createCompatibleImage(size, size);
    final Graphics g = img.createGraphics();
    RenderEngine.renderImage(g, image, new Point2D.Double(0, 0), this.getAngle());
    g.dispose();

    return img;
  }

  public float getAngle() {
    return this.angle;
  }
}
