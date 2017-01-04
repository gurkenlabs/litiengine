/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.util.MathUtilities;

/**
 * The Class Camera.
 */
public class Camera implements ICamera, IUpdateable {

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

  private int shakeDelay;
  private long lastShake;

  private double shakeOffsetX;
  private double shakeOffsetY;

  private float zoom;
  private int zoomDelay;
  private long zoomTick;
  private float zoomStep;

  private Rectangle2D viewPort;

  /**
   * Instantiates a new camera.
   */
  public Camera() {
    this.focus = new Point2D.Double(0, 0);
    Game.getLoop().registerForUpdate(this);
  }

  @Override
  public void update(IGameLoop loop) {
    if (Game.getInfo().getRenderScale() != this.zoom && this.zoom > 0) {
      if (loop.getDeltaTime(this.zoomTick) > this.zoomDelay) {
        Game.getInfo().setRenderScale(this.zoom);
        this.zoom = 0;
        this.zoomDelay = 0;
      } else {

        float newRenderScale = Game.getInfo().getRenderScale() + this.zoomStep;
        Game.getInfo().setRenderScale(newRenderScale);
      }
    }

    if (!this.isShakeEffectActive()) {
      this.shakeOffsetX = 0;
      this.shakeOffsetY = 0;
      return;
    }

    if (loop.getDeltaTime(this.lastShake) > shakeDelay) {
      this.shakeOffsetX = this.getShakeIntensity() * MathUtilities.randomSign();
      this.shakeOffsetY = this.getShakeIntensity() * MathUtilities.randomSign();
      this.lastShake = loop.getTicks();
    }
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
    return new Point2D.Double(x + this.getPixelOffsetX(), y + this.getPixelOffsetY());
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
  public void shake(final double intensity, final int delay, final int shakeDuration) {
    this.shakeTick = Game.getLoop().getTicks();
    this.shakeDelay = delay;
    this.shakeIntensity = intensity;
    this.shakeDuration = shakeDuration;
  }

  @Override
  public void setZoom(float zoom, int delay) {
    this.zoomTick = Game.getLoop().getTicks();
    this.zoom = zoom;
    this.zoomDelay = delay;

    double tickduration = 1000 / Game.getLoop().getUpdateRate();
    double tickAmount = delay / tickduration;
    float totalDelta = zoom - Game.getInfo().getRenderScale();
    this.zoomStep = tickAmount > 0 ? (float) (totalDelta / tickAmount) : totalDelta;
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
    this.focus = this.applyShakeEffect(this.focus);
    this.viewPort = new Rectangle2D.Double(this.getFocus().getX() - this.getViewPortCenterX(), this.getFocus().getY() - this.getViewPortCenterY(), Game.getScreenManager().getResolution().getWidth() / Game.getInfo().getRenderScale(),
        Game.getScreenManager().getResolution().getHeight() / Game.getInfo().getRenderScale());
  }

  /**
   * Gets the shake duration.
   *
   * @return the shake duration
   */
  private int getShakeDuration() {
    return this.shakeDuration;
  }

  /**
   * Gets the shake offset.
   *
   * @return the shake offset
   */
  private double getShakeIntensity() {
    return this.shakeIntensity;
  }

  /**
   * Gets the shake tick.
   *
   * @return the shake tick
   */
  private long getShakeTick() {
    return this.shakeTick;
  }

  /**
   * Apply shake effect.
   *
   * @param cameraLocation
   *          the camera location
   * @return the point2 d
   */
  private Point2D applyShakeEffect(final Point2D cameraLocation) {
    if (this.isShakeEffectActive()) {
      return new Point2D.Double(cameraLocation.getX() + this.shakeOffsetX, cameraLocation.getY() + this.shakeOffsetY);
    }

    return cameraLocation;
  }

  private boolean isShakeEffectActive() {
    return this.getShakeTick() != 0 && Game.getLoop().getDeltaTime(this.getShakeTick()) < this.getShakeDuration();
  }

  private double getViewPortCenterX() {
    return Game.getScreenManager().getResolution().getWidth() * 0.5 / Game.getInfo().getRenderScale();
  }

  private double getViewPortCenterY() {
    return Game.getScreenManager().getResolution().getHeight() * 0.5 / Game.getInfo().getRenderScale();
  }
}