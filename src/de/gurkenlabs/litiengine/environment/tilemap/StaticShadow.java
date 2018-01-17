package de.gurkenlabs.litiengine.environment.tilemap;

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

  public StaticShadow(int id, String name, double x, double y, float width, float height, StaticShadowType shadowType) {
    super(id, name, x, y, width, height);
    this.setShadowType(shadowType);
  }

  public StaticShadowType getShadowType() {
    return this.shadowType;
  }

  public void setShadowType(final StaticShadowType shadowType) {
    this.shadowType = shadowType;
  }
}
