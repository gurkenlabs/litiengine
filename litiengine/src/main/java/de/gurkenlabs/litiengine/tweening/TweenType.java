package de.gurkenlabs.litiengine.tweening;

/**
 * The TweenType determines which values of a {@code Tweenable} will be modified by a {@code Tween}.
 */
public enum TweenType {
  /**
   * The angle of the entity.
   */
  ANGLE,

  /**
   * Both the width and height of the collision box.
   */
  COLLISION_BOTH,

  /**
   * The height of the collision box.
   */
  COLLISION_HEIGHT,

  /**
   * The width of the collision box.
   */
  COLLISION_WIDTH,

  /**
   * The hitpoints of the entity.
   */
  HITPOINTS,

  /**
   * The X coordinate of the location.
   */
  LOCATION_X,

  /**
   * Both the X and Y coordinates of the location.
   */
  LOCATION_XY,

  /**
   * The Y coordinate of the location.
   */
  LOCATION_Y,

  /**
   * Both the width and height of the size.
   */
  SIZE_BOTH,

  /**
   * The height of the size.
   */
  SIZE_HEIGHT,

  /**
   * The width of the size.
   */
  SIZE_WIDTH,

  /**
   * Undefined type.
   */
  UNDEFINED,

  /**
   * The velocity of the entity.
   */
  VELOCITY,

  /**
   * The volume of the entity.
   */
  VOLUME,

  /**
   * The font size.
   */
  FONTSIZE,

  /**
   * The opacity of the entity.
   */
  OPACITY
}
