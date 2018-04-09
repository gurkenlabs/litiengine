package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ITimeToLive;
import de.gurkenlabs.litiengine.physics.CollisionType;

public abstract class Particle implements ITimeToLive {
  public enum ParticleRenderType {
    NONE, EMITTER, GROUND, OVERLAY
  }

  private long aliveTick;
  private long aliveTime;
  private CollisionType collisionType;
  private Color color;
  private int colorAlpha = 255;
  private float deltaHeight;
  private float deltaWidth;
  private float deltaX;
  private float deltaY;

  /**
   * The gravitational pull to the left (negative) and right (positive) acting
   * on this particle.
   */
  private float gravityX;

  /**
   * The gravitational pull to the up (negative) and down (positive) acting on
   * this particle.
   */
  private float gravityY;
  private float height;
  private final int timeToLive;
  private float width;

  /** The currentlocation of the particle on the X-axis. */
  private float x;

  /** The current location of the particle on the Y-axis. */
  private float y;

  private ParticleRenderType particleRenderType;

  private float opacity;

  private boolean fade;

  private boolean fadeOnCollision;

  private boolean colliding;

  private boolean continuousCollision;

  /**
   * Constructs a new particle.
   * 
   * @param width
   *          the width
   * @param height
   *          the height
   * @param ttl
   *          The remaining time to live of the particle.
   * @param color
   *          The color of the effect.
   */
  public Particle(final float width, final float height, final Color color, final int ttl) {
    this.setParticleRenderType(ParticleRenderType.EMITTER);
    this.setWidth(width);
    this.setHeight(height);
    this.timeToLive = ttl;
    this.color = color;
    this.colorAlpha = this.color.getAlpha();
    this.collisionType = CollisionType.NONE;
    this.opacity = 1;
    this.fade = true;
  }

  @Override
  public long getAliveTime() {
    return this.aliveTime;
  }

  public Rectangle2D getBoundingBox(final Point2D origin) {
    return new Rectangle2D.Double(origin.getX() + this.getX(), origin.getY() + this.getY(), this.getWidth(), this.getHeight());
  }

  public CollisionType getCollisionType() {
    return this.collisionType;
  }

  public Color getColor() {
    return this.color;
  }

  public int getColorAlpha() {
    return this.colorAlpha;
  }

  public float getDeltaHeight() {
    return this.deltaHeight;
  }

  public float getDeltaWidth() {
    return this.deltaWidth;
  }

  public float getDx() {
    return this.deltaX;
  }

  public float getDy() {
    return this.deltaY;
  }

  public float getGravityX() {
    return this.gravityX;
  }

  public float getGravityY() {
    return this.gravityY;
  }

  public float getHeight() {
    return this.height;
  }

  /**
   * Gets the location relative to the specified effect location.
   *
   * @param effectLocation
   *          the effect position
   * @return the location
   */
  public Point2D getRenderLocation(Point2D effectLocation) {
    // if we have a camera, we need to render the particle relative to the
    // viewport
    Point2D newEffectLocation = Game.getScreenManager() != null ? Game.getCamera().getViewPortLocation(effectLocation) : effectLocation;
    return this.getRelativeLocation(newEffectLocation);
  }

  public ParticleRenderType getParticleRenderType() {
    return particleRenderType;
  }

  @Override
  public int getTimeToLive() {
    return this.timeToLive;
  }

  public float getWidth() {
    return this.width;
  }

  public float getX() {
    return this.x;
  }

  public float getY() {
    return this.y;
  }

  public boolean isFading() {
    return this.fade;
  }

  public boolean isFadingOnCollision() {
    return this.fadeOnCollision;
  }

  public boolean isContinuousCollisionEnabled() {
    return this.continuousCollision;
  }

  public abstract void render(final Graphics2D g, final Point2D emitterOrigin);

  public Particle setCollisionType(final CollisionType collisionType) {
    this.collisionType = collisionType;
    return this;
  }

  /**
   * Enabling this check can be very performance hungry and should be used with caution and only for a small amount of particles.
   * 
   * @param ccd
   *          If set to true, the collision will be checked continuously by a ray-cast approximation.
   * @return This particle instance.
   */
  public Particle setContinuousCollision(boolean ccd) {
    this.continuousCollision = ccd;
    return this;
  }

  public Particle setColor(final Color color) {
    this.color = color;
    return this;
  }

  /**
   * Sets the color alpha. A value between 0 and 100 is expected. Otherwise it
   * won't be set.
   *
   * @param colorAlpha
   *          the new color alpha
   * 
   * @return This {@link Particle} instance to chain further setter calls.
   */
  public Particle setColorAlpha(final int colorAlpha) {
    if (colorAlpha < 0 || colorAlpha > 100) {
      return this;
    }

    this.colorAlpha = colorAlpha;
    return this;
  }

  public Particle setDeltaHeight(final float deltaHeight) {
    this.deltaHeight = deltaHeight;
    return this;
  }

  public Particle setDeltaIncX(final float gravityX) {
    this.gravityX = gravityX;
    return this;
  }

  public Particle setDeltaIncY(final float gravityY) {
    this.gravityY = gravityY;
    return this;
  }

  public Particle setDeltaWidth(final float deltaWidth) {
    this.deltaWidth = deltaWidth;
    return this;
  }

  public Particle setDeltaX(final float dx) {
    this.deltaX = dx;
    return this;
  }

  public Particle setDeltaY(final float dy) {
    this.deltaY = dy;
    return this;
  }

  public Particle setFade(boolean fade) {
    this.fade = fade;
    return this;
  }

  public Particle setFadeOnCollision(boolean fadeOnCollision) {
    this.fadeOnCollision = fadeOnCollision;
    return this;
  }

  public Particle setHeight(final float height) {
    this.height = height;
    return this;
  }

  public Particle setParticleRenderType(ParticleRenderType particleRenderType) {
    this.particleRenderType = particleRenderType;
    return this;
  }

  public Particle setWidth(final float width) {
    this.width = width;
    return this;
  }

  public Particle setX(final float x) {
    this.x = x;
    return this;
  }

  public Particle setY(final float y) {
    this.y = y;
    return this;
  }

  @Override
  public boolean timeToLiveReached() {
    return this.getTimeToLive() > 0 && this.getAliveTime() >= this.getTimeToLive();
  }

  /**
   * Updates the effect's position, change in xCurrent, change in yCurrent,
   * remaining lifetime, and color.
   * 
   * @param emitterOrigin
   *          The current {@link Emitter} origin
   * @param updateRatio
   *          The update ratio for this particle.
   */
  public void update(final Point2D emitterOrigin, final float updateRatio) {
    if (this.aliveTick == 0) {
      this.aliveTick = Game.getLoop().getTicks();
    }

    this.aliveTime = Game.getLoop().getDeltaTime(this.aliveTick);
    if (this.timeToLiveReached() || this.colliding) {
      return;
    }

    if (this.isFading()) {
      this.opacity = (float) (this.getTimeToLive() > 0 ? (this.getTimeToLive() - this.getAliveTime()) / (double) this.getTimeToLive() : 1);
    }

    final int alpha = (int) (this.getOpacity() * this.getColorAlpha());
    this.color = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), alpha >= 0 ? alpha : 0);

    // test for ray cast collision
    final float targetX = this.x + this.getDx() * updateRatio;
    final float targetY = this.y + this.getDy() * updateRatio;

    if (this.checkForCollision(emitterOrigin, targetX, targetY)) {
      return;
    }

    if (this.getDx() != 0) {
      this.x = targetX;
    }

    if (this.getDy() != 0) {
      this.y = targetY;
    }

    if (this.getGravityX() != 0) {
      this.deltaX += this.getGravityX() * updateRatio;
    }

    if (this.getGravityY() != 0) {
      this.deltaY += this.getGravityY() * updateRatio;
    }

    if (this.getDeltaWidth() != 0) {
      this.width += this.getDeltaWidth() * updateRatio;
    }

    if (this.getDeltaHeight() != 0) {
      this.height += this.getDeltaHeight() * updateRatio;
    }
  }

  private boolean checkForCollision(final Point2D emitterOrigin, float targetX, float targetY) {
    if (this.isContinuousCollisionEnabled()) {
      Point2D start = this.getRelativeLocation(emitterOrigin);
      double endX = emitterOrigin.getX() + targetX - this.getWidth() / 2.0;
      double endY = emitterOrigin.getY() + targetY - this.getHeight() / 2.0;

      Line2D ray = new Line2D.Double(start.getX(), start.getY(), endX, endY);
      if (this.getCollisionType() != CollisionType.NONE && Game.getPhysicsEngine() != null && Game.getPhysicsEngine().collides(ray, this.getCollisionType()) != null) {
        if (this.isFadingOnCollision()) {
          this.opacity = 0;
        }

        this.colliding = true;
        return true;
      }
    } else if (this.getCollisionType() != CollisionType.NONE && Game.getPhysicsEngine() != null && Game.getPhysicsEngine().collides(this.getBoundingBox(emitterOrigin), this.getCollisionType())) {
      if (this.isFadingOnCollision()) {
        this.opacity = 0;
      }

      this.colliding = true;
      return true;
    }

    return false;
  }

  public Point2D getRelativeLocation(final Point2D effectLocation) {
    return new Point2D.Float(getRelativeX(effectLocation.getX()), getRelativeY(effectLocation.getY()));
  }

  protected float getRelativeX(double effectLocationX) {
    return (float) (effectLocationX + this.getX() - this.getWidth() / 2.0);
  }

  protected float getRelativeY(double effectLocationY) {
    return (float) (effectLocationY + this.getY() - this.getHeight() / 2.0);
  }

  protected float getOpacity() {
    return this.opacity;
  }
}
