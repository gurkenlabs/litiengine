package de.gurkenlabs.litiengine.environment;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.entities.AnimationInfo;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.animation.EntityAnimationController;

public class CreatureMapObjectLoader extends MapObjectLoader {
  private static final Logger log = Logger.getLogger(CreatureMapObjectLoader.class.getName());
  private static final List<Class<? extends Creature>> customCreatureType;
  private static final Map<String, Class<? extends Creature>> mapObjectImplementations = new ConcurrentHashMap<>();

  static {
    customCreatureType = new CopyOnWriteArrayList<>();
  }

  protected CreatureMapObjectLoader() {
    super(MapObjectType.CREATURE);
  }

  /// Registers a custom [Creature] implementation that can be automatically provided by this
  /// [MapObjectLoader].
  ///
  /// **This should only be used if the particular implementation doesn't require any additional map object properties to
  /// be initialized.**
  ///
  /// Make sure that the implementation has the following present:
  ///
  /// 1. An [AnimationInfo] annotation with one or more sprite prefixes defined
  /// 2. Either an empty constructor or a constructor that takes in the sprite prefix from the loader.
  ///
  /// The latter is particularly useful for classes that can have different sprite sheets, i.e. share the same logic but
  /// might have a different appearance.
  ///
  /// @param <T>          The type of the custom creature implementation.
  /// @param creatureType The class of the custom [Creature] implementation.
  public static <T extends Creature> void registerCustomCreatureType(Class<T> creatureType) {
    customCreatureType.add(creatureType);
  }

  public static <T extends Creature> void registerMapObjectImplementation(String id, Class<T> creatureType) {
    mapObjectImplementations.put(id, creatureType);
  }

  @Override
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }

    final String spriteSheet = mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME, null);

    Creature creature = this.createNewCreature(environment, mapObject, spriteSheet);
    loadDefaultProperties(creature, mapObject);

    creature.setFacingDirection(mapObject.getEnumValue(MapObjectProperty.SPAWN_DIRECTION, Direction.class, Direction.RIGHT));

    entities.add(creature);
    return entities;
  }

  protected Creature createNewCreature(Environment environment, IMapObject mapObject, String spriteSheet) {
    String implementation = mapObject.getStringValue(MapObjectProperty.IMPLEMENTATION, null);
    Class<? extends Creature> implementationType = implementation == null ? null : mapObjectImplementations.get(implementation);
    if (implementationType != null) {
      IEntity entity = CustomMapObjectLoader.create(implementationType, environment, mapObject);
      if (entity instanceof Creature creature) {
        return creature;
      }
    }
    if (spriteSheet != null) {
      // for each known custom creature type, check if it was registered for the specified spriteSheetName
      // if so: create an instance of the custom class instead of the default Creature class
      for (Class<? extends Creature> customCreature : customCreatureType) {
        for (String prefix : EntityAnimationController.getDefaultSpritePrefixes(customCreature)) {
          if (spriteSheet.equalsIgnoreCase(prefix)) {
            Creature created = createCustomCreature(customCreature, spriteSheet);
            if (created != null) {
              return created;
            }
          }
        }
      }
    }

    // if no custom creature type war registered for the spriteSheet, we just create a new Creature
    // instance
    return new Creature(spriteSheet);
  }

  private static Creature createCustomCreature(Class<? extends Creature> customCreature, String spriteSheet) {
    try {
      return customCreature.getConstructor(String.class).newInstance(spriteSheet);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
        | SecurityException _) {
      try {
        Creature creature = customCreature.getConstructor().newInstance();
        creature.setSpritesheetName(spriteSheet);
        return creature;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
          | SecurityException ex) {
        log.log(Level.WARNING, "Could not automatically create creature of type {0} because a matching constructor is missing.",
            new Object[] {customCreature});
        log.log(Level.SEVERE, ex.getMessage(), ex);
      }
    }
    return null;
  }
}
