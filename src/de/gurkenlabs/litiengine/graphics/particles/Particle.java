package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.ITimeToLive;
import de.gurkenlabs.litiengine.physics.IPhysicsEngine;

public abstract class Particle implements ITimeToLive {
  /** The activation tick. */
  private long aliveTick;
  private long aliveTime;
  /** The color of the particle. */
  private Color color;
  /** The color alpha. */
  private int colorAlpha = 255;
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

  private float deltaWidth;
  private float deltaHeight;

  /** The currentlocation of the particle on the X-axis. */
  private float xCurrent;

  /** The current location of the particle on the Y-axis. */
  private float yCurrent;
  
  private boolean applyStaticPhysics;

  /**
   * Constructs a new particle.
   *
   * @param xCurrent
   *          The current location of the effect on the X-axis relative to the
   *          effects map location.
   * @param yCurrent
   *          The currentlocation of the particle on the Y-axis relative to the
   *          effects map location.
   * @param dx
   *          The change in X, per update, of the effect.
   * @param dy
   *          The change in Y, per update, of the effect.
   * @param deltaIncX
   *          The gravitational pull to the left (negative) and right (positive)
   *          acting on this effect.
   * @param deltaIncY
   *          The gravitational pull to the up (negative) and down (positive)
   *          acting on this effect.
   * @param width
   *          the width
   * @param height
   *          the height
   * @param life
   *          The remaining lifetime of the effect.
   * @param color
   *          The color of the effect.
   * @param particleType
   *          the particle type
   */
  public Particle(final float xCurrent, final float yCurrent, final float dx, final float dy, final float deltaIncX, final float deltaIncY, final float width, final float height, final int life, final Color color) {
    this.xCurrent = xCurrent;
    this.yCurrent = yCurrent;
    this.dx = dx;
    this.dy = dy;
    this.gravityX = deltaIncX;
    this.gravityY = deltaIncY;
    this.setWidth(width);
    this.setHeight(height);
    this.timeToLive = life;
    this.color = color;
    this.colorAlpha = this.color.getAlpha();
  }

  @Override
  public long getAliveTime() {
    return this.aliveTime;
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
    effectLocation = Game.getScreenManager() != null ? Game.getScreenManager().getCamera().getViewPortLocation(effectLocation) : effectLocation;
    return this.getRelativeLocation(effectLocation);
  }

  protected Point2D getRelativeLocation(final Point2D effectLocation) {
    return new Point2D.Double(effectLocation.getX() + (int) this.getxCurrent() - this.getWidth() / 2, effectLocation.getY() + (int) this.getyCurrent() - this.getHeight() / 2);
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
  public float getxCurrent() {
    return this.xCurrent;
  }

  /**
   * Gets the y current.
   *
   * @return the y current
   */
  public float getyCurrent() {
    return this.yCurrent;
  }

  public abstract void render(final Graphics2D g, final Point2D emitterOrigin);

  /**
   * Sets the color.
   *
   * @param color
   *          the new color
   */
  public void setColor(final Color color) {
    this.color = color;
  }

  /**
   * Sets the color alpha. A value between 0 and 100 is expected. Otherwise it
   * won't be set.
   *
   * @param colorAlpha
   *          the new color alpha
   */
  public void setColorAlpha(final int colorAlpha) {
    if (colorAlpha < 0 || colorAlpha > 100) {
      return;
    }

    this.colorAlpha = colorAlpha;
  }

  /**
   * Sets the dx.
   *
   * @param dx
   *          the new dx
   */
  public void setDx(final float dx) {
    this.dx = dx;
  }

  /**
   * Sets the dy.
   *
   * @param dy
   *          the new dy
   */
  public void setDy(final float dy) {
    this.dy = dy;
  }

  /**
   * Sets the gravity x.
   *
   * @param gravityX
   *          the new gravity x
   */
  public void setDeltaIncX(final float gravityX) {
    this.gravityX = gravityX;
  }

  /**
   * Sets the gravity y.
   *
   * @param gravityY
   *          the new gravity y
   */
  public void setDeltaIncY(final float gravityY) {
    this.gravityY = gravityY;
  }

  /**
   * Sets the height.
   *
   * @param height
   *          the new height
   */
  public void setHeight(final float height) {
    this.height = height;
  }

  /**
   * Sets the width.
   *
   * @param width
   *          the new width
   */
  public void setWidth(final float width) {
    this.width = width;
  }

  /**
   * Sets the x current.
   *
   * @param xCurrent
   *          the new x current
   */
  public void setxCurrent(final float xCurrent) {
    this.xCurrent = xCurrent;
  }

  /**
   * Sets the y current.
   *
   * @param yCurrent
   *          the new y current
   */
  public void setyCurrent(final float yCurrent) {
    this.yCurrent = yCurrent;
  }

  @Override
  public boolean timeToLiveReached() {
    return this.getTimeToLive() > 0 && this.getAliveTime() >= this.getTimeToLive();
  }

  /**
   * Updates the effect's position, change in xCurrent, change in yCurrent,
   * remaining lifetime, and color.
   */
  public void update(final IGameLoop loop, Point2D emitterOrigin, final float updateRatio) {
    if (this.aliveTick == 0) {
      this.aliveTick = loop.getTicks();
    }

    this.aliveTime = loop.getDeltaTime(this.aliveTick);
    if (this.timeToLiveReached()) {
      return;
    }
    this.xCurrent += this.dx * updateRatio;
    this.yCurrent += this.dy * updateRatio;

    if (this.isApplyingStaticPhysics() && Game.getPhysicsEngine() != null && Game.getPhysicsEngine().collides(this.getBoundingBox(emitterOrigin), IPhysicsEngine.COLLTYPE_STATIC)) {
      this.xCurrent -= this.dx * updateRatio;
      this.yCurrent -= this.dy * updateRatio;
    }

    this.dx += this.gravityX * updateRatio;
    this.dy += this.gravityY * updateRatio;

    this.width += this.getDeltaWidth() * updateRatio;
    this.height += this.getDeltaHeight() * updateRatio;

    final int alpha = this.getTimeToLive() > 0 ? (int) ((this.getTimeToLive() - this.getAliveTime()) / (double) this.getTimeToLive() * this.getColorAlpha()) : this.getColorAlpha();
    this.color = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), alpha >= 0 ? alpha : 0);
  }

  public float getDeltaHeight() {
    return this.deltaHeight;
  }

  public void setDeltaHeight(final float deltaHeight) {
    this.deltaHeight = deltaHeight;
  }

  public float getDeltaWidth() {
    return this.deltaWidth;
  }

  public void setDeltaWidth(final float deltaWidth) {
    this.deltaWidth = deltaWidth;
  }

  public Rectangle2D getBoundingBox(Point2D origin) {
    return new Rectangle2D.Double(origin.getX() + this.getxCurrent(), origin.getY() + this.getyCurrent(), this.getWidth(), this.getHeight());
  }

  public boolean isApplyingStaticPhysics() {
    return applyStaticPhysics;
  }

  public void setApplyStaticPhysics(boolean applyStaticPhysics) {
    this.applyStaticPhysics = applyStaticPhysics;
  }
}
