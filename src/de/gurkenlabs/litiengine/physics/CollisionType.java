package de.gurkenlabs.litiengine.physics;

public final class CollisionType {

  public static final int COLLTYPE_ENTITY = 1;

  public static final int COLLTYPE_STATIC = 2;

  public static final int COLLTYPE_ALL = COLLTYPE_ENTITY | COLLTYPE_STATIC;

  private CollisionType() {
  }
}
