package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Provides metadata for built-in map object types, including canonical English names,
/// descriptions, and translation keys.
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TmxTypeInfo {

  /// Human-readable English display name for the map object type.
  ///
  /// @return The display name.
  String name();

  /// Canonical English description of the map object type.
  ///
  /// @return The English description.
  String description() default "";

  /// Localization resource key for translated descriptions.
  ///
  /// @return The resource key.
  String resourceKey() default "";
}
