package de.gurkenlabs.litiengine.entities;

import static de.gurkenlabs.litiengine.entities.EntityPivotType.COLLISIONBOX_CENTER;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * The {@code EntityPivot} class represents a pivot point for an entity, which can be used to determine the entity's position relative to a specific
 * alignment or offset. This is particularly useful for positioning entities in the game world with precision.
 */
public class EntityPivot {
  private final IEntity entity;
  private final EntityPivotType type;

  private double offsetX;
  private double offsetY;

  /**
   * Constructs a new {@code EntityPivot} with a specified entity, pivot type, and offsets. This constructor allows for the creation of a pivot based
   * on a predefined pivot type, which can include the center of the entity's collision box or an offset from its current position.
   *
   * @param entity  The entity to which this pivot is associated.
   * @param type    The type of pivot, determining how the pivot point is calculated.
   * @param offsetX The horizontal offset from the calculated pivot point.
   * @param offsetY The vertical offset from the calculated pivot point.
   * @throws IllegalArgumentException If the pivot type is COLLISIONBOX_CENTER but the entity is not a collision entity.
   */
  public EntityPivot(IEntity entity, EntityPivotType type, double offsetX, double offsetY) {
    this.entity = entity;
    this.type = type;
    this.offsetX = offsetX;
    this.offsetY = offsetY;

    if (type == COLLISIONBOX_CENTER && !(entity instanceof ICollisionEntity)) {
      throw new IllegalArgumentException("Pivot type COLLISIONBOX_CENTER is only supported for collision entities.");
    }
  }

  /**
   * Constructs a new {@code EntityPivot} with a specified entity and alignment/vertical alignment. This constructor is a convenience method that
   * allows for the creation of a pivot based on alignment parameters, which are then converted into offset values.
   *
   * @param entity The entity to which this pivot is associated.
   * @param align  The horizontal alignment for the pivot.
   * @param valign The vertical alignment for the pivot.
   */
  public EntityPivot(IEntity entity, Align align, Valign valign) {
    this(entity, align.getValue(entity.getWidth()), valign.getValue(entity.getHeight()));
  }

  /**
   * Constructs a new {@code EntityPivot} with a specified entity and offsets. This constructor directly sets the pivot type to OFFSET and applies the
   * given offsets, allowing for custom positioning relative to the entity's current location.
   *
   * @param entity  The entity to which this pivot is associated.
   * @param offsetX The horizontal offset from the entity's position.
   * @param offsetY The vertical offset from the entity's position.
   */
  public EntityPivot(IEntity entity, double offsetX, double offsetY) {
    this(entity, EntityPivotType.OFFSET, offsetX, offsetY);
  }

  /**
   * Gets the associated entity for this pivot.
   *
   * @return The entity associated with this pivot.
   */
  public IEntity getEntity() {
    return this.entity;
  }

  /**
   * Gets the type of pivot.
   *
   * @return The pivot type.
   */
  public EntityPivotType getType() {
    return this.type;
  }

  /**
   * Gets the horizontal offset for this pivot.
   *
   * @return The horizontal offset.
   */
  public double getOffsetX() {
    return this.offsetX;
  }

  /**
   * Gets the vertical offset for this pivot.
   *
   * @return The vertical offset.
   */
  public double getOffsetY() {
    return this.offsetY;
  }

  /**
   * Calculates and returns the pivot point based on the pivot type and offsets. This method determines the exact location of the pivot point, taking
   * into account the entity's current position, size, and the specified offsets.
   *
   * @return The calculated pivot point as a {@code Point2D} object.
   */
  public Point2D getPoint() {
    EntityPivotType pivot = getType();
    switch (pivot) {
      case COLLISIONBOX_CENTER -> {
        Rectangle2D collisionBox = ((ICollisionEntity) getEntity()).getCollisionBox();
        return new Point2D.Double(collisionBox.getCenterX() + getOffsetX(), collisionBox.getCenterY() + getOffsetY());
      }
      case DIMENSION_CENTER -> {
        return new Point2D.Double(getEntity().getCenter().getX() + getOffsetX(), getEntity().getCenter().getY() + getOffsetY());
      }
      default -> {
        return new Point2D.Double(getEntity().getX() + getOffsetX(), getEntity().getY() + getOffsetY());
      }
    }
  }

  /**
   * Sets the horizontal offset for this pivot.
   *
   * @param offsetX The new horizontal offset.
   */
  public void setOffsetX(double offsetX) {
    this.offsetX = offsetX;
  }

  /**
   * Sets the vertical offset for this pivot.
   *
   * @param offsetY The new vertical offset.
   */
  public void setOffsetY(double offsetY) {
    this.offsetY = offsetY;
  }

  /**
   * Sets both the horizontal and vertical offsets for this pivot.
   *
   * @param offset A {@code Point2D} representing the new offsets.
   */
  public void setOffset(Point2D offset) {
    this.setOffsetX(offset.getX());
    this.setOffsetY(offset.getY());
  }
}
