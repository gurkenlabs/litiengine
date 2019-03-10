package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.physics.CollisionType;

@EntityInfo(renderType = RenderType.OVERLAY)
public class CollisionBox extends CollisionEntity {

  @TmxProperty(name = MapObjectProperty.COLLISIONBOX_OBSTRUCTINGLIGHTS)
  private final boolean obstructingLight;

  public CollisionBox() {
    this(false);
  }

  public CollisionBox(final boolean obstructingLight) {
    super();
    this.obstructingLight = obstructingLight;
    this.setCollisionType(CollisionType.STATIC);
  }

  public boolean isObstructingLight() {
    return this.obstructingLight;
  }
}
