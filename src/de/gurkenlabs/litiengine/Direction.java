package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

/**
 * This class defines the four dimensional directions in 2D space.
 * <p>
 * It can provide a simplified way to look at a rotation or angle which is particularly useful in tile based games.
 * </p>
 * <p>
 * Directions can be converted to or constructed from angles (specified in degree).<br>
 * The directions also specify a flag that can be used to exchange the information in an size-optimized manner (e.g. for network communication).
 * </p>
 * 
 * @see #toFlagValue()
 */
public enum Direction {
  DOWN((byte) 1), LEFT((byte) 2), RIGHT((byte) 4), UNDEFINED((byte) 8), UP((byte) 16);

  private final byte flagValue;

  private Direction(final byte flagValue) {
    this.flagValue = flagValue;
  }

  /**
   * Gets a direction corresponding to the specified angle. Every direction translates to 1/4th (90°) of a full circle.
   * 
   * <pre>
       o 180 o        DOWN = [0-45[ &amp; [315-360]
     o         o      
    o           o     RIGHT = [45-135[
   270          90    
    o           o     UP = [135-225[
     o         o
       o  0  o        LEFT = [225-315[
   * </pre>
   * 
   * @param angle
   *          The angle by which the direction will be determined.
   * @return The direction that corresponds to the specified angle.
   */
  public static Direction fromAngle(final double angle) {
    double actual = GeometricUtilities.normalizeAngle(angle);

    if (actual >= 0 && actual < 45) {
      return Direction.DOWN;
    }
    if (actual >= 45 && actual < 135) {
      return Direction.RIGHT;
    }
    if (actual >= 135 && actual < 225) {
      return Direction.UP;
    }
    if (actual >= 225 && actual < 315) {
      return Direction.LEFT;
    }

    if (actual >= 315 && actual <= 360) {
      return Direction.DOWN;
    }

    return Direction.UNDEFINED;
  }

  /**
   * Get a value of this enumeration that corresponds to the specified flagValue.
   * 
   * @param flagValue
   *          The flag value to convert to a direction.
   * @return A direction that corresponds to the specified flag value or <code>UNDEFINED</code>.
   */
  public static Direction fromFlagValue(final byte flagValue) {
    for (final Direction dir : Direction.values()) {
      if (dir.toFlagValue() == flagValue) {
        return dir;
      }
    }

    return UNDEFINED;
  }

  /**
   * Get the opposite value of this direction.
   * 
   * <pre>
   * UP - DOWN
   * LEFT - RIGHT
   * </pre>
   * 
   * @return The opposite direction.
   */
  public Direction getOpposite() {
    switch (this) {
    case RIGHT:
      return LEFT;
    case UP:
      return DOWN;
    case LEFT:
      return RIGHT;
    case DOWN:
      return UP;
    default:
      return this;
    }
  }

  /**
   * Converts this direction to the median angle of the range that is described by this direction.
   * <pre>
   * e.g. UP 180
   * </pre>
   * 
   * @return The mean angle of this direction.
   */
  public float toAngle() {
    switch (this) {

    case RIGHT:
      return 90;
    case UP:
      return 180;
    case LEFT:
      return 270;
    case DOWN:
      return 360;
    default:
      return 0;
    }
  }

  /**
   * Gets a flag value that can be used to exchange the information of this enum value in an size-optimized manner
   * (e.g. for network communication).
   * 
   * @return The immutable flag value that is assigned to this direction.
   */
  public byte toFlagValue() {
    return this.flagValue;
  }
}
