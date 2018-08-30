package de.gurkenlabs.litiengine.environment;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
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
    obj.setWidth(entity.getWidth());
    obj.setHeight(entity.getHeight());
    obj.setName(entity.getName());

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

      mapObject.setCustomProperty(property.name(), getPropertyValue(field, value));
    } catch (IllegalArgumentException | IllegalAccessException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public static String getPropertyValue(Field field, Object value) {
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
      return ArrayUtilities.getCommaSeparatedString((List<?>) value);
      // special handling
    }

    if (value.getClass().isArray()) {
      if (field.getType().getComponentType() == int.class) {
        return ArrayUtilities.getCommaSeparatedString((int[]) value);
      } else if (field.getType().getComponentType() == double.class) {
        return ArrayUtilities.getCommaSeparatedString((double[]) value);
      } else if (field.getType().getComponentType() == float.class) {
        return ArrayUtilities.getCommaSeparatedString((float[]) value);
      } else if (field.getType().getComponentType() == short.class) {
        return ArrayUtilities.getCommaSeparatedString((short[]) value);
      } else if (field.getType().getComponentType() == byte.class) {
        return ArrayUtilities.getCommaSeparatedString((byte[]) value);
      } else if (field.getType().getComponentType() == long.class) {
        return ArrayUtilities.getCommaSeparatedString((long[]) value);
      } else if (field.getType().getComponentType() == String.class) {
        return ArrayUtilities.getCommaSeparatedString((String[]) value);
      } else if (field.getType().getComponentType() == boolean.class) {
        return ArrayUtilities.getCommaSeparatedString((boolean[]) value);
      } else {
        return ArrayUtilities.getCommaSeparatedString((Object[]) value);
      }
    }

    return value.toString();
  }
}
