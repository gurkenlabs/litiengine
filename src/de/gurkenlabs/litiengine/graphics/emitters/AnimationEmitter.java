package de.gurkenlabs.litiengine.graphics.emitters;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.EmitterInfo;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.EntityAnimationController;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;

/**
 * This class provides the possibility to spawn an animation similar to a usual
 * emitter. It once plays an animation based on the specified spriteSheet and
 * afterwards it disposes itself from the environment just like an emitter
 * would.
 * 
 * The time to live is set by the total duration of the animation.
 */
@EmitterInfo(maxParticles = 0, spawnAmount = 0)
public class AnimationEmitter extends SpritesheetEmitter {

  public AnimationEmitter(Spritesheet spriteSheet, Point2D origin) {
    super(spriteSheet, origin);
    this.setWidth(spriteSheet.getSpriteWidth());
    this.setHeight(spriteSheet.getSpriteHeight());
    EntityAnimationController<AnimationEmitter> animationController = new EntityAnimationController<>(this, spriteSheet, false);
    this.setTimeToLive(animationController.getDefault().getTotalDuration());
    this.getControllers().addController(animationController);
  }

  @Override
  protected Particle createNewParticle() {
    return null;
  }
}
