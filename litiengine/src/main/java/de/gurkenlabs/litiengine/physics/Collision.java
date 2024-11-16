package de.gurkenlabs.litiengine.physics;

/**
 * Enum representing different types of collision behaviors.
 */
public enum Collision {
  /**
   * No collision behavior.
   */
  NONE,

  /**
   * Dynamic collision behavior, typically for moving objects.
   */
  DYNAMIC,

  /**
   * Static collision behavior, typically for immovable objects.
   */
  STATIC,

  /**
   * Any type of collision behavior.
   */
  ANY
}
