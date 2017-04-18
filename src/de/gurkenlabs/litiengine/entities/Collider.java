package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.RenderType;

@EntityInfo(renderType = RenderType.OVERLAY)
public class Collider extends CollisionEntity {

  private final boolean obstacle;

  public Collider(final boolean isObstacle) {
    super();
    this.obstacle = isObstacle;
  }

  public boolean isObstacle() {
    return this.obstacle;
  }
}
