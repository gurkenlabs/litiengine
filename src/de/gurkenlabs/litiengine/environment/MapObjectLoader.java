package de.gurkenlabs.litiengine.environment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.annotation.CustomMapObjectProperty;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.util.ReflectionUtilities;

public abstract class MapObjectLoader implements IMapObjectLoader {
  private final String mapObjectType;

  protected MapObjectLoader(String mapObjectType) {
    this.mapObjectType = mapObjectType;
  }

  protected MapObjectLoader(MapObjectType mapObjectType) {
    this.mapObjectType = mapObjectType.name();
  }

  @Override
  public String getMapObjectType() {
    return this.mapObjectType;
  }

  @Override
  public Collection<IEntity> load(IEnvironment environment, IMapObject mapObject) {
    return new ArrayList<>();
  }

  /**
   * Loads engine default properties to the specified <code>IEntity</code> instance:
   * <ul>
   * <li>width, height</li>
   * <li>mapId</li>
   * <li>name</li>
   * <li>location</li>
   * <li>tags</li>
   * </ul>
   * Also, this supports predefined <code>CustomMapObjectProperties</code>. It loads the specified custom properties via reflection.
   * @param entity
   *          The entity instance that will be initialized.
   * @param mapObject
   *          The mapObject that provides the static information for the new entity.
   * 
   * @see CustomMapObjectProperty
   */
  public static void loadDefaultProperties(IEntity entity, IMapObject mapObject) {
    entity.setMapId(mapObject.getId());
    entity.setWidth(mapObject.getWidth());
    entity.setHeight(mapObject.getHeight());
    entity.setName(mapObject.getName());
    entity.setLocation(mapObject.getLocation());

    String tagsString = mapObject.getCustomProperty(MapObjectProperty.TAGS);
    if (tagsString != null && tagsString.trim().length() > 0) {
      String[] tags = tagsString.split(",");

      for (String rawTag : tags) {
        String tag = rawTag.trim().replaceAll("[^A-Za-z0-9\\-\\_]", "");
        if (tag == null || tag.isEmpty()) {
          continue;
        }

        entity.addTag(tag);
      }
    }

    loadCustomMapObjectProperties(entity, mapObject);
  }

  private static void loadCustomMapObjectProperties(IEntity entity, IMapObject mapObject) {
    CustomMapObjectProperty customProp = entity.getClass().getAnnotation(CustomMapObjectProperty.class);
    if (customProp == null) {
      return;
    }

    for (String key : customProp.keys()) {
      Field field = ReflectionUtilities.getField(entity.getClass(), key);
      if (field == null) {
        return;
      }

      ReflectionUtilities.setFieldValue(entity.getClass(), entity, key, mapObject.getCustomProperty(key));
    }
  }

  public static void loadCollisionProperties(ICollisionEntity entity, IMapObject mapObject) {
    entity.setCollision(mapObject.getCustomPropertyBool(MapObjectProperty.COLLISION));
    entity.setCollisionBoxWidth(mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_WIDTH));
    entity.setCollisionBoxHeight(mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_HEIGHT));
    entity.setCollisionBoxAlign(Align.get(mapObject.getCustomProperty(MapObjectProperty.COLLISION_ALGIN)));
    entity.setCollisionBoxValign(Valign.get(mapObject.getCustomProperty(MapObjectProperty.COLLISION_VALGIN)));
  }
}
