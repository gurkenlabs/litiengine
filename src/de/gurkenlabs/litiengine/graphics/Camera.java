/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.core.IGameLoop;
import de.gurkenlabs.litiengine.core.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * The Class Camera.
 */
public abstract class Camera implements ICamera, IUpdateable {

  private final IGameLoop gameLoop;
  private Point2D focus;

  /** The shake duration. */
  private int shakeDuration = 2;

  /** The shake intensity. */
  private int shakeIntensity = 1;

  /** The shake tick. */
  private int shakeTick;

  /** The update count. */
  private int updateCount;

  /**
   * Instantiates a new camera.
   */
  protected Camera(final IGameLoop gameLoop) {
    this.gameLoop = gameLoop;
    this.gameLoop.registerForUpdate(this);
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
    this.shakeTick = this.updateCount;
    this.shakeIntensity = intensity;
    this.shakeDuration = shakeDuration;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.core.IUpdateable#update()
   */
  @Override
  public void update() {
    this.updateCount++;
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
    if (this.getShakeTick() != 0 && this.gameLoop.convertToMs(this.getUpdateCount() - this.getShakeTick()) < this.getShakeDuration()) {
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
  protected int getShakeTick() {
    return this.shakeTick;
  }

  /**
   * Gets the update count.
   *
   * @return the update count
   */
  protected int getUpdateCount() {
    return this.updateCount;
  }

  protected void setFocus(final Point2D focus) {
    this.focus = focus;
  }
}