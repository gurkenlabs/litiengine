package de.gurkenlabs.utiliti;

import de.gurkenlabs.litiengine.resources.Resources;
import java.awt.image.BufferedImage;

/**
 * A utility class that provides various cursor images for the application.
 */
public final class Cursors {
  /**
   * The default cursor image.
   */
  public static final BufferedImage DEFAULT = Resources.images().get("cursor.png");

  /**
   * The cursor image for adding elements.
   */
  public static final BufferedImage ADD = Resources.images().get("cursor-add.png");

  /**
   * The cursor image for moving elements.
   */
  public static final BufferedImage MOVE = Resources.images().get("cursor-move.png");

  /**
   * The cursor image for loading.
   */
  public static final BufferedImage LOAD = Resources.images().get("cursor-load.png");

  /**
   * The cursor image for horizontal transformations.
   */
  public static final BufferedImage TRANS_HORIZONTAL = Resources.images().get("cursor-trans-horizontal.png");

  /**
   * The cursor image for vertical transformations.
   */
  public static final BufferedImage TRANS_VERTICAL = Resources.images().get("cursor-trans-vertical.png");

  /**
   * The cursor image for diagonal transformations to the left.
   */
  public static final BufferedImage TRANS_DIAGONAL_LEFT = Resources.images().get("cursor-trans-315.png");

  /**
   * The cursor image for diagonal transformations to the right.
   */
  public static final BufferedImage TRANS_DIAGONAL_RIGHT = Resources.images().get("cursor-trans-45.png");

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private Cursors() {
  }
}
