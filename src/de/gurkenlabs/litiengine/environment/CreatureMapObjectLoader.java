package de.gurkenlabs.litiengine.environment;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.annotation.AnimationInfo;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Direction;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.animation.EntityAnimationController;

public class CreatureMapObjectLoader extends MapObjectLoader {
  private static final Logger log = Logger.getLogger(CreatureMapObjectLoader.class.getName());
  private static final List<Class<? extends Creature>> customCreatureType;

  protected CreatureMapObjectLoader() {
    super(MapObjectType.CREATURE);
  }

  static {
    customCreatureType = new CopyOnWriteArrayList<>();
  }

  /**
   * <p>
   * Registers a custom {@link Creature} implementation that can be
   * automatically provided by this {@link MapObjectLoader}.
   * </p>
   * 
   * <p>
   * <b>This should only be used if the particular implementation doesn't
   * require any additional map object properties to be initialized.</b>
   * </p>
   * 
   * Make sure that the implementation has the following present:
   * <ol>
   * <li>An {@link AnimationInfo} annotation with one or more sprite prefixes
   * defined</li>
   * <li>Either an empty constructor or a constructor that takes in the sprite
   * prefix from the loader.</li>
   * </ol>
   * 
   * <p>
   * The latter is particularly useful for classes that can have different
   * sprite sheets, i.e. share the same logic but might have a different
   * appearance.
   * </p>
   * 
   * @param <T>
   *          The type of the custom creature implementation.
   * @param creatureType
   *          The class of the custom {@link Creature} implementation.
   */
  public static <T extends Creature> void registerCustomCreatureType(Class<T> creatureType) {
    customCreatureType.add(creatureType);
  }

  @Override
  public Collection<IEntity> load(IEnvironment environment, IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.CREATURE) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + CreatureMapObjectLoader.class);
    }
    
    Collection<IEntity> entities = super.load(environment, mapObject);
    final String spriteSheet = mapObject.getCustomProperty(MapObjectProperty.SPRITESHEETNAME);
    if (spriteSheet == null) {
      return entities;
    }

    Creature creature = this.createNewCreature(mapObject, spriteSheet, mapObject.getCustomProperty(MapObjectProperty.SPAWN_TYPE));
    loadDefaultProperties(creature, mapObject);
    loadCollisionProperties(creature, mapObject);

    // TODO: load IMobileEntity and ICombatEntity properties
    creature.setFacingDirection(mapObject.getCustomPropertyEnum(MapObjectProperty.SPAWN_DIRECTION, Direction.class, Direction.RIGHT));

    entities.add(creature);
    return entities;
  }

  protected Creature createNewCreature(IMapObject mapObject, String spriteSheet, String spawnType) {
    for (Class<? extends Creature> customCreature : customCreatureType) {
      for (String prefix : EntityAnimationController.getDefaultSpritePrefixes(customCreature)) {
        if (prefix != null && spriteSheet.equalsIgnoreCase(prefix)) {
          Creature created = this.createCustomCreature(customCreature, spriteSheet);
          if (created != null) {
            return created;
          }
        }
      }
    }

    return new Creature(spriteSheet);
  }

  private Creature createCustomCreature(Class<? extends Creature> customCreature, String spriteSheet) {
    try {
      return customCreature.getConstructor(String.class).newInstance(spriteSheet);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      try {
        Creature creature = customCreature.getConstructor().newInstance();
        creature.setSpritePrefix(spriteSheet);
        return creature;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
        log.log(Level.SEVERE, "Could not automatically create creature of type {0} because a matching constructor is missing.", new Object[] { customCreature });
      }
    }
    return null;
  }
}
