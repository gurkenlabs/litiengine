package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.entities.CollisionBox;

public class StaticShadow extends MapArea {
  public enum StaticShadowType {
    DOWN, DOWNLEFT, DOWNRIGHT, LEFT, LEFTDOWN, LEFTRIGHT, NONE, NOOFFSET, RIGHT, RIGHTDOWN, RIGHTLEFT;

    public static StaticShadowType get(final String mapObjectType) {
      if (mapObjectType == null || mapObjectType.isEmpty()) {
        return StaticShadowType.NOOFFSET;
      }

      try {
        return StaticShadowType.valueOf(mapObjectType);
      } catch (final IllegalArgumentException iae) {
        return StaticShadowType.NOOFFSET;
      }
    }
  }

  private StaticShadowType shadowType;
  private final CollisionBox origin;

  public StaticShadow(int id, String name, double x, double y, float width, float height, StaticShadowType shadowType) {
    super(id, name, x, y, width, height);
    this.setShadowType(shadowType);
    this.origin = null;
  }

  public StaticShadow(CollisionBox box) {
    super(-1, null, box.getLocation().getX(), box.getLocation().getY(), box.getWidth(), box.getHeight());
    this.setShadowType(StaticShadowType.NONE);
    this.origin = box;
  }

  public StaticShadowType getShadowType() {
    return this.shadowType;
  }

  public void setShadowType(final StaticShadowType shadowType) {
    this.shadowType = shadowType;
  }

  public CollisionBox getOrigin() {
    return this.origin;
  }

  @Override
  public String toString() {
    if (this.getOrigin() == null) {
      return super.toString();
    }

    return "[" + this.getOrigin().toString() + "] -> " + super.toString();
  }
}
