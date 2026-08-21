package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.SoundSource;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;

/** Resolves map-object metadata to the runtime entity type used for script compatibility. */
public final class ScriptBindingTypeResolver {
  private ScriptBindingTypeResolver() {}

  public static boolean supports(IMapObject mapObject) {
    return mapObject != null && MapObjectType.get(mapObject.getType()) != MapObjectType.AREA;
  }

  public static Class<?> resolve(IMapObject mapObject) {
    if (mapObject == null || mapObject.getType() == null) return IEntity.class;
    String implementation = mapObject.getStringValue(MapObjectProperty.IMPLEMENTATION, null);
    if (implementation != null && !implementation.isBlank()) {
      var discovered = Editor.instance().getProjectCodeIntegration().getDefinitions().stream()
        .filter(definition -> implementation.equals(definition.id())
          || implementation.equals(definition.className()))
        .findFirst().orElse(null);
      if (discovered != null) {
        try {
          ClassLoader loader = Editor.instance().getProjectCodeIntegration().getClassLoader();
          if (loader == null) loader = ScriptBindingTypeResolver.class.getClassLoader();
          return Class.forName(discovered.className(), false, loader);
        } catch (ClassNotFoundException | LinkageError ignored) {
          // Fall back to the built-in map-object type.
        }
      }
    }
    MapObjectType type = MapObjectType.get(mapObject.getType());
    if (type == null) return IEntity.class;
    return switch (type) {
      case PROP -> Prop.class;
      case CREATURE -> Creature.class;
      case LIGHTSOURCE -> LightSource.class;
      case TRIGGER -> Trigger.class;
      case SPAWNPOINT -> Spawnpoint.class;
      case COLLISIONBOX -> CollisionBox.class;
      case STATICSHADOW -> StaticShadow.class;
      case SOUNDSOURCE -> SoundSource.class;
      case EMITTER -> Emitter.class;
      default -> IEntity.class;
    };
  }
}
