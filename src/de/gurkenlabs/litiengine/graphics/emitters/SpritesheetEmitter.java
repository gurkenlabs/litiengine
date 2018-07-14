package de.gurkenlabs.litiengine.graphics.emitters;

import java.awt.Image;
import java.awt.geom.Point2D;
import java.util.Random;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.SpriteParticle;

public abstract class SpritesheetEmitter extends Emitter {
  private final Spritesheet spriteSheet;

  public SpritesheetEmitter(final Spritesheet spriteSheet, final Point2D origin) {
    super(origin);
    this.spriteSheet = spriteSheet;
    this.setSize(this.getSpritesheet().getSpriteWidth(), this.getSpritesheet().getSpriteHeight());

  }

  public Spritesheet getSpritesheet() {
    return this.spriteSheet;
  }

  protected Image getRandomSprite() {
    return this.getSpritesheet().getSprite(new Random().nextInt(this.getSpritesheet().getTotalNumberOfSprites()));
  }
  
  @Override
  protected Particle createNewParticle() {
    final int life = this.getRandomParticleTTL();

    final SpriteParticle p = new SpriteParticle(this.getRandomSprite(), life);
    return p;
  }
}
