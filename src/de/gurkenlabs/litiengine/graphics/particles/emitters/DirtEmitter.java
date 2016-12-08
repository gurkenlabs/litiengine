/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles.emitters;

import java.awt.Color;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.litiengine.graphics.particles.EntityEmitter;
import de.gurkenlabs.litiengine.graphics.particles.Particle;
import de.gurkenlabs.litiengine.graphics.particles.RectangleFillParticle;
import de.gurkenlabs.litiengine.tiled.tmx.RenderType;

// TODO: Auto-generated Javadoc
/**
 * The Class DirtEmitter.
 */
@EmitterInfo(maxParticles = 3, spawnAmount = 3, emitterTTL = 100, particleMinTTL = 100, particleMaxTTL = 100)
@EntityInfo(renderType = RenderType.GROUND)
public class DirtEmitter extends EntityEmitter {

  /** The Constant REDISH_BROWN. */
  private static final Color REDISH_BROWN = new Color(159, 70, 24, 90);

  private final IMovableCombatEntity mob;

  /**
   * Instantiates a new dirt emitter.
   *
   * @param entity
   *          the entity
   */
  public DirtEmitter(final IMovableCombatEntity entity) {
    super(entity);
    this.mob = entity;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#createNewParticle()
   */
  @Override
  public Particle createNewParticle() {
    final boolean randBoolX = Math.random() >= 0.5;
    final boolean randBoolY = Math.random() >= 0.1;
    float xCoord = 0, yCoord = 0, dx = 0, dy = 0;
    switch (this.mob.getFacingDirection()) {
    case UP:
      xCoord = (float) (1 + Math.random()) * 4 * (randBoolX ? -1f : 1f);
      yCoord = (float) (2 * Math.random() + this.getEntity().getHeight() * 0.5);
      break;
    case DOWN:
      xCoord = (float) (1 + Math.random()) * 4 * (randBoolX ? -1f : 1f);
      yCoord = (float) (-2 * Math.random() + this.getEntity().getHeight() * 0.5);
      break;
    case LEFT:
      xCoord = (float) Math.random() * 8;
      yCoord = (float) (Math.random() * -4 + this.getEntity().getHeight() * 0.5);
      break;
    case RIGHT:
      xCoord = (float) Math.random() * -8;
      yCoord = (float) (Math.random() * -4 + this.getEntity().getHeight() * 0.5);
      break;
    default:
      break;
    }

    dx = (float) (Math.random() * 2 * (randBoolX ? -1f : 1f));
    dy = (float) (Math.random() * 2 * (randBoolY ? -1f : 1f));
    final float gravityX = 0.0f;
    final float gravityY = -0.2f;
    final float size = (float) (1 + Math.random() * 2);
    final int life = this.getRandomParticleTTL();

    final Particle p = new RectangleFillParticle(xCoord, yCoord, dx, dy, gravityX, gravityY, size, size, life, REDISH_BROWN);
    p.setColorAlpha(50);
    return p;
  }
}
