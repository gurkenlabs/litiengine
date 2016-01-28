/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * The Class Camera.
 */
public abstract class Camera implements ICamera {

  private Point2D focus;

  /** The shake duration. */
  private int shakeDuration = 2;

  /** The shake intensity. */
  private int shakeIntensity = 1;

  /** The shake tick. */
  private long shakeTick;

  /**
   * Instantiates a new camera.
   */
  protected Camera() {
  }

  @Override
  public Point2D getFocus() {
    return this.focus;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.graphics.ICamera#getMapLocation(java.awt.geom.Point2D)
   */
  @Override
  public Point2D getMapLocation(final Point2D viewPortLocation) {
    final double x = viewPortLocation.getX() - this.getPixelOffsetX();
    final double y = viewPortLocation.getY() - this.getPixelOffsetY();
    return new Point2D.Double(x, y);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getPixelOffsetX()
   */
  @Override
  public double getPixelOffsetX() {
    return this.getCenterX() - this.getFocus().getX();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getPixelOffsetY()
   */
  @Override
  public double getPixelOffsetY() {
    return this.getCenterY() - this.getFocus().getY();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getRenderLocation(double, double)
   */
  @Override
  public Point2D getViewPortLocation(final double x, final double y) {
    return this.getViewPortLocation(new Point2D.Double(x, y));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.graphics.ICamera#getRenderLocation(de.gurkenlabs.liti.
   * entities.Entity)
   */
  @Override
  public Point2D getViewPortLocation(final IEntity entity) {
    // localplayer camera causes flickering and bouncing of the sprite
    if (entity.getAnimationController() != null && entity.getAnimationController().getCurrentAnimation() != null) {
      final Spritesheet spriteSheet = entity.getAnimationController().getCurrentAnimation().getSpritesheet();
      final Point2D location = new Point2D.Double(entity.getLocation().getX() - (spriteSheet.getSpriteWidth() - entity.getWidth()) / 2.0, entity.getLocation().getY() - (spriteSheet.getSpriteHeight() - entity.getHeight()) / 2.0);
      return this.getViewPortLocation(location);
    }

    return this.getViewPortLocation(entity.getLocation());
  }
  
  @Override
  public Point2D getViewPortDimensionCenter(final IEntity entity) {
    final Point2D viewPortLocation = this.getViewPortLocation(entity);
    if (entity.getAnimationController() == null || entity.getAnimationController().getCurrentAnimation() == null) {
      return new Point2D.Double(viewPortLocation.getX() + entity.getWidth() / 2, viewPortLocation.getY() + entity.getHeight() / 2);
    }

    final Spritesheet spriteSheet = entity.getAnimationController().getCurrentAnimation().getSpritesheet();
    return new Point2D.Double(viewPortLocation.getX() + spriteSheet.getSpriteWidth() / 2, viewPortLocation.getY() + spriteSheet.getSpriteHeight() / 2);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getRenderLocation(java.awt.geom.
   * Point2D)
   */
  @Override
  public Point2D getViewPortLocation(final Point2D mapLocation) {
    final double x = mapLocation.getX() + this.getPixelOffsetX();
    final double y = mapLocation.getY() + this.getPixelOffsetY();
    return new Point2D.Double(x, y);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#shake(int, int)
   */
  @Override
  public void shake(final int intensity, final int shakeDuration) {
    this.shakeTick = Game.getTicks();
    this.shakeIntensity = intensity;
    this.shakeDuration = shakeDuration;
  }

  /**
   * Apply shake effect.
   *
   * @param cameraLocation
   *          the camera location
   * @return the point2 d
   */
  protected Point2D applyShakeEffect(final Point2D cameraLocation) {
    final boolean rnd = Math.random() > 0.5;
    if (this.getShakeTick() != 0 && Game.getDeltaTime(this.getShakeTick()) < this.getShakeDuration()) {
      return new Point2D.Double(cameraLocation.getX() + this.getShakeOffset() * (rnd ? -1 : 1), cameraLocation.getY() + this.getShakeOffset() * (rnd ? -1 : 1));
    }

    return cameraLocation;
  }

  /**
   * Gets the shake duration.
   *
   * @return the shake duration
   */
  protected int getShakeDuration() {
    return this.shakeDuration;
  }

  /**
   * Gets the shake offset.
   *
   * @return the shake offset
   */
  protected int getShakeOffset() {
    return this.shakeIntensity;
  }

  /**
   * Gets the shake tick.
   *
   * @return the shake tick
   */
  protected long getShakeTick() {
    return this.shakeTick;
  }

  protected void setFocus(final Point2D focus) {
    this.focus = focus;
  }
}