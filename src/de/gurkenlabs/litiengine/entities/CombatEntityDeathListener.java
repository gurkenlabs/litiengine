package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

/**
 * This listener provides callbacks for when an <code>ICombatEntity</code> died.
 * 
 * @see ICombatEntity#die()
 */
@FunctionalInterface
public interface CombatEntityDeathListener extends EventListener {
  
  /**
   * This method is called whenever a <code>ICombatEntity</code> dies.
   * 
   * @param entity
   *          The combat entity that died.
   */
  public void death(ICombatEntity entity);
}
