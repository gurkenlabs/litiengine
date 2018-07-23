package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class RightLineParticle extends Particle {

  public RightLineParticle(final float width, final float height, final Color color, final int ttl) {
    super(width, height, color, ttl);
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getRenderLocation(emitterOrigin);
    g.setColor(this.getColor());
    g.draw(new Line2D.Double(renderLocation.getX(), renderLocation.getY(), renderLocation.getX() + this.getWidth(), renderLocation.getY() + this.getHeight()));
  }
}
