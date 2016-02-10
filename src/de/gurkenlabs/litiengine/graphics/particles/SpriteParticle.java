package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.RenderEngine;

public class SpriteParticle extends Particle {
  private final Image image;
  private float angle;

  public SpriteParticle(final Image sprite, final float xCurrent, final float yCurrent, final float dx, final float dy, final float gravityX, final float gravityY, final int life) {
    super(xCurrent, yCurrent, dx, dy, gravityX, gravityY, (byte) 0, (byte) 0, life, Color.WHITE);
    this.image = sprite;
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(Game.getScreenManager().getCamera().getViewPortLocation(emitterOrigin));

    if (this.getAngle() != 0) {
      RenderEngine.renderImage(g, this.image, renderLocation, this.getAngle());
    } else {
      RenderEngine.renderImage(g, this.image, renderLocation);
    }
  }

  public float getAngle() {
    return this.angle;
  }

  public void setAngle(float angle) {
    this.angle = angle;
  }
}
