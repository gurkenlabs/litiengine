package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.graphics.RenderEngine;

public class RectangleOutlineParticle extends Particle {

  public RectangleOutlineParticle(final float xCurrent, final float yCurrent, final float dx, final float dy, final float gravityX, final float gravityY, final float width, final float height, final int life, final Color color) {
    super(xCurrent, yCurrent, dx, dy, gravityX, gravityY, width, height, life, color);
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(emitterOrigin);
    g.setColor(this.getColor());
    RenderEngine.drawShape(g,new Rectangle2D.Double(renderLocation.getX(), renderLocation.getY(), this.getWidth(), this.getHeight()));
  }
}
