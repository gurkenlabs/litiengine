package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class LeftLineParticle extends Particle {

  public LeftLineParticle(final float xCurrent, final float yCurrent, final float dx, final float dy, final float gravityX, final float gravityY, final float width, final float height, final int life, final Color color) {
    super(xCurrent, yCurrent, dx, dy, gravityX, gravityY, width, height, life, color);
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(emitterOrigin);
    g.setColor(this.getColor());
    g.draw(new Line2D.Double(renderLocation.getX() + this.getWidth(), renderLocation.getY(), renderLocation.getX(), renderLocation.getY() + this.getHeight()));
  }
}
