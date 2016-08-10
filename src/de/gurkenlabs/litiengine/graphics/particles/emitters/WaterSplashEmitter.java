/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles.emitters;

import java.awt.Color;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.OvalParticle;
import de.gurkenlabs.litiengine.graphics.particles.Particle;

// TODO: Auto-generated Javadoc
/**
 * Represents a fire.
 */

@EntityInfo(width = 10, height = 10)
@EmitterInfo(maxParticles = 80, spawnAmount = 20, activateOnInit = false, particleMinTTL = 300, particleMaxTTL = 400)
public class WaterSplashEmitter extends Emitter {
  /** One of the four colors used by the fire particles. */
  private static final Color DARK_GREY = new Color(111, 111, 111, 60);
  /** One of the four colors used by the fire particles. */
  private static final Color BRIGHT_GREY = new Color(182, 182, 182, 70);
  /** One of the four colors used by the fire particles. */
  private static final Color DARK_WHITE = new Color(216, 216, 216, 80);
  /** One of the four colors used by the fire particles. */
  private static final Color WHITE = new Color(230, 230, 230, 90);

  /**
   * Constructs a new Snow particle effect.
   *
   * @param originX
   *          The origin, on the X-axis, of the effect.
   * @param originY
   *          The origin, on the Y-axis, of the effect.
   */
  public WaterSplashEmitter(final int originX, final int originY) {
    super(originX, originY);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#createNewParticle()
   */
  @Override
  protected Particle createNewParticle() {
    return null;
  }

  /**
   * Creates a new Particle object.
   *
   * @param color
   *          the color
   * @param life
   *          The number of movements before the new Particle decays.
   * @param maxAxisMovement
   *          the max axis movement
   */
  public void newParticle(final Color color, final int life, final short maxAxisMovement) {

    final float xCoord = this.randomWithRange((float) this.getLocation().getX(), (float) this.getLocation().getX() + this.getWidth());
    final float yCoord = this.randomWithRange((float) this.getLocation().getY(), (float) this.getLocation().getY() + this.getHeight());
    final float dx = this.randomWithRange(-0.7f, 0.7f);
    final float dy = this.randomWithRange(-0.03f, 0.07f);
    final float gravityX = this.randomWithRange(-0.07f, 0.07f);
    final float gravityY = this.randomWithRange(0.03f, 0.07f);
    final byte size = (byte) (Math.random() * 4);

    this.addParticle(new OvalParticle(xCoord, yCoord, dx, dy, gravityX, gravityY, size, size, life, color));
  }

  float randomWithRange(final float min, final float max) {
    final float range = max - min;
    return (float) (Math.random() * range) + min;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#spawnParticle()
   */
  @Override
  protected void spawnParticle() {
    final int DARKGREY_COUNT = 2;
    final int BRIGHTGREY_COUNT = 5;
    final int DARKWHITE_COUNT = 7;
    final int WHITE_COUNT = 10;
    final int NEW_PARTICLE_COUNT = DARKGREY_COUNT + BRIGHTGREY_COUNT + DARKWHITE_COUNT + WHITE_COUNT;

    // only allow particle spawn if there is enough space to spawn particles in
    // all colors
    if (this.getParticles().size() > this.getMaxParticles() - NEW_PARTICLE_COUNT) {
      return;
    }

    for (byte i = 0; i < DARKGREY_COUNT; i++) {
      this.newParticle(DARK_GREY, this.getParticleMinTTL(), (short) 10);
    }
    for (byte i = 0; i < BRIGHTGREY_COUNT; i++) {
      this.newParticle(BRIGHT_GREY, this.getRandomParticleTTL(), (short) 8);
    }
    for (byte i = 0; i < DARKWHITE_COUNT; i++) {
      this.newParticle(DARK_WHITE, this.getRandomParticleTTL(), (short) 7);
    }
    for (byte i = 0; i < WHITE_COUNT; i++) {
      this.newParticle(WHITE, this.getParticleMaxTTL(), (short) 5);
    }
  }
}