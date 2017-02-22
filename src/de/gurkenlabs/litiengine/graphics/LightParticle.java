package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.graphics.particles.RectangleFillParticle;

public class LightParticle extends RectangleFillParticle {
  private final float maxHeight;
  private final float maxWidth;

  private final float minHeight;
  private final float minWidth;

  public LightParticle(final float maxWidth, final float maxHeight, final float xCurrent, final float yCurrent, final float dx, final float dy, final float deltaIncX, final float deltaIncY, final float width, final float height, final int life, final Color color) {
    super(xCurrent, yCurrent, dx, dy, deltaIncX, deltaIncY, width, height, life, color);
    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;
    this.minWidth = width;
    this.minHeight = height;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Particle#update()
   */
  @Override
  public void update(final IGameLoop gameLoop, final Point2D origin, final float updateRatio) {
    super.update(gameLoop, origin, updateRatio);
    if (Math.abs(this.maxWidth - this.getWidth()) < 0.1 || Math.abs(this.getWidth() - this.minWidth) < 0.1) {
      this.setDeltaWidth(-this.getDeltaWidth());
    }

    if (Math.abs(this.maxHeight - this.getHeight()) < 0.1 || Math.abs(this.getHeight() - this.minHeight) < 0.1) {
      this.setDeltaHeight(-this.getDeltaHeight());
    }
  }
}
