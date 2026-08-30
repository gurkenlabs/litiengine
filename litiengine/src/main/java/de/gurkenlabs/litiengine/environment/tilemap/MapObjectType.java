package de.gurkenlabs.litiengine.environment.tilemap;

/// Enumerates the built-in map object types LITIengine recognizes when loading `.tmx` maps. Each value matches the `type` attribute used
/// by the engine's map object loaders to instantiate the matching engine entity.
public enum MapObjectType {
  /// A generic area without a dedicated entity representation.
  @TmxTypeInfo(name = "Area", description = "A generic area region without a dedicated entity representation.")
  AREA,

  /// A static collision rectangle.
  @TmxTypeInfo(name = "Collision Box", description = "A static collision rectangle that obstructs entity movement.")
  COLLISIONBOX,

  /// A particle emitter.
  @TmxTypeInfo(name = "Particle Emitter", description = "Spawns and simulates visual particle effects.")
  EMITTER,

  /// A dynamic light source.
  @TmxTypeInfo(name = "Light Source", description = "Emits dynamic ambient lighting into the scene.")
  LIGHTSOURCE,

  /// A prop entity.
  @TmxTypeInfo(name = "Prop", description = "An interactive or decorative static/destructible object.")
  PROP,

  /// A creature entity.
  @TmxTypeInfo(name = "Creature", description = "A mobile or combat-capable entity (NPC, monster, player).")
  CREATURE,

  /// A point/area emitting a sound.
  @TmxTypeInfo(name = "Sound Source", description = "Emits ambient or positional sound effects.")
  SOUNDSOURCE,

  /// A spawn point for dynamically spawned entities.
  @TmxTypeInfo(name = "Spawn Point", description = "Defines a spawn location and orientation for entities.")
  SPAWNPOINT,

  /// A trigger that fires messages when activated.
  @TmxTypeInfo(name = "Trigger", description = "Fires target messages upon collision or interaction.")
  TRIGGER,

  /// A static (baked) shadow.
  @TmxTypeInfo(name = "Static Shadow", description = "Bakes a static shadow graphic onto the environment.")
  STATICSHADOW;

  /*
   * Note that this is not part of the enum since we consider this enum a set of valid types in many places in the engine.
   * Usually there is no need to use this explicitly. This is only used to identify {@code MapObjects} that don't have a type specified (aka. raw Tiled MapObjects).
   */
  /// Sentinel value used to identify [IMapObject] instances without an explicit type attribute.
  public static final String UNDEFINED_MAPOBJECTTYPE = "UNDEFINED";

  /// Returns the enum value at the given ordinal.
  ///
  /// @param n the ordinal
  /// @return the matching enum value
  public static MapObjectType fromOrdinal(final int n) {
    return values()[n];
  }

  /// Returns the enum value matching the supplied name, or `null` if the name is blank or does not match any known value.
  ///
  /// @param mapObjectType the type name
  /// @return the matching enum value, or `null`
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
