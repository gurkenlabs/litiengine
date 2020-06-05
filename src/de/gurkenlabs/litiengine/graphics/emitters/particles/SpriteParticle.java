package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.graphics.ImageRenderer;

public class SpriteParticle extends Particle {
  private float angle;
  private final BufferedImage image;

  public SpriteParticle(final BufferedImage sprite) {
    super(0, 0, null);
    this.image = sprite;
    if (sprite == null) {
      return;
    }
    this.setWidth(sprite.getWidth());
    this.setHeight(sprite.getHeight());
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
      ImageRenderer.renderRotated(g, this.image, renderLocation, this.getAngle());
    } else {
      ImageRenderer.render(g, this.image, renderLocation);
    }
    g.setComposite(oldComp);
  }

  @Override
  public Rectangle2D getBoundingBox(final Point2D origin) {
    return new Rectangle2D.Double(origin.getX() + this.getX() - this.getWidth() / 2, origin.getY() + this.getY() - this.getHeight() / 2, this.getWidth(), this.getHeight());
  }

  public Particle setAngle(final float angle) {
    this.angle = angle;
    return this;
  }
}
