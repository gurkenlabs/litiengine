package de.gurkenlabs.litiengine.graphics;

/**
 * Enumerates the offset directions used when rendering a static (baked) shadow. Each value describes the offset of the shadow shape relative to the
 * source map object.
 */
public enum StaticShadowType {
  /**
   * Shadow offset downward.
   */
  DOWN,
  /**
   * Shadow offset down and to the left.
   */
  DOWNLEFT,
  /** Shadow offset down and to the right. */
  DOWNRIGHT,
  /** Shadow offset to the left. */
  LEFT,
  /** Shadow offset to the left and downward. */
  LEFTDOWN,
  /** Shadow offset to the left and the right. */
  LEFTRIGHT,
  /** No shadow is rendered. */
  NONE,
  /** Shadow is rendered without any offset. */
  NOOFFSET,
  /** Shadow offset to the right. */
  RIGHT,
  /** Shadow offset to the right and downward. */
  RIGHTDOWN,
  /** Shadow offset to the right and the left. */
  RIGHTLEFT;

  /**
   * Returns the enum value matching the supplied name, or {@link #NOOFFSET} if the name is blank or does not match any known value.
   *
   * @param mapObjectType the type name
   * @return the matching enum value, or {@link #NOOFFSET}
   */
  public static StaticShadowType get(final String mapObjectType) {
    if (mapObjectType == null || mapObjectType.isEmpty()) {
      return StaticShadowType.NOOFFSET;
    }

    try {
      return StaticShadowType.valueOf(mapObjectType);
    } catch (final IllegalArgumentException iae) {
      return StaticShadowType.NOOFFSET;
    }
  }
}
