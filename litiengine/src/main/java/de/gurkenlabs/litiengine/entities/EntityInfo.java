package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.graphics.RenderType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** This annotation contains default values for an entity implementation. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EntityInfo {

  float height() default 32;

  RenderType renderType() default RenderType.NORMAL;

  float width() default 32;

  String customMapObjectType() default "";

  boolean renderWithLayer() default false;
}
