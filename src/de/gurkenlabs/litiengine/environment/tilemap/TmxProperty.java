package de.gurkenlabs.litiengine.environment.tilemap;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation identifies which name is used by the map-object property related to the annotated member.
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TmxProperty {
  /**
   * The name of the annotated member in the context of the TMX map.
   * 
   * @return The name of the TMX map-object property.
   */
  String name();
}
