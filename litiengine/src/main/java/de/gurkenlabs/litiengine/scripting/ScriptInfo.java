package de.gurkenlabs.litiengine.scripting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Describes an editor-visible Java script implementation. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScriptInfo {
  String id();

  String name() default "";

  ScriptHostType host() default ScriptHostType.ENTITY;

  Class<?> target() default Object.class;
}
