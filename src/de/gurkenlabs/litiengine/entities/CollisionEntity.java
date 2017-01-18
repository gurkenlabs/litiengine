package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;

@CollisionInfo(collision = true)
public abstract class CollisionEntity extends Entity implements ICollisionEntity {
  private static final double WIDTH_FACTOR = 0.4;
  private static final double HEIGHT_FACTOR = 0.4;

  public enum CollisionAlign {
    CENTER, LEFT, RIGHT;

    public static CollisionAlign get(String align) {
      if (align == null || align.isEmpty()) {
        return CollisionAlign.CENTER;
      }

      try {
        return CollisionAlign.valueOf(align);
      } catch (IllegalArgumentException iae) {
        return CollisionAlign.CENTER;
      }
    }
  }

  public enum CollisionValign {
    DOWN, TOP, MIDDLE;

    public static CollisionValign get(String valign) {
      if (valign == null || valign.isEmpty()) {
        return CollisionValign.DOWN;
      }

      try {
        return CollisionValign.valueOf(valign);
      } catch (IllegalArgumentException iae) {
        return CollisionValign.DOWN;
      }
    }
  }

  private boolean collision;
  private float collisionBoxHeight;

  private float collisionBoxWidth;

  private Rectangle2D collisionBox;

  private CollisionAlign align = CollisionAlign.CENTER;

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
    final double collisionBoxWidth = this.getCollisionBoxWidth() != -1 ? this.getCollisionBoxWidth() : getWidth() * WIDTH_FACTOR;
    final double collisionBoxHeight = this.getCollisionBoxHeight() != -1 ? this.getCollisionBoxHeight() : this.getHeight() * HEIGHT_FACTOR;

    return getCollisionBox(location, this.getWidth(), this.getHeight(), collisionBoxWidth, collisionBoxHeight, this.getAlign(), this.getValign());
  }

  public static Rectangle2D getCollisionBox(Point2D location, double entityWidth, double entityHeight, double collisionBoxWidth, double collisionBoxHeight, CollisionAlign align, CollisionValign valign) {
    double x, y;
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

  public float getCollisionBoxHeight() {
    return this.collisionBoxHeight;
  }

  public float getCollisionBoxWidth() {
    return this.collisionBoxWidth;
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

  public void setCollisionBoxWidth(final float collisionBoxWidth) {
    this.collisionBoxWidth = collisionBoxWidth;
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  public void setCollisionBoxHeight(final float collisionBoxHeight) {
    this.collisionBoxHeight = collisionBoxHeight;
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

  public CollisionAlign getAlign() {
    return this.align;
  }

  public void setCollisionBoxAlign(CollisionAlign align) {
    this.align = align;
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }

  public CollisionValign getValign() {
    return valign;
  }

  public void setCollisionBoxValign(CollisionValign valign) {
    this.valign = valign;
    this.collisionBox = this.getCollisionBox(this.getLocation());
  }
}
