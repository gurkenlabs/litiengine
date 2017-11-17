package de.gurkenlabs.litiengine.graphics;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.util.MathUtilities;

/**
 * The Class Camera.
 */
public class Camera implements ICamera {
  private final List<Consumer<Float>> zoomChangedConsumer;
  private final List<Consumer<Point2D>> focusChangedConsumer;
  /**
   * Provides the center location for the viewport.
   */
  protected Point2D focus;
  private long lastShake;

  private int shakeDelay;

  /** The shake duration. */
  private int shakeDuration = 2;

  /** The shake intensity. */
  private double shakeIntensity = 1;

  private double shakeOffsetX;
  private double shakeOffsetY;

  /** The shake tick. */
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
    this.zoom = 1;
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
  public Point2D getViewPortDimensionCenter(final IEntity entity) {
    final Point2D viewPortLocation = this.getViewPortLocation(entity);

    final IAnimationController animationController = Game.getEntityControllerManager().getAnimationController(entity);
    if (animationController == null || animationController.getCurrentAnimation() == null) {
      return new Point2D.Double(viewPortLocation.getX() + entity.getWidth() * 0.5, viewPortLocation.getY() + entity.getHeight() * 0.5);
    }

    final Spritesheet spriteSheet = animationController.getCurrentAnimation().getSpritesheet();
    return new Point2D.Double(viewPortLocation.getX() + spriteSheet.getSpriteWidth() * 0.5, viewPortLocation.getY() + spriteSheet.getSpriteHeight() * 0.5);
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
    final IAnimationController animationController = Game.getEntityControllerManager().getAnimationController(entity);
    if (animationController != null && animationController.getCurrentAnimation() != null && animationController.getCurrentAnimation().getSpritesheet() != null) {
      final Spritesheet spriteSheet = animationController.getCurrentAnimation().getSpritesheet();
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

  @Override
  public float getZoom() {
    return this.zoom;
  }

  @Override
  public float getRenderScale() {
    return Game.getInfo().getDefaultRenderScale() * this.getZoom();
  }

  @Override
  public void onZoomChanged(final Consumer<Float> zoomCons) {
    this.zoomChangedConsumer.add(zoomCons);
  }

  @Override
  public void onFocusChanged(Consumer<Point2D> focusCons) {
    this.focusChangedConsumer.add(focusCons);
  }

  @Override
  public void setFocus(final Point2D focus) {
    // dunno why but without the factor of 0.01 sometimes everything starts to
    // get wavy while rendering ...
    // it seems to be an issue with the focus location being exactly dividable
    // by up to 4?? (maybe even more for higher renderscales)
    // this is somehow related to the rendering scale: if the rendering scale is
    // lower this will only be affected by lower dividable numbers (e.g.
    // renderscale of 6 only has an issue with 1 and 0.5)
    // seems like java cannot place certain images onto their exact pixel
    // location with an AffineTransform...
    final double fraction = focus.getY() - Math.floor(focus.getY());
    if (MathUtilities.isInt(fraction * 4)) {
      focus.setLocation(focus.getX(), focus.getY() + 0.01);
    }

    this.focus = this.clampToMap(focus);
    for (Consumer<Point2D> consumer : this.focusChangedConsumer) {
      consumer.accept(this.focus);
    }
  }

  @Override
  public void setFocus(double x, double y) {
    this.setFocus(new Point2D.Double(x, y));
  }

  @Override
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

  @Override
  public void updateFocus() {
    this.setFocus(this.applyShakeEffect(this.getFocus()));
    final double viewPortY = this.getFocus().getY() - this.getViewPortCenterY();
    this.viewPort = new Rectangle2D.Double(this.getFocus().getX() - this.getViewPortCenterX(), viewPortY, Game.getScreenManager().getResolution().getWidth() / this.getRenderScale(), Game.getScreenManager().getResolution().getHeight() / this.getRenderScale());
  }

  @Override
  public boolean isClampToMap() {
    return clampToMap;
  }

  @Override
  public void setClampToMap(boolean clampToMap) {
    this.clampToMap = clampToMap;
  }

  protected Point2D clampToMap(Point2D focus) {
    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null || !this.isClampToMap()) {
      return focus;
    }

    final Dimension mapSize = Game.getEnvironment().getMap().getSizeInPixels();
    final Dimension resolution = Game.getScreenManager().getResolution();

    double minX = resolution.getWidth() / this.getRenderScale() / 2;
    double maxX = mapSize.getWidth() - minX;
    double minY = resolution.getHeight() / this.getRenderScale() / 2;
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