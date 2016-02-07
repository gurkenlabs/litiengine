/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles.emitters;

import java.awt.Color;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.particles.EntityEmitter;
import de.gurkenlabs.litiengine.graphics.particles.Particle;

// TODO: Auto-generated Javadoc
/**
 * The Class GlowEmitter.
 */
@EmitterInfo(maxParticles = 40, spawnAmount = 40, particleMinTTL = 100, particleMaxTTL = 400)
public class GlowEmitter extends EntityEmitter {

  /** The Constant DEFAULT_COLOR. */
  private static final Color DEFAULT_COLOR = new Color(255, 0, 0, 50);

  /** The colors. */
  private final Color[] colors = new Color[] { DEFAULT_COLOR };

  /**
   * Instantiates a new glow emitter.
   *
   * @param entity
   *          the entity
   */
  public GlowEmitter(final IEntity entity) {
    super(entity);
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

    final float xCoord = (float) Math.random() * 10 * (randBoolX ? -1f : 1f);
    final float yCoord = (float) Math.random() * 10 * (randBoolY ? -1f : 1f);
    final float dx = (float) (Math.random() * (randBoolX ? -1f : 1f));
    final float dy = (float) (Math.random() * (randBoolY ? -1f : 1f));
    final float gravityX = 0.01f * (randBoolY ? -1f : 1f);
    final float gravityY = 0.01f * (randBoolX ? -1f : 1f);
    final byte size = (byte) (4 + Math.random() * 5);
    final int life = this.getRandomParticleTTL();

    Color color = DEFAULT_COLOR;
    if (this.colors.length > 0) {
      final int randomColorIndex = (int) (Math.random() * this.colors.length);
      color = this.colors[randomColorIndex];
    }

    final Particle p = new Particle(xCoord, yCoord, dx, dy, gravityX, gravityY, size, size, life, color);
    p.setColorAlpha(50);
    return p;
  }
}
