/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles.emitters;

import java.awt.Color;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.Particle;
import de.gurkenlabs.litiengine.graphics.particles.RectangleFillParticle;

// TODO: Auto-generated Javadoc
/**
 * The Class SelectionEmitter.
 */
@EmitterInfo(maxParticles = 200, spawnAmount = 200, particleMinTTL = 500, particleMaxTTL = 1000)
public class SelectionEmitter extends Emitter {

  /** The Constant DARK_GRAY. */
  private static final Color DARK_GRAY = new Color(159, 159, 159, 100);

  /** The Constant DARK_GREEN. */
  private static final Color DARK_GREEN = new Color(45, 163, 73, 100);

  /** The Constant DARKISH_GRAY. */
  private static final Color DARKISH_GRAY = new Color(208, 208, 208, 100);

  /** The Constant DARKISH_GREEN. */
  private static final Color DARKISH_GREEN = new Color(207, 224, 155, 100);

  /** The Constant LIGHT_GREEN. */
  private static final Color LIGHT_GREEN = new Color(99, 237, 131, 100);

  /** The Constant LIGHTER_GRAY. */
  private static final Color LIGHTER_GRAY = new Color(230, 230, 230, 100);

  /** The Constant LIGHTER_GREEN. */
  private static final Color LIGHTER_GREEN = new Color(0, 230, 0, 100);

  /** The Constant WHITE. */
  private static final Color WHITE = new Color(255, 255, 255, 100);

  /** The height. */
  private final int height;

  /** The selected. */
  private boolean selected;

  /** The width. */
  private final int width;

  /**
   * Constructs a new Snow particle effect.
   *
   * @param originX
   *          The origin, on the X-axis, of the effect.
   * @param originY
   *          The origin, on the Y-axis, of the effect.
   * @param width
   *          the width
   * @param height
   *          the height
   */
  public SelectionEmitter(final int originX, final int originY, final int width, final int height) {
    super(originX, originY);
    this.width = width;
    this.height = height;
    this.selected = true;
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
    final boolean randBool = Math.random() >= 0.5;
    final boolean randBoolY = Math.random() >= 0.5;

    final float xCoord = (float) (Math.random() * maxAxisMovement) * (randBool ? -1f : 1f) + this.width / 2;
    final float yCoord = (float) (Math.random() * maxAxisMovement) * (randBoolY ? -1f : 1f) + this.height / 2;
    final float dx = (float) (Math.random() * 3 * (randBool ? -1f : 1f));
    final float dy = (float) (Math.random() * 4 * (randBoolY ? -1f : 1f));
    final float gravityX = 0.0f;
    final float gravityY = 0.0015f * (randBoolY ? 1f : -1f);
    final float width = (float) (4 + Math.random() * this.width / 10);
    final float height = (float) (4 + Math.random() * this.height / 10);

    this.addParticle(new RectangleFillParticle(xCoord, yCoord, dx, dy, gravityX, gravityY, width, height, life, color));
  }

  @Override
  public void setLocation(final Point2D location) {
    super.setLocation(location);
  }

  /**
   * Sets the selected.
   *
   * @param selected
   *          the new selected
   */
  public void setSelected(final boolean selected) {
    this.selected = selected;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#spawnParticle()
   */
  @Override
  protected void spawnParticle() {
    final int DARK_GRAY_COUNT = 10;
    final int DARKISH_GRAY_COUNT = 7;
    final int LIGHTER_GRAY_COUNT = 5;
    final int WHITE_COUNT = 2;
    final int NEW_PARTICLE_COUNT = DARK_GRAY_COUNT + DARKISH_GRAY_COUNT + LIGHTER_GRAY_COUNT + WHITE_COUNT;

    // only allow particle spawn if there is enough space to spawn particles in
    // all colors
    if (this.getParticles().size() > this.getMaxParticles() - NEW_PARTICLE_COUNT) {
      return;
    }

    for (byte i = 0; i < DARK_GRAY_COUNT; i++) {
      this.newParticle(this.selected ? DARK_GREEN : DARK_GRAY, this.getParticleMaxTTL(), (short) 10);
    }
    for (byte i = 0; i < DARKISH_GRAY_COUNT; i++) {
      this.newParticle(this.selected ? DARKISH_GREEN : DARKISH_GRAY, this.getRandomParticleTTL(), (short) 8);
    }
    for (byte i = 0; i < LIGHTER_GRAY_COUNT; i++) {
      this.newParticle(this.selected ? LIGHTER_GREEN : LIGHTER_GRAY, this.getRandomParticleTTL(), (short) 7);
    }
    for (byte i = 0; i < WHITE_COUNT; i++) {
      this.newParticle(this.selected ? LIGHT_GREEN : WHITE, this.getParticleMinTTL(), (short) 5);
    }
  }
}
