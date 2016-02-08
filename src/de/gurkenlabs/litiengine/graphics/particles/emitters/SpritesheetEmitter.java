package de.gurkenlabs.litiengine.graphics.particles.emitters;

import java.awt.Image;
import java.awt.geom.Point2D;
import java.util.Random;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;

public abstract class SpritesheetEmitter extends Emitter {
  private final Spritesheet spriteSheet;

  public SpritesheetEmitter(final Spritesheet spriteSheet, final Point2D origin) {
    super(origin);
    this.spriteSheet = spriteSheet;
  }

  public Spritesheet getSpritesheet() {
    return this.spriteSheet;
  }

  protected Image getRandomSprite() {
    return this.getSpritesheet().getSprite(new Random().nextInt(this.getSpritesheet().getTotalNumberOfSprites()));
  }
}
