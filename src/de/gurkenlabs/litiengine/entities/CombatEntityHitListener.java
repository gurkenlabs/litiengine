package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

/**
 * This listener provides callbacks for when an <code>ICombatEntity</code> was hit.
 * 
 * @see ICombatEntity#hit(int)
 * @see ICombatEntity#hit(int, de.gurkenlabs.litiengine.abilities.Ability)
 */
@FunctionalInterface
public interface CombatEntityHitListener extends EventListener {

  /**
   * This method is called whenever a <code>ICombatEntity</code> was hit.
   * 
   * @param event
   *          The event data that contains information about the entity, for how much it was hit and the ability that caused the hit.
   */
  public void hit(EntityHitEvent event);
}
