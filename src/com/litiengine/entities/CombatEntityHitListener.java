package com.litiengine.entities;

import com.litiengine.abilities.Ability;

import java.util.EventListener;

/**
 * This listener provides callbacks for when an {@code ICombatEntity} was hit.
 * 
 * @see ICombatEntity#hit(int)
 * @see ICombatEntity#hit(int, Ability)
 */
@FunctionalInterface
public interface CombatEntityHitListener extends EventListener {

  /**
   * This method is called whenever a {@code ICombatEntity} was hit.
   * 
   * @param event
   *          The event data that contains information about the entity, for how much it was hit and the ability that caused the hit.
   */
  void hit(EntityHitEvent event);
}
