package de.gurkenlabs.litiengine.graphics;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.litiengine.util.MathUtilities;

public class Camera implements ICamera {
  private final List<DoubleConsumer> zoomChangedConsumer = new CopyOnWriteArrayList<>();
  private final List<Consumer<Point2D>> focusChangedConsumer = new CopyOnWriteArrayList<>();
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

  private Point2D targetFocus;
  private int panTime = 0;

  // TODO: implement possiblity to provide a padding
  private boolean clampToMap;

  /**
   * Instantiates a new camera.
   */
  public Camera() {
    this.focus = new Point2D.Double();
    this.viewPort = new Rectangle2D.Double();
    this.zoom = 1;
  }

  @Override
  public Point2D getFocus() {
    return this.focus;
  }

  @Override
  public Point2D getMapLocation(final Point2D viewPortLocation) {
    final double x = viewPortLocation.getX() - this.getPixelOffsetX();
    final double y = viewPortLocation.getY() - this.getPixelOffsetY();
    return new Point2D.Double(x, y);
  }

  @Override
  public double getPixelOffsetX() {
    return this.getViewPortCenterX() - (this.getFocus() != null ? this.getFocus().getX() : 0);
  }

  @Override
  public double getPixelOffsetY() {
    return this.getViewPortCenterY() - (this.getFocus() != null ? this.getFocus().getY() : 0);
  }

  @Override
  public Rectangle2D getViewport() {
    return this.viewPort;
  }

  @Override
  public Point2D getViewportDimensionCenter(final IEntity entity) {
    final Point2D viewPortLocation = this.getViewportLocation(entity);

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

  @Override
  public Point2D getViewportLocation(final double x, final double y) {
    return new Point2D.Double(x + this.getPixelOffsetX(), y + this.getPixelOffsetY());
  }

  @Override
  public Point2D getViewportLocation(final IEntity entity) {
    return this.getViewportLocation(entity.getLocation());
  }

  @Override
  public Point2D getViewportLocation(final Point2D mapLocation) {
    return this.getViewportLocation(mapLocation.getX(), mapLocation.getY());
  }

  @Override
  public float getZoom() {
    return this.zoom;
  }

  @Override
  public float getRenderScale() {
    return Game.graphics().getBaseRenderScale() * this.getZoom();
  }

  @Override
  public void onZoomChanged(final DoubleConsumer zoomCons) {
    this.zoomChangedConsumer.add(zoomCons);
  }

  @Override
  public void onFocusChanged(Consumer<Point2D> focusCons) {
    this.focusChangedConsumer.add(focusCons);
  }

  @Override
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

  @Override
  public void setFocus(double x, double y) {
    this.setFocus(new Point2D.Double(x, y));
  }

  @Override
  public void setZoom(final float targetZoom, final int delay) {
    if (delay == 0) {
      for (final DoubleConsumer cons : this.zoomChangedConsumer) {
        cons.accept(targetZoom);
      }

      this.zoom = targetZoom;
      this.targetZoom = 0;
      this.zoomDelay = 0;
      this.zoomTick = 0;
      this.zoomStep = 0;
    } else {
      this.zoomTick = Game.loop().getTicks();
      this.targetZoom = targetZoom;
      this.zoomDelay = delay;

      final double tickduration = 1000 / (double) Game.loop().getUpdateRate();
      final double tickAmount = delay / tickduration;
      final float totalDelta = this.targetZoom - this.zoom;
      this.zoomStep = tickAmount > 0 ? (float) (totalDelta / tickAmount) : totalDelta;
    }
  }

  @Override
  public void shake(final double intensity, final int delay, final int shakeDuration) {
    this.shakeTick = Game.loop().getTicks();
    this.shakeDelay = delay;
    this.shakeIntensity = intensity;
    this.shakeDuration = shakeDuration;
  }

  @Override
  public void update() {
    if (Game.world().camera() != null && !Game.world().camera().equals(this)) {
      return;
    }

    if (this.targetZoom > 0) {
      if (Game.loop().getDeltaTime(this.zoomTick) >= this.zoomDelay) {
        for (final DoubleConsumer cons : this.zoomChangedConsumer) {
          cons.accept(this.zoom);
        }

        this.zoom = this.targetZoom;
        this.targetZoom = 0;
        this.zoomDelay = 0;
        this.zoomTick = 0;
        this.zoomStep = 0;
      } else {

        this.zoom += this.zoomStep;
        for (final DoubleConsumer cons : this.zoomChangedConsumer) {
          cons.accept(this.zoom);
        }
      }
    }

    if (this.panTime > 0) {
      if (--this.panTime <= 0) {
        this.setFocus(this.targetFocus);
        this.targetFocus = null;

        for (Consumer<Point2D> cons : this.focusChangedConsumer) {
          cons.accept(focus);
        }
      } else {
        double diff = this.panTime / (this.panTime + 1.0);
        this.focus = new Point2D.Double(this.focus.getX() * diff + this.targetFocus.getX() * (1.0 - diff),
            this.focus.getY() * diff + this.targetFocus.getY() * (1.0 - diff));
      }
    }

    if (!this.isShakeEffectActive()) {
      this.shakeOffsetX = 0;
      this.shakeOffsetY = 0;
      return;
    }

    if (Game.loop().getDeltaTime(this.lastShake) > this.shakeDelay) {
      this.shakeOffsetX = this.getShakeIntensity() * MathUtilities.randomSign();
      this.shakeOffsetY = this.getShakeIntensity() * MathUtilities.randomSign();
      this.lastShake = Game.loop().getTicks();
    }
  }

  @Override
  public void updateFocus() {
    this.setFocus(this.applyShakeEffect(this.getFocus()));

    final double viewPortX = this.getFocus().getX() - this.getViewPortCenterX();
    final double viewPortY = this.getFocus().getY() - this.getViewPortCenterY();
    this.viewPort = new Rectangle2D.Double(viewPortX, viewPortY, Game.window().getResolution().getWidth() / this.getRenderScale(), Game.window().getResolution().getHeight() / this.getRenderScale());
  }

  @Override
  public boolean isClampToMap() {
    return clampToMap;
  }

  @Override
  public void setClampToMap(final boolean clampToMap) {
    this.clampToMap = clampToMap;
  }

  // TODO: write a unit test for this
  protected Point2D clampToMap(Point2D focus) {

    if (Game.world().environment() == null || Game.world().environment().getMap() == null || !this.isClampToMap()) {
      return new Point2D.Double(focus.getX(), focus.getY());
    }

    final Dimension mapSize = Game.world().environment().getMap().getSizeInPixels();

    // TODO: Implement special handling for maps that are smaller than the camera area: use Align, Valign to determine where to render them
    final Dimension resolution = Game.window().getResolution();
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
    return Game.window().getResolution().getWidth() * 0.5 / this.getRenderScale();
  }

  private double getViewPortCenterY() {
    return Game.window().getResolution().getHeight() * 0.5 / this.getRenderScale();
  }

  private boolean isShakeEffectActive() {
    return this.getShakeTick() != 0 && Game.loop().getDeltaTime(this.getShakeTick()) < this.getShakeDuration();
  }

  @Override
  public void pan(Point2D focus, int duration) {
    this.targetFocus = this.clampToMap(focus);
    this.panTime = duration;
  }

  @Override
  public void pan(double x, double y, int duration) {
    this.pan(new Point2D.Double(x, y), duration);
  }
}