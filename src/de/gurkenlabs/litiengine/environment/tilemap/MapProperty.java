package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MapProperty {
  private static final Logger log = Logger.getLogger(MapProperty.class.getName());

  public static final String AMBIENTCOLOR = "AMBIENTLIGHT";
  public static final String SHADOWCOLOR = "SHADOWCOLOR";
  public static final String MAP_DESCRIPTION = "MAP_DESCRIPTION";
  public static final String MAP_TITLE = "MAP_TITLE";
  public static final String GRAVITY = "GRAVITY";

  private MapProperty() {
  }

  private static List<String> availableProperties = new ArrayList<>();

  public static List<String> getAvailableProperties() {
    if (availableProperties.isEmpty()) {
      for (final Field field : MapProperty.class.getDeclaredFields()) {
        if (field.getType() == String.class && Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
          try {
            availableProperties.add((String) field.get(null));
          } catch (final IllegalArgumentException | IllegalAccessException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
          }
        }
      }
    }
    return availableProperties;
  }

  public static boolean isCustom(final String name) {
    return getAvailableProperties().stream().noneMatch(x -> x.equalsIgnoreCase(name));
  }

}
