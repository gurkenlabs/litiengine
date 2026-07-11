package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Describes an editor-visible property on a project-defined map object type. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapObjectPropertyDefinition {
  String name();

  MapObjectPropertyType type() default MapObjectPropertyType.STRING;

  String defaultValue() default "";

  String displayName() default "";
}
