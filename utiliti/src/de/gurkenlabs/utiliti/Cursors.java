package de.gurkenlabs.utiliti;

import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.resources.Resources;

public final class Cursors {
  public static final BufferedImage DEFAULT = Resources.images().get("cursor.png");
  public static final BufferedImage ADD = Resources.images().get("cursor-add.png");
  public static final BufferedImage MOVE = Resources.images().get("cursor-move.png");
  public static final BufferedImage SELECT = Resources.images().get("cursor-select.png");
  public static final BufferedImage LOAD = Resources.images().get("cursor-load.png");
  public static final BufferedImage TRANS_HORIZONTAL = Resources.images().get("cursor-trans-horizontal.png");
  public static final BufferedImage TRANS_VERTICAL = Resources.images().get("cursor-trans-vertical.png");
  public static final BufferedImage TRANS_DIAGONAL_LEFT = Resources.images().get("cursor-trans-315.png");
  public static final BufferedImage TRANS_DIAGONAL_RIGHT = Resources.images().get("cursor-trans-45.png");

  private Cursors() {
  }
}
