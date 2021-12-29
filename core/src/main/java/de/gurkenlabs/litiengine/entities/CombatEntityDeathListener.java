package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

/**
 * This listener provides callbacks for when an {@code ICombatEntity} died.
 *
 * @see ICombatEntity#die()
 */
@FunctionalInterface
public interface CombatEntityDeathListener extends EventListener {

  /**
   * This method is called whenever a {@code ICombatEntity} dies.
   *
   * @param entity
   *          The combat entity that died.
   */
  void death(ICombatEntity entity);
}
