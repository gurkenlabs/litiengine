package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

/**
 * This listener provides callbacks for when an <code>ICombatEntity</code> dies, was resurrected or is being hit.
 * 
 * @see ICombatEntity#die()
 * @see ICombatEntity#resurrect()
 * @see ICombatEntity#hit(int)
 */
public interface CombatEntityListener extends CombatEntityHitListener, CombatEntityDeathListener, EventListener {

  /**
   * This method is called whenever a <code>ICombatEntity</code> was resurrected.
   * 
   * @param entity
   *          The combat entity that was resurrected.
   */
  public void onResurrection(ICombatEntity entity);
}
