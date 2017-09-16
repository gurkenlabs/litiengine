package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class OvalOutlineParticle extends Particle {
  private Stroke stroke;

  public OvalOutlineParticle(final float width, final float height, final Color color, final int ttl) {
    super(width, height, color, ttl);
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(emitterOrigin);
    g.setColor(this.getColor());
    Stroke oldStroke = g.getStroke();
    g.setStroke(this.getStroke());
    g.draw(new Ellipse2D.Double(renderLocation.getX(), renderLocation.getY(), this.getWidth(), this.getHeight()));
    g.setStroke(oldStroke);
  }

  public Stroke getStroke() {
    return this.stroke;
  }

  public void setStroke(Stroke stroke) {
    this.stroke = stroke;
  }
}
