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
import com.litiengine.entities.AnimationInfo;
import com.litiengine.entities.IEntity;
import com.litiengine.entities.Material;
import com.litiengine.entities.Prop;
import com.litiengine.graphics.animation.EntityAnimationController;
import com.litiengine.graphics.animation.PropAnimationController;

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
  public Collection<IEntity> load(Environment environment, IMapObject mapObject) {
    Collection<IEntity> entities = new ArrayList<>();
    if (!this.isMatchingType(mapObject)) {
      return entities;
    }


    final Prop prop = this.createNewProp(mapObject, mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME));
    loadDefaultProperties(prop, mapObject);
    
    prop.setMaterial(Material.get(mapObject.getStringValue(MapObjectProperty.PROP_MATERIAL)));

    entities.add(prop);
    return entities;
  }

  protected Prop createNewProp(IMapObject mapObject, String spriteSheet) {
    for (Class<? extends Prop> customProp : customPropType) {
      for (String prefix : EntityAnimationController.getDefaultSpritePrefixes(customProp)) {
        if (prefix != null && (PropAnimationController.PROP_IDENTIFIER + spriteSheet).equalsIgnoreCase(prefix)) {
          Prop created = createCustomProp(customProp, spriteSheet);
          if (created != null) {
            return created;
          }
        }
      }
    }

    return new Prop(spriteSheet);
  }

  private static Prop createCustomProp(Class<? extends Prop> customProp, String spriteSheet) {
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
