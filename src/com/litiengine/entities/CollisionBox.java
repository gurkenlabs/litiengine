package com.litiengine.entities;

import java.awt.geom.Rectangle2D;

import com.litiengine.environment.tilemap.MapObjectProperty;
import com.litiengine.environment.tilemap.MapObjectType;
import com.litiengine.environment.tilemap.TmxProperty;
import com.litiengine.environment.tilemap.TmxType;
import com.litiengine.graphics.RenderType;
import com.litiengine.physics.Collision;

@EntityInfo(renderType = RenderType.OVERLAY)
@CollisionInfo(collision = true, collisionType = Collision.STATIC)
@TmxType(MapObjectType.COLLISIONBOX)
public class CollisionBox extends CollisionEntity {

  @TmxProperty(name = MapObjectProperty.COLLISIONBOX_OBSTRUCTINGLIGHTS)
  private boolean obstructingLight;

  /**
   * Instantiates a new {@code CollisionBox} entity.
   */
  public CollisionBox() {
  }

  /**
   * Instantiates a new {@code CollisionBox} entity.
   * 
   * @param obstructingLight
   *          A flag indicating whether this instance should obstruct lights.
   */
  public CollisionBox(final boolean obstructingLight) {
    this.obstructingLight = obstructingLight;
  }

  /**
   * Instantiates a new {@code CollisionBox} entity.
   *
   * @param width
   *          The width of this instance.
   * @param height
   *          The height of this instance.
   */
  public CollisionBox(double width, double height) {
    this.setWidth(width);
    this.setHeight(height);
    this.setCollisionBoxWidth(this.getWidth());
    this.setCollisionBoxHeight(this.getHeight());
  }

  /**
   * Instantiates a new {@code CollisionBox} entity.
   *
   * @param x
   *          The x-coordinate of this instance.
   * @param y
   *          The y-coordinate of this instance.
   * @param width
   *          The width of this instance.
   * @param height
   *          The height of this instance.
   */
  public CollisionBox(double x, double y, double width, double height) {
    this(width, height);
    this.setX(x);
    this.setY(y);
  }

  /**
   * Instantiates a new {@code CollisionBox} entity.
   *
   * @param box
   *          The rectangle defining the location and dimension of this instnace.
   */
  public CollisionBox(Rectangle2D box) {
    this(box.getX(), box.getY(), box.getWidth(), box.getHeight());
  }

  public boolean isObstructingLight() {
    return this.obstructingLight;
  }
}
