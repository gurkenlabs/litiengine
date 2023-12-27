package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.graphics.RenderType;

import java.awt.*;
import java.awt.geom.Point2D;

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
   * @return a {@link Point2D} representing the offset
   */
  Point2D getOffset();

  /**
   * Gets the horizontal offset of the layer.
   *
   * @return the x offset
   */
  double getOffsetX();

  /**
   * Gets the vertical offset of the layer.
   *
   * @return the y offset
   */
  double getOffsetY();

  /**
   * Gets the horizontal parallax scrolling factor of the layer. Defaults to 1.0.
   *
   * @return The horizontal parallax scrolling factor.
   */
  double getHorizontalParallaxFactor();

  /**
   * Gets the vertical parallax scrolling factor of the layer. Defaults to 1.0.
   *
   * @return The vertical parallax scrolling factor.
   */
  double getVerticalParallaxFactor();

  /**
   * Gets a tint color that affects the way contents of this layer are rendered.
   *
   * @return A color that is used to tint the visible contents of this layer.
   */
  Color getTintColor();

  /**
   * Sets the tint color of this layer.
   *
   * @param tintcolor The tint color of this layer.
   */
  void setTintColor(Color tintcolor);

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
