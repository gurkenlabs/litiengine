package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.resources.Resources;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Thread-safe registry that indexes all annotated map object properties and types in LITIengine.
///
/// Provides canonical English documentation, data types, default values, and category groupings
/// for MCP exposure and in-editor documentation, while resolving translations via [Resources#strings()].
public final class TmxPropertyMetadataRegistry {
  private static final Logger log = Logger.getLogger(TmxPropertyMetadataRegistry.class.getName());

  /// Documentation and serialization metadata for one TMX property.
  ///
  /// @param name The serialized property name.
  /// @param description The canonical English description.
  /// @param category The editor grouping.
  /// @param type The serialized value type.
  /// @param defaultValue The default represented as text.
  /// @param resourceKey The optional localization key.
  public record PropertyMetadata(
      String name,
      String description,
      String category,
      String type,
      String defaultValue,
      String resourceKey) {

    /// Gets the localized description if a resource key translation is available,
    /// falling back to the canonical English description.
    ///
    /// @return The localized or canonical description.
    public String getTranslatedDescription() {
      if (resourceKey != null && !resourceKey.isEmpty()) {
        try {
          String translated = Resources.strings().get(resourceKey);
          if (translated != null && !translated.isEmpty() && !translated.startsWith("[")) {
            return translated;
          }
        } catch (Exception ignored) {
        }
      }
      return description;
    }
  }

  /// Documentation and localization metadata for one TMX object type.
  ///
  /// @param typeName The serialized type identifier.
  /// @param displayName The editor-facing type name.
  /// @param description The canonical English description.
  /// @param resourceKey The optional localization key.
  public record TypeMetadata(
      String typeName,
      String displayName,
      String description,
      String resourceKey) {

    /// Gets the localized description if available, falling back to the canonical English description.
    ///
    /// @return The localized or canonical description.
    public String getTranslatedDescription() {
      if (resourceKey != null && !resourceKey.isEmpty()) {
        try {
          String translated = Resources.strings().get(resourceKey);
          if (translated != null && !translated.isEmpty() && !translated.startsWith("[")) {
            return translated;
          }
        } catch (Exception ignored) {
        }
      }
      return description;
    }
  }

  private static volatile Map<String, PropertyMetadata> PROPERTIES = Collections.emptyMap();
  private static volatile Map<String, TypeMetadata> TYPES = Collections.emptyMap();

  static {
    initialize();
  }

  private TmxPropertyMetadataRegistry() {
  }

  /// Rebuilds the registry from the engine's annotated TMX model classes.
  ///
  /// Existing immutable snapshots are replaced atomically after indexing completes.
  public static synchronized void initialize() {
    Map<String, PropertyMetadata> newProperties = new LinkedHashMap<>();
    Map<String, TypeMetadata> newTypes = new LinkedHashMap<>();

    // Index native MapObject fields and MapObjectProperty static fields across main and nested classes
    registerPropertiesFromClass(MapObject.class, newProperties);
    registerPropertiesFromClass(MapObjectProperty.class, newProperties);
    registerPropertiesFromClass(MapObjectProperty.Emitter.class, newProperties);
    registerPropertiesFromClass(MapObjectProperty.Particle.class, newProperties);

    // Index MapObjectType enum values
    for (MapObjectType type : MapObjectType.values()) {
      try {
        Field enumField = MapObjectType.class.getField(type.name());
        if (enumField.isAnnotationPresent(TmxTypeInfo.class)) {
          TmxTypeInfo info = enumField.getAnnotation(TmxTypeInfo.class);
          TypeMetadata meta = new TypeMetadata(
              type.name(),
              info.name(),
              info.description(),
              info.resourceKey());
          newTypes.put(type.name(), meta);
        } else {
          newTypes.put(type.name(), new TypeMetadata(type.name(), type.name(), "", ""));
        }
      } catch (Exception ignored) {
        newTypes.put(type.name(), new TypeMetadata(type.name(), type.name(), "", ""));
      }
    }

    PROPERTIES = Collections.unmodifiableMap(newProperties);
    TYPES = Collections.unmodifiableMap(newTypes);
  }

  private static void registerPropertiesFromClass(Class<?> clazz, Map<String, PropertyMetadata> targetMap) {
    for (Field field : clazz.getDeclaredFields()) {
      if (field.isAnnotationPresent(TmxPropertyInfo.class)) {
        try {
          TmxPropertyInfo info = field.getAnnotation(TmxPropertyInfo.class);
          String propertyName = info.name();
          if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
            String val = (String) field.get(null);
            if (val != null && !val.isEmpty()) {
              propertyName = val;
            }
          }
          PropertyMetadata meta = new PropertyMetadata(
              propertyName,
              info.description(),
              info.category(),
              info.type(),
              info.defaultValue(),
              info.resourceKey());
          targetMap.put(meta.name(), meta);
        } catch (Exception e) {
          log.log(Level.WARNING, "Failed to register property metadata for field: " + field.getName(), e);
        }
      }
    }
  }

  /// Retrieves metadata for a registered TMX property by name.
  ///
  /// @param propertyName The property name key.
  /// @return The [PropertyMetadata], or `null` if not registered.
  public static PropertyMetadata getProperty(String propertyName) {
    return propertyName != null ? PROPERTIES.get(propertyName) : null;
  }

  /// Retrieves all registered property metadata objects.
  ///
  /// @return Unmodifiable list of property metadata.
  public static List<PropertyMetadata> getAllProperties() {
    return Collections.unmodifiableList(new ArrayList<>(PROPERTIES.values()));
  }

  /// Retrieves all properties belonging to a specific category.
  ///
  /// @param category The category name.
  /// @return List of matching property metadata objects.
  public static List<PropertyMetadata> getPropertiesByCategory(String category) {
    if (category == null || category.isEmpty()) {
      return getAllProperties();
    }
    List<PropertyMetadata> list = new ArrayList<>();
    for (PropertyMetadata meta : PROPERTIES.values()) {
      if (category.equalsIgnoreCase(meta.category())) {
        list.add(meta);
      }
    }
    return Collections.unmodifiableList(list);
  }

  /// Retrieves metadata for a map object type.
  ///
  /// @param typeName The type name string.
  /// @return The [TypeMetadata], or `null` if not registered.
  public static TypeMetadata getType(String typeName) {
    return typeName != null ? TYPES.get(typeName) : null;
  }

  /// Retrieves all registered map object type metadata objects.
  ///
  /// @return Unmodifiable list of type metadata.
  public static List<TypeMetadata> getAllTypes() {
    return Collections.unmodifiableList(new ArrayList<>(TYPES.values()));
  }
}
