package de.gurkenlabs.litiengine.entities;

/**
 * The {@code EntityPivotType} enum defines the types of pivot points that can be used for entities within the game. A pivot point determines the
 * reference point for positioning and rotating entities.
 */
public enum EntityPivotType {
  /**
   * Represents the center of the entity's collision box. This pivot type is useful for entities where interactions or collisions are centered around
   * the entity's collision box rather than its entity dimensions.
   */
  COLLISIONBOX_CENTER,
  /**
   * Represents the center of the entity's dimensions. This is commonly used for graphical alignment, ensuring that rotations or scaling operations
   * are centered on the visual representation of the entity.
   */
  DIMENSION_CENTER,
  /**
   * Represents the entity's current location, defined as its top left corner. This pivot type is typically used for simple positioning where the
   * reference object has the same dimensions as the reference entity.
   */
  LOCATION,
  /**
   * Represents a distribution or spread of entities or effects over the entire bounding box of the reference entity. This pivot type can be used for
   * scenarios where an entity's effect or presence needs to be distributed across an area, rather than focused on a single point. For example, it
   * could be used for area-of-effect spells, explosions, or spreading Props or Creatures in a game environment.
   */
  SPREAD
}
