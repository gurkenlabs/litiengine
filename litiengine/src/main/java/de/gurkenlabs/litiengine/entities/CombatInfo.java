package de.gurkenlabs.litiengine.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides initial values for combat entity attributes. It can be applied to types (classes, interfaces, etc.) and is inherited by
 * subclasses.
 *
 * <p>Attributes:</p>
 * <ul>
 *   <li>hitpoints: The initial hitpoints of the combat entity. Default is {@link CombatEntity#DEFAULT_HITPOINTS}.</li>
 *   <li>team: The team number of the combat entity. Default is 0.</li>
 *   <li>isIndestructible: Indicates whether the combat entity is indestructible. Default is false.</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CombatInfo {
  /**
   * The initial hitpoints of the combat entity.
   *
   * @return the initial hitpoints
   */
  int hitpoints() default CombatEntity.DEFAULT_HITPOINTS;

  /**
   * The team number of the combat entity.
   *
   * @return the team number
   */
  int team() default 0;

  /**
   * Indicates whether the combat entity is indestructible.
   *
   * @return true if the entity is indestructible, false otherwise
   */
  boolean isIndestructible() default false;
}
