package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides rich metadata for a TMX map object property, including canonical English documentation,
 * category grouping, value type, default values, and translation keys for localization.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TmxPropertyInfo {

  /**
   * The property name key as stored in TMX map objects.
   *
   * @return The TMX property key name.
   */
  String name();

  /**
   * Canonical English description of the property for documentation and tooltips.
   *
   * @return The English description.
   */
  String description() default "";

  /**
   * Category or group name for organizing properties in inspectors and docs (e.g. "Combat", "Collision", "Graphics").
   *
   * @return The property category.
   */
  String category() default "General";

  /**
   * Expected value type (e.g. "string", "int", "float", "boolean", "color", "enum").
   *
   * @return The value type string.
   */
  String type() default "string";

  /**
   * Default value representation string.
   *
   * @return The default value string.
   */
  String defaultValue() default "";

  /**
   * Localization resource key for translated descriptions.
   *
   * @return The resource key.
   */
  String resourceKey() default "";
}
