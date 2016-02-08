/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ITimeToLive;
import de.gurkenlabs.litiengine.IUpdateable;

/**
 * Represents a particle in 2D space.
 */
public class Particle implements IUpdateable, ITimeToLive {
  /** The activation tick. */
  private final long aliveTick;
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
  private float deltaIncX;

  /**
   * The gravitational pull to the up (negative) and down (positive) acting on
   * this particle.
   */
  private float deltaIncY;

  /** The height. */
  private float height;

  private final int timeToLife;

  /** The width. */
  private float width;

  private float deltaWidth;
  private float deltaHeight;

  /** The currentlocation of the particle on the X-axis. */
  private float xCurrent;

  /** The current location of the particle on the Y-axis. */
  private float yCurrent;

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
    this.deltaIncX = deltaIncX;
    this.deltaIncY = deltaIncY;
    this.setWidth(width);
    this.setHeight(height);
    this.timeToLife = life;
    this.color = color;
    this.colorAlpha = this.color.getAlpha();
    this.aliveTick = Game.getLoop().getTicks();
  }

  @Override
  public long getAliveTime() {
    return Game.getLoop().getDeltaTime(this.aliveTick);
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
    return this.deltaIncX;
  }

  /**
   * Gets the gravity y.
   *
   * @return the gravity y
   */
  public float getGravityY() {
    return this.deltaIncY;
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
  public Point2D getLocation(final Point2D effectLocation) {
    return new Point2D.Double(effectLocation.getX() + (int) this.getxCurrent() - this.getWidth() / 2, effectLocation.getY() + (int) this.getyCurrent() - this.getHeight() / 2);
  }

  @Override
  public int getTimeToLive() {
    return this.timeToLife;
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

  public void render(final Graphics2D g, final Point2D emitterOrigin) {
    final Point2D renderLocation = this.getLocation(Game.getScreenManager().getCamera().getViewPortLocation(emitterOrigin));
    g.setColor(this.getColor());
    g.fill(new Rectangle2D.Double(renderLocation.getX(), renderLocation.getY(), this.getWidth(), this.getHeight()));
  }

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
    this.deltaIncX = gravityX;
  }

  /**
   * Sets the gravity y.
   *
   * @param gravityY
   *          the new gravity y
   */
  public void setDeltaIncY(final float gravityY) {
    this.deltaIncY = gravityY;
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
  @Override
  public void update() {
    if (this.timeToLiveReached()) {
      return;
    }

    this.xCurrent += this.dx;
    this.yCurrent += this.dy;

    this.dx += this.deltaIncX;
    this.dy += this.deltaIncY;

    this.width += this.getDeltaWidth();
    this.height += this.getDeltaHeight();

    final int alpha = this.getTimeToLive() > 0 ? (int) ((this.getTimeToLive() - this.getAliveTime()) / (double) this.getTimeToLive() * this.getColorAlpha()) : this.getColorAlpha();
    this.color = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), alpha >= 0 ? alpha : 0);
  }

  public float getDeltaHeight() {
    return this.deltaHeight;
  }

  public void setDeltaHeight(float deltaHeight) {
    this.deltaHeight = deltaHeight;
  }

  public float getDeltaWidth() {
    return this.deltaWidth;
  }

  public void setDeltaWidth(float deltaWidth) {
    this.deltaWidth = deltaWidth;
  }
}