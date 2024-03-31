package de.gurkenlabs.litiengine.abilities;

import de.gurkenlabs.litiengine.entities.EntityPivotType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code AbilityInfo} annotation is used to define the properties of an Ability. It includes information such as cast type, cooldown,
 * description, duration, impact, impact angle, multi-target capability, name, origin, pivot offsets, range, and value.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AbilityInfo {
  /**
   * The cast type of the ability.
   *
   * @return The cast type of the ability
   */
  CastType castType() default CastType.INSTANT;

  /**
   * The cooldown of the ability.
   *
   * @return The cooldown of the ability
   */
  int cooldown() default 0;

  /**
   * The description of the ability.
   *
   * @return The description of the ability
   */
  String description() default "";

  /**
   * The duration of the ability.
   *
   * @return The duration of the ability
   */
  int duration() default 0;

  /**
   * The impact of the ability.
   *
   * @return The impact of the ability
   */
  int impact() default 0;

  /**
   * The impact angle of the ability.
   *
   * @return The impact angle of the ability
   */
  int impactAngle() default 360;

  /**
   * Whether the ability is multi-target.
   *
   * @return {@code true} if the ability is multi-target; {@code false} otherwise
   */
  boolean multiTarget() default false;

  /**
   * The name of the ability.
   *
   * @return The name of the ability
   */
  String name() default "";

  /**
   * The origin of the ability.
   *
   * @return The origin of the ability
   */
  EntityPivotType origin() default EntityPivotType.COLLISIONBOX_CENTER;

  /**
   * The pivot offset X of the ability.
   *
   * @return The pivot offset X of the ability
   */
  double pivotOffsetX() default 0;

  /**
   * The pivot offset Y of the ability.
   *
   * @return The pivot offset Y of the ability
   */
  double pivotOffsetY() default 0;

  /**
   * The range of the ability.
   *
   * @return The range of the ability
   */
  int range() default 0;

  /**
   * The value of the ability.
   *
   * @return The value of the ability
   */
  int value() default 0;
}
