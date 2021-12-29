package de.gurkenlabs.litiengine.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by the LITIENGINE to identify methods that should be registered as {@code
 * EntityAction} by the entity framework.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Action {
  /**
   * The name of the {@code EntityAction}.
   *
   * <p>
   * <i> If null or empty, the framework will use the name of the methods that this annotation was declared on. </i>
   *
   * @return The name of the EntityAction.
   */
  String name() default "";

  String description() default "";
}
