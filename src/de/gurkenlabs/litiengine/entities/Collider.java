package de.gurkenlabs.litiengine.entities;

public class Collider extends CollisionEntity {

  public Collider() {
    super();
    this.setCollisionBoxHeightFactor(1);
    this.setCollisionBoxWidthFactor(1);
  }

  public enum StaticShadowType {
    DOWN, DOWNLEFT, DOWNRIGHT, LEFT, LEFTDOWN, LEFTRIGHT, RIGHTLEFT, RIGHT, RIGHTDOWN, NOOFFSET;
  }
}
