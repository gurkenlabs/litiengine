package de.gurkenlabs.litiengine.scripting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks a script field as configurable by bindings and utiLITI.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ScriptProperty {
  String name() default "";

  String description() default "";

  String category() default "Script";

  String type() default "";

  String defaultValue() default "";

  double min() default Double.NEGATIVE_INFINITY;

  double max() default Double.POSITIVE_INFINITY;

  String unit() default "";

  boolean required() default false;
}
