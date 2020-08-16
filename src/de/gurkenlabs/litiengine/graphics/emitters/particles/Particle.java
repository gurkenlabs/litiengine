package de.gurkenlabs.litiengine.graphics.emitters.particles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ITimeToLive;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.MathUtilities;

public abstract class Particle implements ITimeToLive {
  private long aliveTick;
  private long aliveTime;
  private float angle;
  private float deltaAngle;

  private Collision collisionType;
  private Color color;
  private float colorAlpha = 1;
  private float deltaHeight;
  private float deltaWidth;
  /**
   * The horizontal velocity (horizontal movement per update) for this particle.
   */
  private float velocityX;
  /**
   * The vertical velocity (vertical movement per update) for this particle.
   */
  private float velocityY;
  private boolean outlineOnly;
  private boolean antiAliasing;

  /**
   * The horizontal acceleration (increase / decrease in velocity over time) for this particle.
   */
  private float accelerationX;

  /**
   * The vertical acceleration (increase / decrease in velocity over time) for this particle.
   */
  private float accelerationY;
  private float height;
  private int timeToLive;
  private float width;

  /** The current location of the particle on the X-axis. */
  private float x;

  /** The current location of the particle on the Y-axis. */
  private float y;

  private RenderType customRenderType;
  private boolean useCustomRenderType;

  private float opacity;

  private boolean fade;

  private boolean fadeOnCollision;

  private boolean colliding;

  private boolean continuousCollision;

  private boolean stopOnCollision;

  /**
   * Constructs a new particle.
   * 
   * @param width
   *          the particle width in pixels
   * @param height
   *          the particle height in pixels
   */
  public Particle(final float width, final float height) {
    this.setCustomRenderType(RenderType.NONE);
    this.setWidth(width);
    this.setHeight(height);
    this.collisionType = Collision.NONE;
    this.opacity = 1;
    this.fade = true;
    this.setStopOnCollision(true);
    this.setContinuousCollision(false);
  }

  @Override
  public long getAliveTime() {
    return this.aliveTime;
  }

  /**
   * Gets the current bounding box of the particle, depending on its spawn location.
   * 
   * @param origin
   *          the spawn location of this particle
   * @return The Rectangular particle bounding box.
   */
  public Rectangle2D getBoundingBox(final Point2D origin) {
    return new Rectangle2D.Double(origin.getX() + this.getX(), origin.getY() + this.getY(), this.getWidth(), this.getHeight());
  }

  public Collision getCollisionType() {
    return this.collisionType;
  }

  public Color getColor() {
    return this.color;
  }

  public float getColorAlpha() {
    return this.colorAlpha;
  }

  public float getDeltaHeight() {
    return this.deltaHeight;
  }

  public float getDeltaWidth() {
    return this.deltaWidth;
  }

  public float getVelocityX() {
    return this.velocityX;
  }

  public float getVelocityY() {
    return this.velocityY;
  }

  public float getAccelerationX() {
    return this.accelerationX;
  }

  public float getAccelerationY() {
    return this.accelerationY;
  }

  public float getAngle() {
    return this.angle;
  }

  public float getDeltaAngle() {
    return this.deltaAngle;
  }

  public float getHeight() {
    return this.height;
  }

  public boolean isOutlineOnly() {
    return this.outlineOnly;
  }

  public boolean isAntiAliased() {
    return this.antiAliasing;
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
    Point2D newEffectLocation = Game.screens() != null ? Game.world().camera().getViewportLocation(effectLocation) : effectLocation;
    return this.getAbsoluteLocation(newEffectLocation);
  }

  public RenderType getCustomRenderType() {
    return this.customRenderType;
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

  public boolean isStoppingOnCollision() {
    return this.stopOnCollision;
  }

  public abstract void render(final Graphics2D g, final Point2D emitterOrigin);

  public Particle setCollisionType(final Collision collisionType) {
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

  public void setStopOnCollision(boolean stopOnCollision) {
    this.stopOnCollision = stopOnCollision;
  }

  public Particle setColor(final Color color) {
    if (color != null) {
      this.color = color;
      this.setColorAlpha((float) this.color.getAlpha() / 255);
    }
    return this;
  }

  /**
   * Sets the color alpha. A float value between 0 and 1 is expected.
   *
   * @param colorAlpha
   *          the new color alpha
   * 
   * @return This {@link Particle} instance to chain further setter calls.
   */
  public Particle setColorAlpha(final float colorAlpha) {
    this.colorAlpha = MathUtilities.clamp(colorAlpha, 0, 1);
    return this;
  }

  public Particle setDeltaHeight(final float deltaHeight) {
    this.deltaHeight = deltaHeight;
    return this;
  }

  public Particle setAccelerationX(final float accelerationX) {
    this.accelerationX = accelerationX;
    return this;
  }

  public Particle setAccelerationY(final float accelerationY) {
    this.accelerationY = accelerationY;
    return this;
  }

  public Particle setAngle(final float angle) {
    this.angle = angle;
    return this;
  }

  public Particle setDeltaAngle(final float deltaAngle) {
    this.deltaAngle = deltaAngle;
    return this;
  }

  public Particle setDeltaWidth(final float deltaWidth) {
    this.deltaWidth = deltaWidth;
    return this;
  }

  public Particle setVelocityX(final float velocityX) {
    this.velocityX = velocityX;
    return this;
  }

  public Particle setVelocityY(final float velocityY) {
    this.velocityY = velocityY;
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

  public Particle setOutlineOnly(final boolean outlineOnly) {
    this.outlineOnly = outlineOnly;
    return this;
  }

  public Particle setAntiAliasing(final boolean antiAliasing) {
    this.antiAliasing = antiAliasing;
    return this;
  }

  public Particle setCustomRenderType(RenderType renderType) {
    this.customRenderType = renderType;
    this.useCustomRenderType = true;
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

  public Particle setTimeToLive(final int ttl) {
    this.timeToLive = ttl;
    return this;
  }

  public Particle init(final EmitterData data) {
    this.setX((float) data.getParticleOffsetX().get());
    this.setY((float) data.getParticleOffsetY().get());

    this.setAccelerationX((float) data.getAccelerationX().get());
    this.setAccelerationY((float) data.getAccelerationY().get());

    this.setVelocityX((float) data.getVelocityX().get());
    this.setVelocityY((float) data.getVelocityY().get());

    this.setDeltaWidth((float) data.getDeltaWidth().get());
    this.setDeltaHeight((float) data.getDeltaHeight().get());

    this.setAngle((float) data.getAngle().get());
    this.setDeltaAngle((float) data.getDeltaAngle().get());

    this.setTimeToLive((int) data.getParticleTTL().get());
    this.setColor(ColorHelper.decode(Game.random().choose(data.getColors())));

    this.setCollisionType(data.getCollisionType());
    this.setOutlineOnly(data.isOutlineOnly());
    this.setAntiAliasing(data.isAntiAliased());
    this.setFade(data.isFading());

    this.setFadeOnCollision(data.isFadingOnCollision());
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
      this.aliveTick = Game.time().now();
    }

    this.aliveTime = Game.time().since(this.aliveTick);
    if (this.timeToLiveReached()) {
      return;
    }
    final int alpha = (int) (this.getOpacity() * 255);

    if (this.color != null) {
      this.setColor(new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), MathUtilities.clamp(alpha, 0, 255)));
    }

    if (this.colliding) {
      return;
    }

    if (this.getDeltaWidth() != 0) {
      this.width += this.getDeltaWidth() * updateRatio;
    }

    if (this.getDeltaHeight() != 0) {
      this.height += this.getDeltaHeight() * updateRatio;
    }

    if (this.getDeltaAngle() != 0) {
      this.angle += this.getDeltaAngle() * updateRatio;
    }

    // test for ray cast collision
    final float targetX = this.x + this.getVelocityX() * updateRatio;
    final float targetY = this.y + this.getVelocityY() * updateRatio;

    if (targetX == this.x && targetY == this.y) {
      return;
    }

    if (this.checkForCollision(emitterOrigin, targetX, targetY)) {
      return;
    }

    if (this.getVelocityX() != 0) {
      this.x = targetX;
    }

    if (this.getVelocityY() != 0) {
      this.y = targetY;
    }

    if (this.getAccelerationX() != 0) {
      this.velocityX += this.getAccelerationX() * updateRatio;
    }

    if (this.getAccelerationY() != 0) {
      this.velocityY += this.getAccelerationY() * updateRatio;
    }
  }

  private boolean checkForCollision(final Point2D emitterOrigin, float targetX, float targetY) {
    if (this.isStoppingOnCollision() && this.colliding) {
      return true;
    }

    if (this.isContinuousCollisionEnabled()) {
      Point2D start = this.getAbsoluteLocation(emitterOrigin);
      double endX = emitterOrigin.getX() + targetX;
      double endY = emitterOrigin.getY() + targetY;
      Line2D ray = new Line2D.Double(start.getX(), start.getY(), endX, endY);
      if (this.getCollisionType() != Collision.NONE && Game.physics() != null && Game.physics().collides(ray, this.getCollisionType())) {
        collide();
        return true;
      }
    } else if (this.getCollisionType() != Collision.NONE && Game.physics() != null && Game.physics().collides(this.getBoundingBox(emitterOrigin).getBounds2D(), this.getCollisionType())) {
      collide();
      return true;
    }

    return false;
  }

  private void collide() {
    if (!this.colliding) {
      this.colliding = true;
      if (this.isFadingOnCollision()) {
        this.setFade(true);
      }
    }
  }

  public Point2D getAbsoluteLocation(final Point2D effectLocation) {
    return new Point2D.Float(getAbsoluteX(effectLocation), getAbsoluteY(effectLocation));
  }

  protected float getAbsoluteX(Point2D emitterOrigin) {
    return (float) (emitterOrigin.getX() + this.getX() - this.getWidth() / 2.0);
  }

  protected float getAbsoluteY(Point2D emitterOrigin) {
    return (float) (emitterOrigin.getY() + this.getY() - this.getHeight() / 2.0);
  }

  protected float getOpacity() {
    if (this.isFading() && this.getTimeToLive() > 0) {
      this.opacity = MathUtilities.clamp(1 - this.getColorAlpha() - (float) this.getAliveTime() / this.getTimeToLive(), 0, 1);
      return this.opacity;
    }
    return 1;
  }

  public boolean usesCustomRenderType() {
    return this.useCustomRenderType;
  }
}
