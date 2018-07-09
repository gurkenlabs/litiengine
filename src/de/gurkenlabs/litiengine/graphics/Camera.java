package de.gurkenlabs.litiengine.graphics;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.litiengine.util.MathUtilities;

public class Camera implements IUpdateable {
  private final List<Consumer<Float>> zoomChangedConsumer;
  private final List<Consumer<Point2D>> focusChangedConsumer;
  /**
   * Provides the center location for the viewport.
   */
  protected Point2D focus;
  private long lastShake;

  private int shakeDelay;

  private int shakeDuration = 2;
  private double shakeIntensity = 1;

  private double shakeOffsetX;
  private double shakeOffsetY;

  private long shakeTick;
  private Rectangle2D viewPort;

  private float zoom;
  private float targetZoom;

  private int zoomDelay;
  private float zoomStep;

  private long zoomTick;

  private boolean clampToMap;

  /**
   * Instantiates a new camera.
   */
  public Camera() {
    this.zoomChangedConsumer = new CopyOnWriteArrayList<>();
    this.focusChangedConsumer = new CopyOnWriteArrayList<>();
    this.focus = new Point2D.Double(0, 0);
    this.viewPort = new Rectangle2D.Double(0, 0, 0, 0);
    this.zoom = 1;
  }

  /**
   * Gets the map location that is focused by this camera.
   *
   * @return the focus map location
   */
  public Point2D getFocus() {
    return this.focus;
  }

  /**
   * Gets the map location.
   *
   * @param viewPortLocation
   *          the point
   * @return the map location
   */
  public Point2D getMapLocation(final Point2D viewPortLocation) {
    final double x = viewPortLocation.getX() - this.getPixelOffsetX();
    final double y = viewPortLocation.getY() - this.getPixelOffsetY();
    return new Point2D.Double(x, y);
  }

  /**
   * Gets the pixel offset x.
   *
   * @return the pixel offset x
   */
  public double getPixelOffsetX() {
    return this.getViewPortCenterX() - (this.getFocus() != null ? this.getFocus().getX() : 0);
  }

  /**
   * Gets the pixel offset y.
   *
   * @return the pixel offset y
   */
  public double getPixelOffsetY() {
    return this.getViewPortCenterY() - (this.getFocus() != null ? this.getFocus().getY() : 0);
  }

  /**
   * Gets the camera region.
   *
   * @return the camera region
   */
  public Rectangle2D getViewPort() {
    return this.viewPort;
  }

  public Point2D getViewPortDimensionCenter(final IEntity entity) {
    final Point2D viewPortLocation = this.getViewPortLocation(entity);

    final IAnimationController animationController = entity.getAnimationController();
    if (animationController == null || animationController.getCurrentAnimation() == null) {
      return new Point2D.Double(viewPortLocation.getX() + entity.getWidth() * 0.5, viewPortLocation.getY() + entity.getHeight() * 0.5);
    }

    final Spritesheet spriteSheet = animationController.getCurrentAnimation().getSpritesheet();
    if (spriteSheet == null) {
      return viewPortLocation;
    }

    return new Point2D.Double(viewPortLocation.getX() + spriteSheet.getSpriteWidth() * 0.5, viewPortLocation.getY() + spriteSheet.getSpriteHeight() * 0.5);
  }

  /**
   * Gets the render location.
   *
   * @param x
   *          the x
   * @param y
   *          the y
   * @return the render location
   */
  public Point2D getViewPortLocation(final double x, final double y) {
    return new Point2D.Double(x + this.getPixelOffsetX(), y + this.getPixelOffsetY());
  }

  /**
   * This method calculates to location for the specified entity in relation to
   * the focus map location of the camera.
   *
   * @param entity
   *          the entity
   * @return the render location
   */
  public Point2D getViewPortLocation(final IEntity entity) {
    return this.getViewPortLocation(entity.getLocation());
  }

  /**
   * This method calculates to location for the specified point in relation to
   * the focus map location of the camera.
   *
   * @param mapLocation
   *          the point
   * @return the render location
   */
  public Point2D getViewPortLocation(final Point2D mapLocation) {
    return this.getViewPortLocation(mapLocation.getX(), mapLocation.getY());
  }

  public float getZoom() {
    return this.zoom;
  }

  public float getRenderScale() {
    return Game.getRenderEngine().getBaseRenderScale() * this.getZoom();
  }

  public void onZoomChanged(final Consumer<Float> zoomCons) {
    this.zoomChangedConsumer.add(zoomCons);
  }

  public void onFocusChanged(Consumer<Point2D> focusCons) {
    this.focusChangedConsumer.add(focusCons);
  }

  public void setFocus(final Point2D focus) {
    this.focus = this.clampToMap(focus);
    
    // dunno why but without the factor of 0.01 sometimes everything starts to
    // get wavy while rendering ...
    // it seems to be an issue with the focus location being exactly dividable
    // by up to 4?? (maybe even more for higher renderscales)
    // this is somehow related to the rendering scale: if the rendering scale is
    // lower this will only be affected by lower dividable numbers (e.g.
    // renderscale of 6 only has an issue with 1 and 0.5)
    // seems like java cannot place certain images onto their exact pixel
    // location with an AffineTransform...
    final double fraction = this.focus.getY() - Math.floor(this.focus.getY());
    if (MathUtilities.isInt(fraction * 4)) {
      this.focus.setLocation(this.focus.getX(), this.focus.getY() + 0.01);
    }
    
    for (Consumer<Point2D> consumer : this.focusChangedConsumer) {
      consumer.accept(this.focus);
    }
  }

  public void setFocus(double x, double y) {
    this.setFocus(new Point2D.Double(x, y));
  }

  public void setZoom(final float targetZoom, final int delay) {
    if (delay == 0) {
      for (final Consumer<Float> cons : this.zoomChangedConsumer) {
        cons.accept(targetZoom);
      }

      this.zoom = targetZoom;
      this.targetZoom = 0;
      this.zoomDelay = 0;
      this.zoomTick = 0;
      this.zoomStep = 0;
    } else {
      this.zoomTick = Game.getLoop().getTicks();
      this.targetZoom = targetZoom;
      this.zoomDelay = delay;

      final double tickduration = 1000 / (double) Game.getLoop().getUpdateRate();
      final double tickAmount = delay / tickduration;
      final float totalDelta = this.targetZoom - this.zoom;
      this.zoomStep = tickAmount > 0 ? (float) (totalDelta / tickAmount) : totalDelta;
    }
  }

  public void shake(final double intensity, final int delay, final int shakeDuration) {
    this.shakeTick = Game.getLoop().getTicks();
    this.shakeDelay = delay;
    this.shakeIntensity = intensity;
    this.shakeDuration = shakeDuration;
  }

  @Override
  public void update() {
    if (Game.getCamera() != null && !Game.getCamera().equals(this)) {
      return;
    }

    if (this.targetZoom > 0) {
      if (Game.getLoop().getDeltaTime(this.zoomTick) >= this.zoomDelay) {
        for (final Consumer<Float> cons : this.zoomChangedConsumer) {
          cons.accept(this.zoom);
        }

        this.zoom = this.targetZoom;
        this.targetZoom = 0;
        this.zoomDelay = 0;
        this.zoomTick = 0;
        this.zoomStep = 0;
      } else {

        this.zoom += this.zoomStep;
        for (final Consumer<Float> cons : this.zoomChangedConsumer) {
          cons.accept(this.zoom);
        }
      }
    }

    if (!this.isShakeEffectActive()) {
      this.shakeOffsetX = 0;
      this.shakeOffsetY = 0;
      return;
    }

    if (Game.getLoop().getDeltaTime(this.lastShake) > this.shakeDelay) {
      this.shakeOffsetX = this.getShakeIntensity() * MathUtilities.randomSign();
      this.shakeOffsetY = this.getShakeIntensity() * MathUtilities.randomSign();
      this.lastShake = Game.getLoop().getTicks();
    }
  }

  public void updateFocus() {
    this.setFocus(this.applyShakeEffect(this.getFocus()));

    final double viewPortX = this.getFocus().getX() - this.getViewPortCenterX();
    final double viewPortY = this.getFocus().getY() - this.getViewPortCenterY();
    this.viewPort = new Rectangle2D.Double(viewPortX, viewPortY, Game.getScreenManager().getResolution().getWidth() / this.getRenderScale(), Game.getScreenManager().getResolution().getHeight() / this.getRenderScale());
  }

  public boolean isClampToMap() {
    return clampToMap;
  }

  public void setClampToMap(boolean clampToMap) {
    this.clampToMap = clampToMap;
  }

  protected Point2D clampToMap(Point2D focus) {
    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null || !this.isClampToMap()) {
      return focus;
    }

    final Dimension mapSize = Game.getEnvironment().getMap().getSizeInPixels();
    final Dimension resolution = Game.getScreenManager().getResolution();

    double minX = resolution.getWidth() / this.getRenderScale() / 2.0;
    double maxX = mapSize.getWidth() - minX;
    double minY = resolution.getHeight() / this.getRenderScale() / 2.0;
    double maxY = mapSize.getHeight() - minY;

    double x = mapSize.getWidth() * this.getRenderScale() < resolution.getWidth() ? minX : MathUtilities.clamp(focus.getX(), minX, maxX);
    double y = mapSize.getHeight() * this.getRenderScale() < resolution.getHeight() ? minY : MathUtilities.clamp(focus.getY(), minY, maxY);

    return new Point2D.Double(x, y);
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

  private double getViewPortCenterX() {
    return Game.getScreenManager().getResolution().getWidth() * 0.5 / this.getRenderScale();
  }

  private double getViewPortCenterY() {
    return Game.getScreenManager().getResolution().getHeight() * 0.5 / this.getRenderScale();
  }

  private boolean isShakeEffectActive() {
    return this.getShakeTick() != 0 && Game.getLoop().getDeltaTime(this.getShakeTick()) < this.getShakeDuration();
  }
}