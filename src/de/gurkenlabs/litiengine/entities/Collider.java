package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.RenderType;

@EntityInfo(renderType = RenderType.OVERLAY)
public class Collider extends CollisionEntity {
  private StaticShadowType shadowType;

  public Collider() {
    super();
    this.setCollisionBoxHeightFactor(1);
    this.setCollisionBoxWidthFactor(1);
  }

  public StaticShadowType getShadowType() {
    return this.shadowType;
  }

  public void setShadowType(StaticShadowType shadowType) {
    this.shadowType = shadowType;
  }

  public enum StaticShadowType {
    DOWN, DOWNLEFT, DOWNRIGHT, LEFT, LEFTDOWN, LEFTRIGHT, RIGHTLEFT, RIGHT, RIGHTDOWN, NOOFFSET, NONE;
  }
}
