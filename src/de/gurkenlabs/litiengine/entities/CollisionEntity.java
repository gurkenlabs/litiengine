package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.annotation.CollisionInfo;

@CollisionInfo(collision = true)
public abstract class CollisionEntity extends Entity implements ICollisionEntity {
  public enum CollisionAlign {
    CENTER, LEFT, RIGHT;

    public static CollisionAlign get(final String align) {
      if (align == null || align.isEmpty()) {
        return CollisionAlign.CENTER;
      }

      try {
        return CollisionAlign.valueOf(align);
      } catch (final IllegalArgumentException iae) {
        return CollisionAlign.CENTER;
      }
    }
  }

  public enum CollisionValign {
    DOWN, MIDDLE, TOP;

    public static CollisionValign get(final String valign) {
      if (valign == null || valign.isEmpty()) {
        return CollisionValign.DOWN;
      }

      try {
        return CollisionValign.valueOf(valign);
      } catch (final IllegalArgumentException iae) {
        return CollisionValign.DOWN;
      }
    }
  }

  private static final double HEIGHT_FACTOR = 0.4;

  private static final double WIDTH_FACTOR = 0.4;

  public static Rectangle2D getCollisionBox(final Point2D location, final double entityWidth, final double entityHeight, final double collisionBoxWidth, final double collisionBoxHeight, final CollisionAlign align, final CollisionValign valign) {
    double x;
    double y;
    switch (align) {
    case LEFT:
      x = location.getX();
      break;
    case RIGHT:
      x = location.getX() + entityWidth - collisionBoxWidth;
      break;
    case CENTER:
    default:
      x = location.getX() + entityWidth * 0.5 - collisionBoxWidth * 0.5;
      break;
    }

    switch (valign) {
    case MIDDLE:
      y = location.getY() + entityHeight * 0.5 - collisionBoxHeight * 0.5;
      break;
    case TOP:
      y = location.getY();
      break;
    case DOWN:
    default:
      y = location.getY() + entityHeight - collisionBoxHeight;
      break;
    }

    return new Rectangle2D.Double(x, y, collisionBoxWidth, collisionBoxHeight);
  }

  private CollisionAlign align = CollisionAlign.CENTER;

  private boolean collision;

  private Rectangle2D collisionBox;

  private float collisionBoxHeight;

  private float collisionBoxWidth;

  private CollisionValign valign = CollisionValign.DOWN;

  protected CollisionEntity() {
    super();
    final CollisionInfo info = this.getClass().getAnnotation(CollisionInfo.class);
    this.collisionBoxWidth = info.collisionBoxWidth();
    this.collisionBoxHeight = info.collisionBoxHeight();
    this.collision = info.collision();
    this.setCollisionBoxValign(info.valign());
    this.setCollisionBoxAlign(info.align());
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public boolean canCollideWith(final ICollisionEntity otherEntity) {
    return true;
  }

  public CollisionAlign getAlign() {
    return this.align;
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
    final double newCollisionBoxWidth = this.getCollisionBoxWidth() != -1 ? this.getCollisionBoxWidth() : this.getWidth() * WIDTH_FACTOR;
    final double newCollisionBoxHeight = this.getCollisionBoxHeight() != -1 ? this.getCollisionBoxHeight() : this.getHeight() * HEIGHT_FACTOR;

    return getCollisionBox(location, this.getWidth(), this.getHeight(), newCollisionBoxWidth, newCollisionBoxHeight, this.getAlign(), this.getValign());
  }

  public float getCollisionBoxHeight() {
    return this.collisionBoxHeight;
  }

  public float getCollisionBoxWidth() {
    return this.collisionBoxWidth;
  }

  public Point2D getCollisionBoxCenter() {
    return new Point2D.Double(this.getCollisionBox().getCenterX(), this.getCollisionBox().getCenterY());
  }

  public CollisionValign getValign() {
    return this.valign;
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

  public void setCollisionBoxAlign(final CollisionAlign align) {
    this.align = align;
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setCollisionBoxHeight(final float collisionBoxHeight) {
    this.collisionBoxHeight = collisionBoxHeight;
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  public void setCollisionBoxValign(final CollisionValign valign) {
    this.valign = valign;
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setCollisionBoxWidth(final float collisionBoxWidth) {
    this.collisionBoxWidth = collisionBoxWidth;
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

  @Override
  public void setHeight(final float height) {
    super.setHeight(height);
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  @Override
  public void setWidth(final float width) {
    super.setWidth(width);
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }
}
