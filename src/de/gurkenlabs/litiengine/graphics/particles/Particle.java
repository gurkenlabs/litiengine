package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.ITimeToLive;

public abstract class Particle implements ITimeToLive {
  public enum ParticleRenderType {
    NONE, EMITTER, GROUND, OVERLAY
  }

  /** The activation tick. */
  private long aliveTick;
  private long aliveTime;
  private boolean applyPhysics;
  private int collisionType;
  /** The color of the particle. */
  private Color color;
  /** The color alpha. */
  private int colorAlpha = 255;

  private float deltaHeight;

  private float deltaWidth;

  /** The change in X, per update, of the particle. */
  private float dx;

  /** The change in Y, per update, of the particle. */
  private float dy;

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
  /** The height. */
  private float height;

  private final int timeToLive;

  /** The width. */
  private float width;

  /** The currentlocation of the particle on the X-axis. */
  private float x;

  /** The current location of the particle on the Y-axis. */
  private float y;

  private ParticleRenderType particleRenderType;

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
  }

  @Override
  public long getAliveTime() {
    return this.aliveTime;
  }

  public Rectangle2D getBoundingBox(final Point2D origin) {
    return new Rectangle2D.Double(origin.getX() + this.getX(), origin.getY() + this.getY(), this.getWidth(), this.getHeight());
  }

  public int getCollisionType() {
    return this.collisionType;
  }

  /**
   * Gets the color.
   *
   * @return the color
   */
  public Color getColor() {
    return this.color;
  }

  /**
   * Gets the color alpha.
   *
   * @return the color alpha
   */
  public int getColorAlpha() {
    return this.colorAlpha;
  }

  public float getDeltaHeight() {
    return this.deltaHeight;
  }

  public float getDeltaWidth() {
    return this.deltaWidth;
  }

  /**
   * Gets the dx.
   *
   * @return the dx
   */
  public float getDx() {
    return this.dx;
  }

  /**
   * Gets the dy.
   *
   * @return the dy
   */
  public float getDy() {
    return this.dy;
  }

  /**
   * Gets the gravity x.
   *
   * @return the gravity x
   */
  public float getGravityX() {
    return this.gravityX;
  }

  /**
   * Gets the gravity y.
   *
   * @return the gravity y
   */
  public float getGravityY() {
    return this.gravityY;
  }

  /**
   * Gets the height.
   *
   * @return the height
   */
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
  public Point2D getLocation(Point2D effectLocation) {
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

  /**
   * Gets the width.
   *
   * @return the width
   */
  public float getWidth() {
    return this.width;
  }

  /**
   * Gets the x current.
   *
   * @return the x current
   */
  public float getX() {
    return this.x;
  }

  /**
   * Gets the y current.
   *
   * @return the y current
   */
  public float getY() {
    return this.y;
  }

  public boolean isApplyingPhysics() {
    return this.applyPhysics;
  }

  public abstract void render(final Graphics2D g, final Point2D emitterOrigin);

  public Particle setApplyPhysics(final boolean applyStaticPhysics) {
    this.applyPhysics = applyStaticPhysics;
    return this;
  }

  public Particle setCollisionType(final int collisionType) {
    this.collisionType = collisionType;
    return this;
  }

  /**
   * Sets the color.
   *
   * @param color
   *          the new color
   */
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

  /**
   * Sets the gravity x.
   *
   * @param gravityX
   *          the new gravity x
   */
  public Particle setDeltaIncX(final float gravityX) {
    this.gravityX = gravityX;
    return this;
  }

  /**
   * Sets the gravity y.
   *
   * @param gravityY
   *          the new gravity y
   */
  public Particle setDeltaIncY(final float gravityY) {
    this.gravityY = gravityY;
    return this;
  }

  public Particle setDeltaWidth(final float deltaWidth) {
    this.deltaWidth = deltaWidth;
    return this;
  }

  /**
   * Sets the dx.
   *
   * @param dx
   *          the new dx
   */
  public Particle setDeltaX(final float dx) {
    this.dx = dx;
    return this;
  }

  /**
   * Sets the dy.
   *
   * @param dy
   *          the new dy
   */
  public Particle setDeltaY(final float dy) {
    this.dy = dy;
    return this;
  }

  /**
   * Sets the height.
   *
   * @param height
   *          the new height
   */
  public Particle setHeight(final float height) {
    this.height = height;
    return this;
  }

  public Particle setParticleRenderType(ParticleRenderType particleRenderType) {
    this.particleRenderType = particleRenderType;
    return this;
  }

  /**
   * Sets the width.
   *
   * @param width
   *          the new width
   */
  public Particle setWidth(final float width) {
    this.width = width;
    return this;
  }

  /**
   * Sets the x current.
   *
   * @param x
   *          the new x current
   */
  public Particle setX(final float x) {
    this.x = x;
    return this;
  }

  /**
   * Sets the y current.
   *
   * @param y
   *          the new y current
   */
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
   */
  public void update(final IGameLoop loop, final Point2D emitterOrigin, final float updateRatio) {
    if (this.aliveTick == 0) {
      this.aliveTick = loop.getTicks();
    }

    this.aliveTime = loop.getDeltaTime(this.aliveTick);
    if (this.timeToLiveReached()) {
      return;
    }
    this.x += this.dx * updateRatio;
    this.y += this.dy * updateRatio;

    if (this.isApplyingPhysics() && Game.getPhysicsEngine() != null && Game.getPhysicsEngine().collides(this.getBoundingBox(emitterOrigin), this.getCollisionType())) {
      this.x -= this.dx * updateRatio;
      this.y -= this.dy * updateRatio;
    }

    this.dx += this.gravityX * updateRatio;
    this.dy += this.gravityY * updateRatio;

    this.width += this.getDeltaWidth() * updateRatio;
    this.height += this.getDeltaHeight() * updateRatio;

    final int alpha = this.getTimeToLive() > 0 ? (int) ((this.getTimeToLive() - this.getAliveTime()) / (double) this.getTimeToLive() * this.getColorAlpha()) : this.getColorAlpha();
    this.color = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), alpha >= 0 ? alpha : 0);
  }

  protected Point2D getRelativeLocation(final Point2D effectLocation) {
    return new Point2D.Double(effectLocation.getX() + (int) this.getX() - this.getWidth() / 2, effectLocation.getY() + (int) this.getY() - this.getHeight() / 2);
  }
}
