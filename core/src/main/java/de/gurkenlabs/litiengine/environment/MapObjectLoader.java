package de.gurkenlabs.litiengine.environment;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.util.ReflectionUtilities;

public abstract class MapObjectLoader implements IMapObjectLoader {
  private static final Logger log = Logger.getLogger(MapObjectLoader.class.getName());
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

  /**
   * Loads engine default properties to the specified {@code IEntity} instance:
   * <ul>
   * <li>width, height</li>
   * <li>mapId</li>
   * <li>name</li>
   * <li>location</li>
   * <li>tags</li>
   * </ul>
   * Also, this supports predefined {@code CustomMapObjectProperties}. It loads the specified custom properties via
   * reflection.
   *
   * @param entity
   *          The entity instance that will be initialized.
   * @param mapObject
   *          The mapObject that provides the static information for the new entity.
   * @see TmxProperty
   */
  public static void loadDefaultProperties(IEntity entity, IMapObject mapObject) {
    entity.setMapId(mapObject.getId());
    entity.setWidth(mapObject.getWidth());
    entity.setHeight(mapObject.getHeight());
    entity.setName(mapObject.getName());
    entity.setLocation(mapObject.getLocation());
    if (mapObject.hasCustomProperty(MapObjectProperty.RENDERWITHLAYER)) {
      entity.setRenderWithLayer(mapObject.getBoolValue(MapObjectProperty.RENDERWITHLAYER));
    }

    String tagsString = mapObject.getStringValue(MapObjectProperty.TAGS);
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

    RenderType renderType = mapObject.getEnumValue(MapObjectProperty.RENDERTYPE, RenderType.class);
    if (renderType != null) {
      entity.setRenderType(renderType);
    }

    loadCustomMapObjectProperties(entity, mapObject);

    mapObject.getProperties().forEach((name, property) -> {
      if (MapObjectProperty.isCustom(name)) {
        entity.getProperties().setValue(name, property);
      }
    });
  }

  public void afterLoad(Collection<IEntity> entities, IMapObject mapObject) {
    for (IEntity entity : entities) {
      callAfterTmxUnmarshal(entity, mapObject);
    }
  }

  protected boolean isMatchingType(IMapObject mapObject) {
    if (!mapObject.getType().equalsIgnoreCase(this.getMapObjectType())) {
      log.log(Level.SEVERE, "Cannot load a mapobject of the type [{0}] with a loader of type [{1}].",
          new Object[] {mapObject.getType(), this.getClass()});
      return false;
    }

    return true;
  }

  private static void loadCustomMapObjectProperties(IEntity entity, IMapObject mapObject) {
    for (final Field field : ReflectionUtilities.getAllFields(new ArrayList<Field>(), entity.getClass())) {
      TmxProperty property = field.getAnnotation(TmxProperty.class);

      if (property == null) {
        continue;
      }

      String value = mapObject.getStringValue(property.name(), null);
      if (value == null) {
        continue;
      }

      if (!ReflectionUtilities.setFieldValue(field.getDeclaringClass(), entity, field.getName(), value)) {
        log.warning("entity #" + entity.getMapId() + ": value \"" + value + "\" for custom property " + property.name() + " could not be set.");
      }
    }
  }

  /**
   * If present, this method calls the private {@code afterTmxUnmarshal(IMapObject)} method on the specified entity.
   *
   * <p>
   * The {@link MapObjectLoader} implementation provides the possibility to extend the unmarshalling of the IMapObject
   * within the entity implementation by implementing a private method with the name "afterTmxUnmarshal" accepting a
   * parameter of type {@link IMapObject}. This method is called, after the loading has been finished and the entities
   * have been instantiated.
   * </p>
   *
   * @param entity
   *          The entity instance to call the "afterTmxUnmarshal" method on.
   * @param mapObject
   *          The map object to pass to the entity instance when invoking the "afterTmxUnmarshal" method.
   */
  private void callAfterTmxUnmarshal(IEntity entity, IMapObject mapObject) {
    Method afterTmxUnmarshal = ReflectionUtilities.getMethod("afterTmxUnmarshal", entity.getClass(), IMapObject.class);
    if (afterTmxUnmarshal == null) {
      return;
    }

    afterTmxUnmarshal.setAccessible(true);
    try {
      afterTmxUnmarshal.invoke(entity, mapObject);
    } catch (IllegalAccessException | InvocationTargetException e) {
      log.log(Level.SEVERE, "Could not invoke afterTmxUnmarshal method on type [{0}]: {1}",
          new Object[] {entity.getClass().getName(), e.getMessage()});
    }
  }
}
