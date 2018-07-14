package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.Color;
import java.awt.geom.Point2D;

public class LightParticle extends RectangleFillParticle {
  private final float maxHeight;
  private final float maxWidth;

  private final float minHeight;
  private final float minWidth;

  public LightParticle(final float maxWidth, final float maxHeight, final float width, final float height, final Color color, final int life) {
    super(width, height, color, life);
    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;
    this.minWidth = width;
    this.minHeight = height;
  }

  @Override
  public void update(final Point2D origin, final float updateRatio) {
    super.update(origin, updateRatio);
    if (Math.abs(this.maxWidth - this.getWidth()) < 0.1 || Math.abs(this.getWidth() - this.minWidth) < 0.1) {
      this.setDeltaWidth(-this.getDeltaWidth());
    }

    if (Math.abs(this.maxHeight - this.getHeight()) < 0.1 || Math.abs(this.getHeight() - this.minHeight) < 0.1) {
      this.setDeltaHeight(-this.getDeltaHeight());
    }
  }
}
