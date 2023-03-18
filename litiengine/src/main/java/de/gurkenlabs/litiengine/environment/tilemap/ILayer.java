package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.graphics.RenderType;
import java.awt.Dimension;
import java.awt.Point;

public interface ILayer extends ICustomPropertyProvider {

  int getId();

  /**
   * Gets the name.
   *
   * @return the name
   */
  String getName();

  void setName(String name);

  /**
   * Gets the opacity.
   *
   * @return the opacity
   */
  float getOpacity();

  void setOpacity(float opacity);

  /**
   * Gets both the X and the Y offset of the layer.
   *
   * @return a {@link Point} representing the offset
   */
  Point getOffset();

  /**
   * Gets the horizontal offset of the layer.
   *
   * @return the x offset
   */
  int getOffsetX();

  /**
   * Gets the vertical offset of the layer.
   *
   * @return the y offset
   */
  int getOffsetY();

  /**
   * Gets the size in tiles.
   *
   * @return the size in tiles
   */
  Dimension getSizeInTiles();

  IMap getMap();

  /**
   * Checks if is visible.
   *
   * @return true, if is visible
   */
  boolean isVisible();

  void setVisible(boolean visible);

  RenderType getRenderType();

  void setRenderType(RenderType renderType);

  int getWidth();

  void setWidth(int newWidth);

  int getHeight();

  void setHeight(int newWidth);
}
