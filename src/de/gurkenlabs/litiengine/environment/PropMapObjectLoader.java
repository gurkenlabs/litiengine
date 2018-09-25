package de.gurkenlabs.litiengine.environment;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.annotation.AnimationInfo;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.animation.EntityAnimationController;

public class PropMapObjectLoader extends MapObjectLoader {
  private static final Logger log = Logger.getLogger(PropMapObjectLoader.class.getName());
  private static final List<Class<? extends Prop>> customPropType;

  static {
    customPropType = new CopyOnWriteArrayList<>();
  }

  protected PropMapObjectLoader() {
    super(MapObjectType.PROP);
  }

  /**
   * <p>
   * Registers a custom {@link Prop} implementation that can be automatically
   * provided by this {@link MapObjectLoader}.
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
   * @param propType
   *          The class of the custom {@link Prop} implementation.
   */
  public static <T extends Prop> void registerCustomPropType(Class<T> propType) {
    customPropType.add(propType);
  }

  @Override
  public Collection<IEntity> load(IEnvironment environment, IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.PROP) {
      throw new IllegalArgumentException("Cannot load a mapobject of the type " + mapObject.getType() + " with a loader of the type " + PropMapObjectLoader.class);
    }

    final Prop prop = this.createNewProp(mapObject, mapObject.getString(MapObjectProperty.SPRITESHEETNAME));
    loadDefaultProperties(prop, mapObject);
    loadCollisionProperties(prop, mapObject);
    final Material material = mapObject.getString(MapObjectProperty.PROP_MATERIAL) == null ? Material.UNDEFINED : Material.valueOf(mapObject.getString(MapObjectProperty.PROP_MATERIAL));
    prop.setMaterial(material);
    prop.setObstacle(mapObject.getBool(MapObjectProperty.PROP_OBSTACLE));

    final Rotation rotation = mapObject.getString(MapObjectProperty.PROP_ROTATION) == null ? Rotation.NONE : Rotation.valueOf(mapObject.getString(MapObjectProperty.PROP_ROTATION));
    prop.setSpriteRotation(rotation);

    prop.setIndestructible(mapObject.getBool(MapObjectProperty.COMBAT_INDESTRUCTIBLE));

    AttributeModifier<Integer> mod = new AttributeModifier<>(Modification.SET, mapObject.getInt(MapObjectProperty.COMBAT_HEALTH));
    prop.getAttributes().getHealth().modifyMaxBaseValue(mod);
    prop.getAttributes().getHealth().modifyBaseValue(mod);

    prop.setAddShadow(mapObject.getBool(MapObjectProperty.PROP_ADDSHADOW));

    prop.setFlipHorizontally(mapObject.getBool(MapObjectProperty.PROP_FLIPHORIZONTALLY));
    prop.setFlipVertically(mapObject.getBool(MapObjectProperty.PROP_FLIPVERTICALLY));
    prop.setScaling(mapObject.getBool(MapObjectProperty.PROP_SCALE));

    prop.setTeam(mapObject.getInt(MapObjectProperty.COMBAT_TEAM));

    Collection<IEntity> entities = new ArrayList<>();
    entities.add(prop);
    return entities;
  }

  protected Prop createNewProp(IMapObject mapObject, String spriteSheet) {
    for (Class<? extends Prop> customProp : customPropType) {
      for (String prefix : EntityAnimationController.getDefaultSpritePrefixes(customProp)) {
        if (prefix != null && ("prop-" + spriteSheet).equalsIgnoreCase(prefix)) {
          Prop created = this.createCustomProp(customProp, spriteSheet);
          if (created != null) {
            return created;
          }
        }
      }
    }

    return new Prop(spriteSheet);
  }

  private Prop createCustomProp(Class<? extends Prop> customProp, String spriteSheet) {
    try {
      return customProp.getConstructor(String.class).newInstance(spriteSheet);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      try {
        Prop creature = customProp.getConstructor().newInstance();
        creature.setSpritesheetName(spriteSheet);
        return creature;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
        log.log(Level.SEVERE, "Could not automatically create prop of type {0} because a matching constructor is missing.", new Object[] { customProp });
        log.log(Level.SEVERE, ex.getMessage(), ex);
      }
    }

    return null;
  }
}
