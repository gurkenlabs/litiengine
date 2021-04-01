package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;

import static de.gurkenlabs.litiengine.entities.EntityPivotType.COLLISIONBOX_CENTER;
import static de.gurkenlabs.litiengine.entities.EntityPivotType.DIMENSION_CENTER;

public class EntityPivot {
  private final IEntity entity;
  private final EntityPivotType type;

  private double offsetX;
  private double offsetY;

  public EntityPivot(IEntity entity, EntityPivotType type, double offsetX, double offsetY) {
    this.entity = entity;
    this.type = type;
    this.offsetX = offsetX;
    this.offsetY = offsetY;

    if (type == COLLISIONBOX_CENTER && !(entity instanceof ICollisionEntity)) {
      throw new IllegalArgumentException("Pivot type COLLISIONBOX_CENTER is only supported for collision entities.");
    }
  }

  public EntityPivot(IEntity entity, Align align, Valign valign) {
    this(entity, align.getValue(entity.getWidth()), valign.getValue(entity.getHeight()));

  }

  public EntityPivot(IEntity entity, double offsetX, double offsetY) {
    this(entity, EntityPivotType.OFFSET, offsetX, offsetY);
  }

  public IEntity getEntity() {
    return this.entity;
  }

  public EntityPivotType getType() {
    return this.type;
  }

  public double getOffsetX() {
    return this.offsetX;
  }

  public double getOffsetY() {
    return this.offsetY;
  }

  public Point2D getPoint() {
    EntityPivotType type = this.getType();
    if (type == COLLISIONBOX_CENTER) {
      Rectangle2D collisionBox = ((ICollisionEntity) this.getEntity()).getCollisionBox();
      return new Point2D.Double(collisionBox.getCenterX() + this.getOffsetX(), collisionBox.getCenterY() + this.getOffsetY());
    } else if(type == DIMENSION_CENTER) {
      return this.getEntity().getCenter();
    } else {
      return new Point2D.Double(this.getEntity().getX() + this.getOffsetX(), this.getEntity().getY() + this.getOffsetY());
    }
  }

  public void setOffsetX(double offsetX) {
    this.offsetX = offsetX;
  }

  public void setOffsetY(double offsetY) {
    this.offsetY = offsetY;
  }

  public void setOffset(Point2D offset) {
    this.setOffsetX(offset.getX());
    this.setOffsetY(offset.getY());
  }
}
