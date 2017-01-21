package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.RenderType;

@EntityInfo(renderType = RenderType.OVERLAY)
public class Collider extends CollisionEntity {
  private StaticShadowType shadowType;
  private final boolean obstacle;

  public Collider(boolean isObstacle) {
    super();
    this.obstacle = isObstacle;
  }

  public StaticShadowType getShadowType() {
    return this.shadowType;
  }

  public void setShadowType(StaticShadowType shadowType) {
    this.shadowType = shadowType;
  }

  public boolean isObstacle() {
    return obstacle;
  }

  public enum StaticShadowType {
    DOWN, DOWNLEFT, DOWNRIGHT, LEFT, LEFTDOWN, LEFTRIGHT, RIGHTLEFT, RIGHT, RIGHTDOWN, NOOFFSET, NONE;

    public static StaticShadowType get(String mapObjectType) {
      if (mapObjectType == null || mapObjectType.isEmpty()) {
        return StaticShadowType.NONE;
      }

      try {
        return StaticShadowType.valueOf(mapObjectType);
      } catch (IllegalArgumentException iae) {
        return StaticShadowType.NONE;
      }
    }
  }
}
