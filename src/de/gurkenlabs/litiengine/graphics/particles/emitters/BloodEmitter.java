/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles.emitters;

import java.awt.Color;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.particles.EntityEmitter;
import de.gurkenlabs.litiengine.graphics.particles.Particle;
import de.gurkenlabs.litiengine.graphics.particles.RectangleFillParticle;
import de.gurkenlabs.litiengine.tiled.tmx.RenderType;

// TODO: Auto-generated Javadoc
/**
 * The Class BloodEmitter.
 */
@EmitterInfo(maxParticles = 150, spawnAmount = 100, emitterTTL = 15000, particleMinTTL = 15000, particleMaxTTL = 15000)
@EntityInfo(renderType = RenderType.GROUND)
public class BloodEmitter extends EntityEmitter {

  /** The Constant MAX_MOVE_TIME. */
  private static final int MAX_MOVE_TIME = 500;

  /** The has stopped. */
  private boolean hasStopped;

  /**
   * Instantiates a new blood emitter.
   *
   * @param entity
   *          the entity
   */
  public BloodEmitter(final IEntity entity) {
    super(entity);
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

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#createNewParticle()
   */
  @Override
  public Particle createNewParticle() {
    final boolean randBoolX = Math.random() >= 0.5;
    final boolean randBoolY = Math.random() >= 0.5;

    final float xCoord = (float) (Math.random() * (this.getEntity().getWidth() * 0.25) * (randBoolX ? -1f : 1f));
    final float yCoord = (float) (Math.random() * (this.getEntity().getHeight() * 0.25) * (randBoolY ? -1f : 1f));
    final float dx = (float) (1.5 * Math.random() * (randBoolX ? -1f : 1f));
    final float dy = (float) (1.5 * Math.random() * (randBoolY ? -1f : 1f));
    final float gravityX = 0.05f * (Math.random() >= 0.5 ? -1f : 1f);
    final float gravityY = 0.05f * (Math.random() >= 0.5 ? -1f : 1f);
    final float size = (float) (2 + Math.random() * 3);
    final int life = this.getRandomParticleTTL();

    final Particle p = new RectangleFillParticle(xCoord, yCoord, dx, dy, gravityX, gravityY, size, size, life, Math.random() >= 0.5 ? Color.RED : new Color(150, 0, 0, 150));
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
}
