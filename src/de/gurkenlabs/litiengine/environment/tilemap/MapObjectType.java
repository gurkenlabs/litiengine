package de.gurkenlabs.litiengine.environment.tilemap;

public enum MapObjectType {
  AREA, COLLISIONBOX, DECORMOB, EMITTER, PATH, LIGHTSOURCE, PROP, SPAWNPOINT, TRIGGER, STATICSHADOW;

  public static MapObjectType fromOrdinal(final int n) {
    return values()[n];
  }

  public static MapObjectType get(final String mapObjectType) {
    if (mapObjectType == null || mapObjectType.isEmpty()) {
      return MapObjectType.AREA;
    }

    try {
      return MapObjectType.valueOf(mapObjectType);
    } catch (final IllegalArgumentException iae) {
      return MapObjectType.AREA;
    }
  }
}
