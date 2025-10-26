package de.gurkenlabs.litiengine.environment;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.DecimalFloatAdapter;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class MapObjectSerializer {
  private static final Logger log = Logger.getLogger(MapObjectSerializer.class.getName());

  private MapObjectSerializer() {}

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

    if (!entity.getTags().isEmpty()) {
      obj.setValue(MapObjectProperty.TAGS, String.join(",", entity.getTags()));
    }

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
      if (!field.canAccess(entity)) {
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
    Class<?> type = field.getType();
    if (type.equals(Float.class) || type.equals(Double.class)) {
      try {
        return new DecimalFloatAdapter().marshal((Float) value);
      } catch (Exception e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    if (value instanceof List<?> list) {
      return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    if (value.getClass().isArray()) {
      if (type.getComponentType() == int.class) {
        return Arrays.stream((int[]) value).mapToObj(String::valueOf).collect(Collectors.joining(","));
      } else if (type.getComponentType() == double.class) {
        return Arrays.stream((double[]) value).mapToObj(String::valueOf).collect(Collectors.joining(","));
      } else if (type.getComponentType() == float.class) {
        String[] arr = new String[((float[]) value).length];
        float[] floatArr = (float[]) value;
        for (int i = 0; i < arr.length; i++) {
          arr[i] = String.valueOf(floatArr[i]);
        }
        return String.join(",", arr);
      } else if (type.getComponentType() == short.class) {
        String[] arr = new String[((short[]) value).length];
        short[] shortArr = (short[]) value;
        for (int i = 0; i < arr.length; i++) {
          arr[i] = String.valueOf(shortArr[i]);
        }
        return String.join(",", arr);
      } else if (type.getComponentType() == byte.class) {
        String[] arr = new String[((byte[]) value).length];
        byte[] byteArr = (byte[]) value;
        for (int i = 0; i < arr.length; i++) {
          arr[i] = String.valueOf(byteArr[i]);
        }
        return String.join(",", arr);
      } else if (type.getComponentType() == long.class) {
        return Arrays.stream((long[]) value).mapToObj(String::valueOf).collect(Collectors.joining(","));
      } else if (type.getComponentType() == String.class) {
        return String.join(",", (String[]) value);
      } else if (type.getComponentType() == boolean.class) {
        String[] arr = new String[((boolean[]) value).length];
        boolean[] boolArr = (boolean[]) value;
        for (int i = 0; i < arr.length; i++) {
          arr[i] = String.valueOf(boolArr[i]);
        }
        return String.join(",", arr);
      } else {
        return Arrays.stream((Object[]) value).map(String::valueOf).collect(Collectors.joining(","));
      }
    }

    return value.toString();
  }
}
