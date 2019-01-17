package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Dimension;
import java.awt.Point;

import de.gurkenlabs.litiengine.graphics.RenderType;

/**
 * The Interface ILayer.
 */
public interface ILayer extends ICustomPropertyProvider {

  public int getId();
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
   * Gets both the X and the Y offset of the layer.
   *
   * @return a {@link Point} representing the offset
   */
  public Point getOffset();

  /**
   * Gets the horizontal offset of the layer.
   *
   * @return the x offset
   */
  public int getOffsetX();

  /**
   * Gets the vertical offset of the layer.
   *
   * @return the y offset
   */
  public int getOffsetY();

  /**
   * Gets the size in tiles.
   *
   * @return the size in tiles
   */
  public Dimension getSizeInTiles();

  public int getOrder();
  
  public IMap getMap();

  /**
   * Checks if is visible.
   *
   * @return true, if is visible
   */
  public boolean isVisible();

  public void setVisible(boolean visible);

  public void setName(String name);

  public RenderType getRenderType();

  public int getWidth();

  public int getHeight();

  public void setWidth(int newWidth);

  public void setHeight(int newWidth);
}
