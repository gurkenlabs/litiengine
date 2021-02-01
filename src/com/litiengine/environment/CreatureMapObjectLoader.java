package com.litiengine.environment;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.MapObjectProperty;
import com.litiengine.environment.tilemap.MapObjectType;
import com.litiengine.Direction;
import com.litiengine.entities.AnimationInfo;
import com.litiengine.entities.Creature;
import com.litiengine.entities.IEntity;
import com.litiengine.graphics.animation.EntityAnimationController;

public class CreatureMapObjectLoader extends MapObjectLoader {
  private static final Logger log = Logger.getLogger(CreatureMapObjectLoader.class.getName());
  private static final List<Class<? extends Creature>> customCreatureType;

  static {
    customCreatureType = new CopyOnWriteArrayList<>();
  }

  protected CreatureMapObjectLoader() {
    super(MapObjectType.CREATURE);
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
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }

    final String spriteSheet = mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME);

    Creature creature = this.createNewCreature(mapObject, spriteSheet);
    loadDefaultProperties(creature, mapObject);

    if (mapObject.hasCustomProperty(MapObjectProperty.MOVEMENT_VELOCITY)) {
      creature.setVelocity(mapObject.getFloatValue(MapObjectProperty.MOVEMENT_VELOCITY));
    }

    creature.setFacingDirection(mapObject.getEnumValue(MapObjectProperty.SPAWN_DIRECTION, Direction.class, Direction.RIGHT));

    entities.add(creature);
    return entities;
  }

  protected Creature createNewCreature(IMapObject mapObject, String spriteSheet) {
    // for each known custom creature type, check if it was registered for the specified spriteSheetName
    // if so: create an instance of the custom class instead of the default Creature class
    for (Class<? extends Creature> customCreature : customCreatureType) {
      for (String prefix : EntityAnimationController.getDefaultSpritePrefixes(customCreature)) {
        if (prefix != null && spriteSheet.equalsIgnoreCase(prefix)) {
          Creature created = createCustomCreature(customCreature, spriteSheet);
          if (created != null) {
            return created;
          }
        }
      }
    }

    // if no custom creature type war registered for the spriteSheet, we just create a new Creature instance
    return new Creature(spriteSheet);
  }

  private static Creature createCustomCreature(Class<? extends Creature> customCreature, String spriteSheet) {
    try {
      return customCreature.getConstructor(String.class).newInstance(spriteSheet);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      try {
        Creature creature = customCreature.getConstructor().newInstance();
        creature.setSpritesheetName(spriteSheet);
        return creature;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
        log.log(Level.WARNING, "Could not automatically create creature of type {0} because a matching constructor is missing.", new Object[] { customCreature });
        log.log(Level.SEVERE, ex.getMessage(), ex);
      }
    }
    return null;
  }
}
