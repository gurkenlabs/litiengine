package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines the built-in custom property names that LITIengine recognizes on {@link IMap} instances. These keys are referenced when reading map-level
 * settings such as ambient color or gravity.
 */
public final class MapProperty {
  private static final Logger log = Logger.getLogger(MapProperty.class.getName());

  /**
   * Ambient color tint applied to the map.
   */
  public static final String AMBIENTCOLOR = "AMBIENTLIGHT";
  /**
   * Color used for static shadows on the map.
   */
  public static final String SHADOWCOLOR = "SHADOWCOLOR";
  /** Description text associated with the map. */
  public static final String MAP_DESCRIPTION = "MAP_DESCRIPTION";
  /** Display title of the map. */
  public static final String MAP_TITLE = "MAP_TITLE";
  /** Gravity value applied to physics simulations on this map. */
  public static final String GRAVITY = "GRAVITY";

  private MapProperty() {}

  private static List<String> availableProperties = new ArrayList<>();

  /**
   * Returns the lazily-computed list of all built-in map property names defined by this class.
   *
   * @return the available property names
   */
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

  /**
   * Determines whether the given property name is a custom (user-defined) property.
   *
   * @param name the property name to test
   * @return {@code true} if the name is not one of the built-in properties
   */
  public static boolean isCustom(final String name) {
    return getAvailableProperties().stream().noneMatch(x -> x.equalsIgnoreCase(name));
  }

}
