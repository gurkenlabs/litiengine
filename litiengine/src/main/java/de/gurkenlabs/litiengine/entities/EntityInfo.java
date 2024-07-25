package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.graphics.RenderType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation contains default values for an entity implementation.
 * It can be used to specify default properties such as height, width, render type,
 * custom map object type, and whether the entity should be rendered with a layer.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EntityInfo {

  /**
   * Specifies the default height of the entity.
   * Defaults to 32.
   *
   * @return the default height of the entity
   */
  float height() default 32;

  /**
   * Specifies the default render type of the entity.
   *  Defaults to {@link RenderType#NORMAL}.
   *
   * @return the default render type of the entity
   */
  RenderType renderType() default RenderType.NORMAL;

  /**
   * Specifies the default width of the entity.
   * Defaults to 32.
   *
   * @return the default width of the entity
   */
  float width() default 32;

  /**
   * Specifies a custom map object type for the entity.
   * Defaults to an empty string.
   *
   * @return the custom map object type of the entity
   */
  String customMapObjectType() default "";

  /**
   * Specifies whether the entity should be rendered with a layer.
   * Defaults to false.
   *
   * @return true if the entity should be rendered with a layer, false otherwise
   */
  boolean renderWithLayer() default false;
}
