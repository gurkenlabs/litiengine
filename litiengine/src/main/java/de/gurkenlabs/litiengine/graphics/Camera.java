package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.litiengine.util.MathUtilities;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Camera implements ICamera {
  private final Collection<ZoomChangedListener> zoomListeners = ConcurrentHashMap.newKeySet();
  private final Collection<FocusChangedListener> focusChangedListeners =
      ConcurrentHashMap.newKeySet();

  private Point2D focus;
  private long lastShake;

  private int shakeDelay;

  private int shakeDuration = 2;
  private double shakeIntensity = 1;

  private double shakeOffsetX;
  private double shakeOffsetY;

  private long shakeTick;
  private Rectangle2D viewport;

  private float zoom;
  private float targetZoom;

  private int zoomDelay;
  private float zoomStep;

  private long zoomTick;

  private Point2D targetFocus;
  private int panTime = 0;

  private boolean clampToMap;
  private Align align = Align.LEFT;
  private Valign valign = Valign.TOP;

  /**
   * Instantiates a new {@code Camera} instance.
   */
  public Camera() {
    this.focus = new Point2D.Double();
    this.viewport = new Rectangle2D.Double();
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
    return -this.viewport.getX();
  }

  @Override
  public double getPixelOffsetY() {
    return -this.viewport.getY();
  }

  @Override
  public Rectangle2D getViewport() {
    return (Rectangle2D) this.viewport.clone();
  }

  @Override
  public Point2D getViewportDimensionCenter(final IEntity entity) {
    final Point2D viewPortLocation = this.getViewportLocation(entity);

    final IAnimationController animationController = entity.animations();
    if (animationController == null || animationController.getCurrent() == null) {
      return new Point2D.Double(viewPortLocation.getX() + entity.getWidth() * 0.5,
          viewPortLocation.getY() + entity.getHeight() * 0.5);
    }

    final Spritesheet spriteSheet = animationController.getCurrent().getSpritesheet();
    if (spriteSheet == null) {
      return viewPortLocation;
    }

    return new Point2D.Double(viewPortLocation.getX() + spriteSheet.getSpriteWidth() * 0.5,
        viewPortLocation.getY() + spriteSheet.getSpriteHeight() * 0.5);
  }

  @Override
  public Point2D getViewportLocation(final double x, final double y) {
    return new Point2D.Double(x + this.getPixelOffsetX(), y + this.getPixelOffsetY());
  }

  @Override
  public float getZoom() {
    return this.zoom;
  }

  @Override
  public void onZoom(final ZoomChangedListener listener) {
    this.zoomListeners.add(listener);
  }

  @Override
  public void removeZoomListener(ZoomChangedListener listener) {
    this.zoomListeners.remove(listener);
  }

  @Override
  public void onFocus(final FocusChangedListener listener) {
    this.focusChangedListeners.add(listener);
  }

  @Override
  public void removeFocusListener(FocusChangedListener listener) {
    this.focusChangedListeners.remove(listener);
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

    final FocusChangedEvent event = new FocusChangedEvent(this, this.focus);
    for (FocusChangedListener listener : this.focusChangedListeners) {
      listener.focusChanged(event);
    }
  }

  @Override
  public void setFocus(double x, double y) {
    this.setFocus(new Point2D.Double(x, y));
  }

  @Override
  public void setZoom(final float targetZoom, final int delay) {
    if (delay == 0) {

      this.zoom = targetZoom;
      this.targetZoom = 0;
      this.zoomDelay = 0;
      this.zoomTick = 0;
      this.zoomStep = 0;

      final ZoomChangedEvent event = new ZoomChangedEvent(this, targetZoom);
      for (final ZoomChangedListener listener : this.zoomListeners) {
        listener.zoomChanged(event);
      }
    } else {
      this.zoomTick = Game.time().now();
      this.targetZoom = targetZoom;
      this.zoomDelay = delay;

      final double tickduration = 1000 / (double) Game.loop().getTickRate();
      final double tickAmount = delay / tickduration;
      final float totalDelta = this.targetZoom - this.zoom;
      this.zoomStep = tickAmount > 0 ? (float) (totalDelta / tickAmount) : totalDelta;
    }
  }

  @Override
  public void shake(final double intensity, final int delay, final int shakeDuration) {
    this.shakeTick = Game.time().now();
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
      if (Game.time().since(this.zoomTick) >= this.zoomDelay) {
        this.zoom = this.targetZoom;
        this.targetZoom = 0;
        this.zoomDelay = 0;
        this.zoomTick = 0;
        this.zoomStep = 0;
      } else {
        this.zoom += this.zoomStep;
      }

      final ZoomChangedEvent event = new ZoomChangedEvent(this, this.getZoom());
      for (final ZoomChangedListener listener : this.zoomListeners) {
        listener.zoomChanged(event);
      }
    }

    if (this.panTime > 0) {
      if (--this.panTime <= 0) {
        this.setFocus(this.targetFocus);
        this.targetFocus = null;
      } else {
        double diff = this.panTime / (this.panTime + 1.0);
        this.focus =
            new Point2D.Double(
                this.focus.getX() * diff + this.targetFocus.getX() * (1.0 - diff),
                this.focus.getY() * diff + this.targetFocus.getY() * (1.0 - diff));
      }
    }

    if (!this.isShakeEffectActive()) {
      this.shakeOffsetX = 0;
      this.shakeOffsetY = 0;
      return;
    }

    if (Game.time().since(this.lastShake) > this.shakeDelay) {
      this.shakeOffsetX = this.getShakeIntensity() * ThreadLocalRandom.current().nextGaussian();
      this.shakeOffsetY = this.getShakeIntensity() * ThreadLocalRandom.current().nextGaussian();
      this.lastShake = Game.time().now();
    }
  }

  @Override
  public void updateFocus() {
    Point2D shook = this.applyShakeEffect(this.getFocus());

    final double viewPortX = shook.getX() - this.getViewPortCenterX();
    final double viewPortY = shook.getY() - this.getViewPortCenterY();
    this.viewport.setFrame(viewPortX, viewPortY, this.getViewportWidth(), this.getViewportHeight());
  }

  @Override
  public boolean isClampToMap() {
    return clampToMap;
  }

  @Override
  public void setClampToMap(final boolean clampToMap) {
    this.clampToMap = clampToMap;
  }

  @Override
  public void setClampAlign(Align align, Valign valign) {
    this.align = Objects.requireNonNull(align);
    this.valign = Objects.requireNonNull(valign);
  }

  @Override
  public Align getClampAlign() {
    return this.align;
  }

  @Override
  public Valign getClampValign() {
    return this.valign;
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

  // TODO: write a unit test for this
  protected Point2D clampToMap(Point2D focus) {

    if (Game.world().environment() == null
        || Game.world().environment().getMap() == null
        || !this.isClampToMap()) {
      return new Point2D.Double(focus.getX(), focus.getY());
    }

    final Dimension mapSize = Game.world().environment().getMap().getSizeInPixels();

    double minX = this.getViewportWidth() / 2.0;
    double maxX = mapSize.getWidth() - minX;
    double minY = this.getViewportHeight() / 2.0;
    double maxY = mapSize.getHeight() - minY;

    // implementation note: inside the "true" sections, min and max are effectively swapped and
    // become max and min for alignment
    double x =
        maxX < minX
            ? maxX + this.align.getValue(minX - maxX - mapSize.getWidth())
            : Math.clamp(focus.getX(), minX, maxX);
    double y =
        maxY < minY
            ? maxY + this.valign.getValue(minY - maxY - mapSize.getHeight())
            : Math.clamp(focus.getY(), minY, maxY);

    return new Point2D.Double(x, y);
  }

  protected int panTime() {
    return this.panTime;
  }

  protected double getViewportWidth() {
    return Game.window().getResolution().getWidth() / this.getRenderScale();
  }

  protected double getViewportHeight() {
    return Game.window().getResolution().getHeight() / this.getRenderScale();
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
      return new Point2D.Double(
          cameraLocation.getX() + this.shakeOffsetX, cameraLocation.getY() + this.shakeOffsetY);
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
    return this.getViewportWidth() * 0.5;
  }

  private double getViewPortCenterY() {
    return this.getViewportHeight() * 0.5;
  }

  private boolean isShakeEffectActive() {
    return this.getShakeTick() != 0
        && Game.time().since(this.getShakeTick()) < this.getShakeDuration();
  }
}
