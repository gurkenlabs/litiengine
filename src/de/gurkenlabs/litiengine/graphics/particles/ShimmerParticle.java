/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

// TODO: Auto-generated Javadoc
/**
 * The Class ShimmerParticle.
 */
public class ShimmerParticle extends Particle {

  /** The bounding box. */
  private final Rectangle2D boundingBox;

  /**
   * Instantiates a new shimmer particle.
   *
   * @param boundingBox
   *          the bounding box
   * @param xCurrent
   *          the x current
   * @param yCurrent
   *          the y current
   * @param dx
   *          the dx
   * @param dy
   *          the dy
   * @param gravityX
   *          the gravity x
   * @param gravityY
   *          the gravity y
   * @param width
   *          the width
   * @param height
   *          the height
   * @param life
   *          the life
   * @param color
   *          the color
   * @param particleType
   *          the particle type
   */
  public ShimmerParticle(final Rectangle2D boundingBox, final float xCurrent, final float yCurrent, final float dx, final float dy, final float gravityX, final float gravityY, final byte width, final byte height, final int life, final Color color) {
    super(xCurrent, yCurrent, dx, dy, gravityX, gravityY, width, height, life, color);
    this.boundingBox = boundingBox;
  }

  /**
   * Gets the bounding box.
   *
   * @return the bounding box
   */
  public Rectangle2D getBoundingBox() {
    return this.boundingBox;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Particle#update()
   */
  @Override
  public void update() {
    super.update();
    final Random rand = new Random();

    final Point2D emitterLocation = new Point2D.Double(this.getBoundingBox().getX(), this.getBoundingBox().getY());
    final Point2D relativeParticleLocation = this.getLocation(emitterLocation);
    if (relativeParticleLocation.getX() < this.getBoundingBox().getX()) {
      this.setDx(rand.nextFloat());
    }

    if (relativeParticleLocation.getX() > this.getBoundingBox().getX() + this.getBoundingBox().getWidth()) {
      this.setDx(-rand.nextFloat());
    }

    if (relativeParticleLocation.getY() < this.getBoundingBox().getY()) {
      this.setDy(rand.nextFloat());
    }

    if (relativeParticleLocation.getY() > this.getBoundingBox().getY() + this.getBoundingBox().getHeight()) {
      this.setDy(-rand.nextFloat());
    }
  }

}
