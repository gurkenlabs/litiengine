/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles.emitters;

import java.awt.Color;
import java.util.Random;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.Particle;
import de.gurkenlabs.litiengine.graphics.particles.ShimmerParticle;

// TODO: Auto-generated Javadoc
/**
 * The Class ShimmerEmitter.
 */
@EntityInfo(width = 64, height = 64)
@EmitterInfo(maxParticles = 10, spawnAmount = 10, spawnRate = 10, activateOnInit = false)
public class ShimmerEmitter extends Emitter {

  /**
   * Instantiates a new shimmer emitter.
   *
   * @param originX
   *          the origin x
   * @param originY
   *          the origin y
   */
  public ShimmerEmitter(final int originX, final int originY) {
    super(originX, originY);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Emitter#createNewParticle()
   */
  @Override
  public Particle createNewParticle() {
    final Random rand = new Random();

    final float xCoord = rand.nextInt((int) this.getWidth());
    final float yCoord = rand.nextInt((int) this.getHeight());
    final float randX = rand.nextFloat();
    final float dx = Math.random() >= 0.5 ? -randX : randX;
    final float dy = Math.random() >= 0.5 ? -randX : randX;
    final float gravityX = 0.0f;
    final float gravityY = 0.0f;
    final byte size = (byte) (rand.nextInt(3) + 2);
    final short life = (short) 0;

    Color color = new Color(255, 255, 255, (int) Math.round(Math.random() * 155) + 100);
    if (rand.nextFloat() > 0.5) {
      color = new Color(170, 255, 255, (int) Math.round(Math.random() * 155) + 100);
    }
    final Particle p = new ShimmerParticle(this.getBoundingBox(), xCoord, yCoord, dx, dy, gravityX, gravityY, size, size, life, color);
    return p;
  }
}