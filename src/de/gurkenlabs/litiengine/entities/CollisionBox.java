package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.RenderType;

@EntityInfo(renderType = RenderType.OVERLAY)
public class CollisionBox extends CollisionEntity {

  private final boolean obstacle;
  private final boolean obstructingLight;

  public CollisionBox(final boolean isObstacle) {
    this(isObstacle, false);
  }

  public CollisionBox(final boolean isObstacle, final boolean obstructingLight) {
    super();
    this.obstacle = isObstacle;
    this.obstructingLight = obstructingLight;
  }

  public boolean isObstacle() {
    return this.obstacle;
  }

  public boolean isObstructingLight() {
    return this.obstructingLight;
  }
}
