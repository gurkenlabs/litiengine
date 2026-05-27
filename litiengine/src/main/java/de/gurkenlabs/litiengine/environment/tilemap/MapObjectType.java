package de.gurkenlabs.litiengine.environment.tilemap;

/**
 * Enumerates the built-in map object types LITIengine recognizes when loading {@code .tmx} maps. Each value matches the {@code type} attribute used
 * by the engine's map object loaders to instantiate the matching engine entity.
 */
public enum MapObjectType {
  /**
   * A generic area without a dedicated entity representation.
   */
  AREA,
  /**
   * A static collision rectangle.
   */
  COLLISIONBOX,
  /** A particle emitter. */
  EMITTER,
  /** A dynamic light source. */
  LIGHTSOURCE,
  /** A prop entity. */
  PROP,
  /** A creature entity. */
  CREATURE,
  /** A point/area emitting a sound. */
  SOUNDSOURCE,
  /** A spawn point for dynamically spawned entities. */
  SPAWNPOINT,
  /** A trigger that fires messages when activated. */
  TRIGGER,
  /** A static (baked) shadow. */
  STATICSHADOW;

  /*
   * Note that this is not part of the enum since we consider this enum a set of valid types in many places in the engine.
   * Usually there is no need to use this explicitly. This is only used to identify {@code MapObjects} that don't have a type specified (aka. raw Tiled MapObjects).
   */
  /** Sentinel value used to identify {@link IMapObject} instances without an explicit type attribute. */
  public static final String UNDEFINED_MAPOBJECTTYPE = "UNDEFINED";

  /**
   * Returns the enum value at the given ordinal.
   *
   * @param n the ordinal
   * @return the matching enum value
   */
  public static MapObjectType fromOrdinal(final int n) {
    return values()[n];
  }

  /**
   * Returns the enum value matching the supplied name, or {@code null} if the name is blank or does not match any known value.
   *
   * @param mapObjectType the type name
   * @return the matching enum value, or {@code null}
   */
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
