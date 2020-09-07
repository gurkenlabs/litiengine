package de.gurkenlabs.litiengine.environment.tilemap;

public enum MapObjectType {
  AREA, COLLISIONBOX, EMITTER, LIGHTSOURCE, PROP, CREATURE, SOUNDSOURCE, SPAWNPOINT, TRIGGER, STATICSHADOW;

  /*
   * Note that this is not part of the enum since we consider this enum a set of valid types in many places in the engine.
   * Usually there is no need to use this explicitly. This is only used to identify {@code MapObjects} that don't have a type specified (aka. raw Tiled MapObjects).
   */
  public static final String UNDEFINED_MAPOBJECTTYPE = "UNDEFINED";

  public static MapObjectType fromOrdinal(final int n) {
    return values()[n];
  }

  public static MapObjectType get(final String mapObjectType) {
    if (mapObjectType == null || mapObjectType.isEmpty()) {
      return null;
    }

    try {
      return MapObjectType.valueOf(mapObjectType);
    } catch (final IllegalArgumentException iae) {
      return null;
    }
  }
}
