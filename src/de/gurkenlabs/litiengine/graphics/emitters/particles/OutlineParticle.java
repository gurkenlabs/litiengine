package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.RenderEngine;

public abstract class OutlineParticle extends ShapeParticle {
  private float stroke = 1.0f / Game.graphics().getBaseRenderScale();

  public OutlineParticle(float width, float height, Color color, int ttl) {
    super(width, height, color, ttl);
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    g.setColor(this.getColor());

    RenderEngine.renderOutline(g, this.getShape(emitterOrigin), new BasicStroke(this.getStroke()));
  }

  public float getStroke() {
    return this.stroke;
  }

  public void setStroke(final float stroke) {
    this.stroke = stroke;
  }
}