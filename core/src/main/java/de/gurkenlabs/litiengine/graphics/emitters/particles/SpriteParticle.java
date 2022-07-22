package de.gurkenlabs.litiengine.graphics.emitters.particles;

import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.AnimationController;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class SpriteParticle extends Particle {

  private AnimationController animation;
  private boolean animateSprite;
  private boolean loopSprite;
  private BufferedImage currentImage;
  private Spritesheet spritesheet;

  public SpriteParticle(final Spritesheet spritesheet) {
    super(0, 0);
    this.spritesheet = spritesheet;
    if (spritesheet == null) {
      return;
    }
    setWidth(spritesheet.getSpriteWidth());
    setHeight(spritesheet.getSpriteHeight());
    this.animation = new AnimationController(this.spritesheet);
  }

  @Override
  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = getRenderLocation(emitterOrigin);
    if (isAnimatingSprite()) {
      currentImage = animation.getCurrentImage();
    }
    Composite oldComp = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getOpacity()));
    if (getAngle() != 0) {
      ImageRenderer.renderRotated(g, currentImage, renderLocation, getAngle());
    } else {
      ImageRenderer.render(g, currentImage, renderLocation);
    }
    g.setComposite(oldComp);
  }

  @Override
  public void update(Point2D emitterOrigin, float updateRatio) {
    super.update(emitterOrigin, updateRatio);
    this.animation.update();
  }

  @Override
  public Rectangle2D getBoundingBox(final Point2D origin) {
    return new Rectangle2D.Double(
      origin.getX() + getX() - getWidth() / 2,
      origin.getY() + getY() - getHeight() / 2,
      getWidth(),
      getHeight());
  }

  public boolean isAnimatingSprite() {
    return animateSprite;
  }

  public SpriteParticle setAnimateSprite(boolean animateSprite) {
    this.animateSprite = animateSprite;
    if (!this.animateSprite) {
      this.currentImage = spritesheet.getRandomSprite();
    }
    return this;
  }

  public boolean isLoopingSprite() {
    return loopSprite;
  }

  public SpriteParticle setLoopSprite(boolean loopSprite) {
    this.loopSprite = loopSprite;
    this.animation.getDefault().setLooping(loopSprite);
    return this;
  }
}
