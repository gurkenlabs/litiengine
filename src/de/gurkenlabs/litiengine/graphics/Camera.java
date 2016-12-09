/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.util.MathUtilities;

/**
 * The Class Camera.
 */
public class Camera implements ICamera {

  /**
   * Provides the center location for the viewport.
   */
  private Point2D focus;

  /** The shake duration. */
  private int shakeDuration = 2;

  /** The shake intensity. */
  private double shakeIntensity = 1;

  /** The shake tick. */
  private long shakeTick;

  private Rectangle2D viewPort;

  /**
   * Instantiates a new camera.
   */
  public Camera() {
    this.focus = new Point2D.Double(0, 0);
  }

  /**
   * Apply shake effect.
   *
   * @param cameraLocation
   *          the camera location
   * @return the point2 d
   */
  protected Point2D applyShakeEffect(final Point2D cameraLocation) {
    if (this.getShakeTick() != 0 && Game.getLoop().getDeltaTime(this.getShakeTick()) < this.getShakeDuration()) {
      return new Point2D.Double(cameraLocation.getX() + this.getShakeOffset() * MathUtilities.randomSign(), cameraLocation.getY() + this.getShakeOffset() * MathUtilities.randomSign());
    }

    return cameraLocation;
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
    return this.getViewPortCenterX() - (this.getFocus() != null ? this.getFocus().getX() : 0);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getPixelOffsetY()
   */
  @Override
  public double getPixelOffsetY() {
    return this.getViewPortCenterY() - (this.getFocus() != null ? this.getFocus().getY() : 0);
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
  protected double getShakeOffset() {
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

  @Override
  public Point2D getViewPortDimensionCenter(final IEntity entity) {
    final Point2D viewPortLocation = this.getViewPortLocation(entity);
    if (entity.getAnimationController() == null || entity.getAnimationController().getCurrentAnimation() == null) {
      return new Point2D.Double(viewPortLocation.getX() + entity.getWidth() * 0.5, viewPortLocation.getY() + entity.getHeight() * 0.5);
    }

    final Spritesheet spriteSheet = entity.getAnimationController().getCurrentAnimation().getSpritesheet();
    return new Point2D.Double(viewPortLocation.getX() + spriteSheet.getSpriteWidth() * 0.5, viewPortLocation.getY() + spriteSheet.getSpriteHeight() * 0.5);
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
      final Point2D location = new Point2D.Double(entity.getLocation().getX() - (spriteSheet.getSpriteWidth() - entity.getWidth()) * 0.5, entity.getLocation().getY() - (spriteSheet.getSpriteHeight() - entity.getHeight()) * 0.5);
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
    return this.getViewPortLocation(mapLocation.getX(), mapLocation.getY());
  }
  
  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getRenderLocation(double, double)
   */
  @Override
  public Point2D getViewPortLocation(final double x, final double y) {
    return new Point2D.Double( x + this.getPixelOffsetX(), y + this.getPixelOffsetY());
  }
  
  @Override
  public void setFocus(final Point2D focus) {
    this.focus = focus;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#shake(int, int)
   */
  @Override
  public void shake(final double intensity, final int shakeDuration) {
    this.shakeTick = Game.getLoop().getTicks();
    this.shakeIntensity = intensity;
    this.shakeDuration = shakeDuration;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getCameraRegion()
   */
  @Override
  public Rectangle2D getViewPort() {
    return this.viewPort;
  }

  @Override
  public void updateFocus() {
    this.viewPort = new Rectangle2D.Double(this.getFocus().getX() - this.getViewPortCenterX(), this.getFocus().getY() - this.getViewPortCenterY(), Game.getScreenManager().getResolution().getWidth() / Game.getInfo().getRenderScale(),
        Game.getScreenManager().getResolution().getHeight() / Game.getInfo().getRenderScale());
  }

  private double getViewPortCenterX() {
    return Game.getScreenManager().getResolution().getWidth() * 0.5 / Game.getInfo().getRenderScale();
  }

  private double getViewPortCenterY() {
    return Game.getScreenManager().getResolution().getHeight() * 0.5 / Game.getInfo().getRenderScale();
  }
}