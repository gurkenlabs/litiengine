package com.litiengine.graphics.emitters.particles;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.litiengine.graphics.animation.AnimationController;
import com.litiengine.graphics.ImageRenderer;
import com.litiengine.graphics.Spritesheet;

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
    this.setWidth(spritesheet.getSpriteWidth());
    this.setHeight(spritesheet.getSpriteHeight());
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
    return new Rectangle2D.Double(origin.getX() + this.getX() - this.getWidth() / 2, origin.getY() + this.getY() - this.getHeight() / 2, this.getWidth(), this.getHeight());
  }

  public boolean isAnimatingSprite() {
    return animateSprite;
  }

  public void setAnimateSprite(boolean animateSprite) {
    this.animateSprite = animateSprite;
    if (!this.animateSprite) {
      this.currentImage = spritesheet.getRandomSprite();
    }
  }

  public boolean isLoopingSprite() {
    return loopSprite;
  }

  public void setLoopSprite(boolean loopSprite) {
    this.loopSprite = loopSprite;
    this.animation.getDefault().setLooping(loopSprite);
  }

}
