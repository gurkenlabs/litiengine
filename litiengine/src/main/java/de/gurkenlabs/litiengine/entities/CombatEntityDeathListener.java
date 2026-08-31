package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

/// This listener provides callbacks for when an `ICombatEntity` died.
///
/// @see ICombatEntity#die()
@FunctionalInterface
public interface CombatEntityDeathListener extends EventListener {

  /// This method is called whenever a `ICombatEntity` dies.
  ///
  /// @param entity
  /// The combat entity that died.
  /// @param hitEvent
  /// The hit event that caused the death or null.
  void death(ICombatEntity entity, EntityHitEvent hitEvent);
}
