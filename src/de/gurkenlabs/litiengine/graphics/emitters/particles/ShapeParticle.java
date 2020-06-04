package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;

public abstract class ShapeParticle extends Particle {

  public ShapeParticle(float width, float height, Color color) {
    super(width, height, color);
  }

  protected abstract Shape getShape(final Point2D emitterOrigin);

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    g.setColor(this.getColor());
    if (this.isOutlineOnly()) {
      Game.graphics().renderOutline(g, this.getShape(emitterOrigin), new BasicStroke(1.0f / Game.graphics().getBaseRenderScale()));
    } else
      Game.graphics().renderShape(g, this.getShape(emitterOrigin));
  }
}
