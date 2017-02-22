package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.RenderType;

@EntityInfo(renderType = RenderType.OVERLAY)
public class Collider extends CollisionEntity {
  public enum StaticShadowType {
    DOWN, DOWNLEFT, DOWNRIGHT, LEFT, LEFTDOWN, LEFTRIGHT, NONE, NOOFFSET, RIGHT, RIGHTDOWN, RIGHTLEFT;

    public static StaticShadowType get(final String mapObjectType) {
      if (mapObjectType == null || mapObjectType.isEmpty()) {
        return StaticShadowType.NONE;
      }

      try {
        return StaticShadowType.valueOf(mapObjectType);
      } catch (final IllegalArgumentException iae) {
        return StaticShadowType.NONE;
      }
    }
  }

  private final boolean obstacle;

  private StaticShadowType shadowType;

  public Collider(final boolean isObstacle) {
    super();
    this.obstacle = isObstacle;
  }

  public StaticShadowType getShadowType() {
    return this.shadowType;
  }

  public boolean isObstacle() {
    return this.obstacle;
  }

  public void setShadowType(final StaticShadowType shadowType) {
    this.shadowType = shadowType;
  }
}
