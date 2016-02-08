package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;

public class RectangleOutlineParticle extends Particle{

  public RectangleOutlineParticle(float xCurrent, float yCurrent, float dx, float dy, float gravityX, float gravityY, float width, float height, int life, Color color) {
    super(xCurrent, yCurrent, dx, dy, gravityX, gravityY, width, height, life, color);
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(Game.getScreenManager().getCamera().getViewPortLocation(emitterOrigin));
    g.setColor(this.getColor());
    g.draw(new Rectangle2D.Double(renderLocation.getX(), renderLocation.getY(), this.getWidth(), this.getHeight()));
  }
}
