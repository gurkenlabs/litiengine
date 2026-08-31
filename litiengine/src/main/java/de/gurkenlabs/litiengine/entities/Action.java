package de.gurkenlabs.litiengine.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// This annotation is used by the LITIENGINE to identify methods that should be registered as `EntityAction` by the entity framework.
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Action {
  /// The name of the `EntityAction`.
  ///
  /// *If null or empty, the framework will use the name of the methods that this annotation was declared on.*
  ///
  /// @return The name of the EntityAction.
  String name() default "";

  /// A brief description of the `EntityAction`.
  ///
  /// @return The description of the EntityAction.
  String description() default "";
}
