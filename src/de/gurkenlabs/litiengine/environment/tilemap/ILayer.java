package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Dimension;
import java.awt.Point;

/**
 * The Interface ILayer.
 */
public interface ILayer extends ICustomPropertyProvider {

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName();

  /**
   * Gets the opacity.
   *
   * @return the opacity
   */
  public float getOpacity();

  /**
   * Gets the position.
   *
   * @return the position
   */
  public Point getPosition();

  public int getOffsetX();

  public int getOffsetY();

  /**
   * Gets the size in tiles.
   *
   * @return the size in tiles
   */
  public Dimension getSizeInTiles();

  public int getOrder();

  /**
   * Checks if is visible.
   *
   * @return true, if is visible
   */
  public boolean isVisible();

  public void setName(String name);
}
