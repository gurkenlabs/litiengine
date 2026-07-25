package de.gurkenlabs.litiengine.environment.tilemap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks an entity implementation as a map object type that game tooling can discover. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapObjectDefinition {
  /** Stable implementation identifier stored in {@link MapObjectProperty#IMPLEMENTATION}. */
  String id();

  /** Built-in map object behavior and editor panels inherited by this type. */
  MapObjectType baseType();

  String displayName() default "";

  MapObjectPropertyDefinition[] properties() default {};
}
