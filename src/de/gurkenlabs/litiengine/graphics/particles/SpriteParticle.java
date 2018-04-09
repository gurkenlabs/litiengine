package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.graphics.RenderEngine;

public class SpriteParticle extends Particle {
  private float angle;
  private final Image image;

  public SpriteParticle(final Image sprite, final int ttl) {
    super(0, 0, Color.WHITE, ttl);
    this.image = sprite;
  }

  public float getAngle() {
    return this.angle;
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getRenderLocation(emitterOrigin);

    Composite oldComp = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.getOpacity()));
    if (this.getAngle() != 0) {
      RenderEngine.renderImage(g, this.image, renderLocation, this.getAngle());
    } else {
      RenderEngine.renderImage(g, this.image, renderLocation);
    }
    g.setComposite(oldComp);
  }

  public Particle setAngle(final float angle) {
    this.angle = angle;
    return this;
  }
}
