package com.litiengine.entities;

import java.util.EventListener;

/**
 * This listener provides callbacks for when an {@code ICombatEntity} dies, was resurrected or is being hit.
 * 
 * @see ICombatEntity#die()
 * @see ICombatEntity#resurrect()
 * @see ICombatEntity#hit(int)
 */
public interface CombatEntityListener extends CombatEntityHitListener, CombatEntityDeathListener, CombatEntityResurrectListener, EventListener {
}
