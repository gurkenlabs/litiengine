/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles.emitters;

import java.awt.Color;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.Particle;
import de.gurkenlabs.litiengine.graphics.particles.RectangleFillParticle;

/**
 * The Class HitBloodEmitter.
 */
@EmitterInfo(maxParticles = 20, spawnAmount = 20, emitterTTL = 20000, particleMinTTL = 10000, particleMaxTTL = 20000)
public class HitEmitter extends Emitter {
  private static final int MAX_MOVE_TIME = 500;
  /** The hit color. */
  private final Color hitColor;

  /** The has stopped. */
  private boolean hasStopped;

  /**
   * Instantiates a new hit blood emitter.
   *
   * @param entity
   *          the entity
   * @param color
   *          the color
   */
  public HitEmitter(final ICombatEntity entity, final Color color) {
    super(entity.getDimensionCenter());
    this.hitColor = color;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#createNewParticle()
   */
  @Override
  public Particle createNewParticle() {
    final float dx = (float) (1.2 * Math.random() * (Math.random() >= 0.5 ? -1f : 1f));
    final float dy = (float) (1.2 * Math.random() * (Math.random() >= 0.5 ? -1f : 1f));
    final float gravityX = 0.05f * (Math.random() >= 0.5 ? -1f : 1f);
    final float gravityY = 0.05f * (Math.random() >= 0.5 ? -1f : 1f);
    final float size = (float) (3 + Math.random() * 3);
    final int life = this.getRandomParticleTTL();

    final Particle p = new RectangleFillParticle(0, 0, dx, dy, gravityX, gravityY, size, size, life, this.hitColor);
    p.setColorAlpha(50);
    return p;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#update()
   */
  @Override
  public void update(final IGameLoop loop) {
    super.update(loop);
    if (this.getAliveTime() >= MAX_MOVE_TIME) {
      this.getParticles().forEach(particle -> particle.setDx(0));
      this.getParticles().forEach(particle -> particle.setDy(0));
      this.hasStopped = true;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#canTakeNewParticles()
   */
  @Override
  protected boolean canTakeNewParticles() {
    return !this.hasStopped && super.canTakeNewParticles();
  }
}
