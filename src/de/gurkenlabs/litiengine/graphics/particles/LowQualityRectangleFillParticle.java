package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

public class LowQualityRectangleFillParticle extends Particle {

  public LowQualityRectangleFillParticle(final float xCurrent, final float yCurrent, final float dx, final float dy, final float deltaIncX, final float deltaIncY, final float width, final float height, final int life, final Color color) {
    super(xCurrent, yCurrent, dx, dy, deltaIncX, deltaIncY, width, height, life, color);
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(emitterOrigin);
    g.setColor(this.getColor());
    g.fillRect((int) renderLocation.getX(), (int) renderLocation.getY(), (int) this.getWidth(), (int) this.getHeight());
  }
}