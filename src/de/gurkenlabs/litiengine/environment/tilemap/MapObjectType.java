package de.gurkenlabs.litiengine.environment.tilemap;

public enum MapObjectType {
  AREA, COLLISIONBOX, CUSTOM, DECORMOB, EMITTER, LANE, LIGHTSOURCE, PROP, SPAWNPOINT, TRIGGER;

  public static MapObjectType fromOrdinal(final int n) {
    return values()[n];
  }

  public static MapObjectType get(final String mapObjectType) {
    if (mapObjectType == null || mapObjectType.isEmpty()) {
      return MapObjectType.CUSTOM;
    }

    try {
      return MapObjectType.valueOf(mapObjectType);
    } catch (final IllegalArgumentException iae) {
      return MapObjectType.CUSTOM;
    }
  }
}
