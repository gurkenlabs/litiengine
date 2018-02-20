package de.gurkenlabs.litiengine.graphics;

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
