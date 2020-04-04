package de.gurkenlabs.litiengine.environment;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.DecimalFloatAdapter;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

public final class MapObjectSerializer {
  private static final Logger log = Logger.getLogger(MapObjectSerializer.class.getName());

  private MapObjectSerializer() {
  }

  public static MapObject serialize(IEntity entity) {
    MapObject obj = new MapObject();
    obj.setId(entity.getMapId());
    obj.setX((float) entity.getX());
    obj.setY((float) entity.getY());
    obj.setWidth((float) entity.getWidth());
    obj.setHeight((float) entity.getHeight());
    obj.setName(entity.getName());

    TmxType type = entity.getClass().getAnnotation(TmxType.class);
    if (type != null) {
      obj.setType(type.value().toString());
    }

    serialize(entity.getClass(), entity, obj);
    return obj;
  }

  private static <T extends IEntity> void serialize(Class<?> clz, T entity, MapObject mapObject) {

    for (final Field field : clz.getDeclaredFields()) {
      serialize(field, entity, mapObject);
    }

    // recursively call all parent classes and serialize annotated fields
    Class<?> parentClass = clz.getSuperclass();
    if (parentClass != null) {
      serialize(parentClass, entity, mapObject);
    }
  }

  private static void serialize(Field field, Object entity, IMapObject mapObject) {
    TmxProperty property = field.getAnnotation(TmxProperty.class);
    if (property == null) {
      return;
    }

    Object value;
    try {
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }

      value = field.get(entity);
      if (value == null) {
        return;
      }

      mapObject.setValue(property.name(), getPropertyValue(field, value));
    } catch (IllegalAccessException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private static String getPropertyValue(Field field, Object value) {
    if (field.getType().equals(Float.class) || field.getType().equals(Double.class)) {
      try {
        return new DecimalFloatAdapter().marshal((Float) value);
      } catch (Exception e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    } else if (field.getType().equals(Integer.class)) {
      return Integer.toString((int) value);
    } else if (field.getType().equals(short.class)) {
      return Short.toString((short) value);
    } else if (field.getType().equals(byte.class)) {
      return Byte.toString((byte) value);
    } else if (field.getType().equals(long.class)) {
      return Long.toString((long) value);
    }

    if (value instanceof List<?>) {
      return ArrayUtilities.join((List<?>) value);
      // special handling
    }

    if (value.getClass().isArray()) {
      if (field.getType().getComponentType() == int.class) {
        return ArrayUtilities.join((int[]) value);
      } else if (field.getType().getComponentType() == double.class) {
        return ArrayUtilities.join((double[]) value);
      } else if (field.getType().getComponentType() == float.class) {
        return ArrayUtilities.join((float[]) value);
      } else if (field.getType().getComponentType() == short.class) {
        return ArrayUtilities.join((short[]) value);
      } else if (field.getType().getComponentType() == byte.class) {
        return ArrayUtilities.join((byte[]) value);
      } else if (field.getType().getComponentType() == long.class) {
        return ArrayUtilities.join((long[]) value);
      } else if (field.getType().getComponentType() == String.class) {
        return ArrayUtilities.join((String[]) value);
      } else if (field.getType().getComponentType() == boolean.class) {
        return ArrayUtilities.join((boolean[]) value);
      } else {
        return ArrayUtilities.join((Object[]) value);
      }
    }

    return value.toString();
  }
}
