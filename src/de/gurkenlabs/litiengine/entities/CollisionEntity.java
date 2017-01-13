package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.annotation.CollisionInfo;

@CollisionInfo(collision = true)
public abstract class CollisionEntity extends Entity implements ICollisionEntity {
  private boolean collision;
  private float collisionBoxHeightFactor;

  private float collisionBoxWidthFactor;

  private Rectangle2D collisionBox;

  protected CollisionEntity() {
    super();
    final CollisionInfo info = this.getClass().getAnnotation(CollisionInfo.class);
    this.collisionBoxWidthFactor = info.collisionBoxWidthFactor();
    this.collisionBoxHeightFactor = info.collisionBoxHeightFactor();
    this.collision = info.collision();
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public boolean canCollideWith(final ICollisionEntity otherEntity) {
    return true;
  }

  /**
   * Gets the collision box.
   *
   * @return the collision box
   */
  @Override
  public Rectangle2D getCollisionBox() {
    return this.collisionBox;
  }

  /**
   * Gets the collision box.
   *
   * @param location
   *          the location
   * @return the collision box
   */
  @Override
  public Rectangle2D getCollisionBox(final Point2D location) {
    final float collisionBoxWidth = this.getWidth() * this.getCollisionBoxWidthFactor();
    final float collisionBoxHeight = this.getHeight() * this.getCollisionBoxHeightFactor();
    return new Rectangle2D.Double(location.getX() + this.getWidth() * 0.5 - collisionBoxWidth * 0.5, location.getY() + this.getHeight() - collisionBoxHeight, collisionBoxWidth, collisionBoxHeight);
  }

  public float getCollisionBoxHeightFactor() {
    return this.collisionBoxHeightFactor;
  }

  public float getCollisionBoxWidthFactor() {
    return this.collisionBoxWidthFactor;
  }

  /**
   * Checks for collision.
   *
   * @return true, if successful
   */
  @Override
  public boolean hasCollision() {
    return this.collision;
  }

  /**
   * Sets the collision.
   *
   * @param collision
   *          the new collision
   */
  @Override
  public void setCollision(final boolean collision) {
    this.collision = collision;
  }

  public void setCollisionBoxHeightFactor(final float collisionBoxHeightFactor) {
    this.collisionBoxHeightFactor = collisionBoxHeightFactor;
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  public void setCollisionBoxWidthFactor(final float collisionBoxWidthFactor) {
    this.collisionBoxWidthFactor = collisionBoxWidthFactor;
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setLocation(final Point2D location) {
    super.setLocation(location);
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setSize(final float width, final float height) {
    super.setSize(width, height);
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }
}
