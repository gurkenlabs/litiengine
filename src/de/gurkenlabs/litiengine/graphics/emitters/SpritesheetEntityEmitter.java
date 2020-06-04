package de.gurkenlabs.litiengine.graphics.emitters;

import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.SpriteParticle;

public class SpritesheetEntityEmitter extends EntityEmitter {

  private final Spritesheet spriteSheet;

  public SpritesheetEntityEmitter(final Spritesheet spriteSheet, final IEntity entity) {
    this(spriteSheet, entity, false);
  }

  public SpritesheetEntityEmitter(final Spritesheet spriteSheet, final IEntity entity, final boolean dynamicLocation) {
    super(entity, dynamicLocation);
    this.spriteSheet = spriteSheet;
  }

  public Spritesheet getSpritesheet() {
    return this.spriteSheet;
  }

  protected Image getRandomSprite() {
    return this.getSpritesheet().getSprite(ThreadLocalRandom.current().nextInt(this.getSpritesheet().getTotalNumberOfSprites()));
  }

  @Override
  protected Particle createNewParticle() {
    return new SpriteParticle(this.getRandomSprite());
  }
}