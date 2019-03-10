package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.physics.CollisionType;

@EntityInfo(renderType = RenderType.OVERLAY)
@CollisionInfo(collision = true, collisionType = CollisionType.STATIC)
public class CollisionBox extends CollisionEntity {

  @TmxProperty(name = MapObjectProperty.COLLISIONBOX_OBSTRUCTINGLIGHTS)
  private final boolean obstructingLight;

  public CollisionBox() {
    this(false);
  }

  public CollisionBox(final boolean obstructingLight) {
    this.obstructingLight = obstructingLight;
  }

  public CollisionBox(double width, double height) {
    this(false);
    this.setWidth(width);
    this.setHeight(height);
    this.setCollisionBoxWidth(this.getWidth());
    this.setCollisionBoxHeight(this.getHeight());
  }

  public CollisionBox(double x, double y, double width, double height) {
    this(width, height);
    this.setX(x);
    this.setY(y);
  }

  public CollisionBox(Rectangle2D box) {
    this(box.getX(), box.getY(), box.getWidth(), box.getHeight());
  }

  public boolean isObstructingLight() {
    return this.obstructingLight;
  }
}
