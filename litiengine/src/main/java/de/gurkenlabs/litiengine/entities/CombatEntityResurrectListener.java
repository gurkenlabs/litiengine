package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

/// This listener provides callbacks for when an `ICombatEntity` was resurrected.
///
/// @see ICombatEntity#resurrect()
@FunctionalInterface
public interface CombatEntityResurrectListener extends EventListener {
  /// This method is called whenever a `ICombatEntity` was resurrected.
  ///
  /// @param entity
  /// The combat entity that was resurrected.
  void resurrect(ICombatEntity entity);
}
