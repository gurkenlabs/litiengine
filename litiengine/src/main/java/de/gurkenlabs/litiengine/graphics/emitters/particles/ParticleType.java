package de.gurkenlabs.litiengine.graphics.emitters.particles;

/**
 * Enumerates the shape/render types supported by the engine's built-in {@link Particle} implementations.
 */
public enum ParticleType {
  /**
   * Rectangular particle.
   */
  RECTANGLE,
  /**
   * Elliptic particle.
   */
  ELLIPSE,
  /** Triangular particle. */
  TRIANGLE,
  /** Diamond-shaped particle. */
  DIAMOND,
  /** Line particle. */
  LINE,
  /** Text particle. */
  TEXT,
  /** Sprite-based particle. */
  SPRITE
}
