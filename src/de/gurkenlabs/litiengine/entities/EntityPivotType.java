package de.gurkenlabs.litiengine.entities;

/**
 * Determines where abilities are originating from. This also determines the location of the effect
 * shape.
 */
public enum EntityPivotType {
  COLLISIONBOX_CENTER,
  OFFSET,
  DIMENSION_CENTER,
  LOCATION;
}
