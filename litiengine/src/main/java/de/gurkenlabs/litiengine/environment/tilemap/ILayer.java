package de.gurkenlabs.litiengine.environment.tilemap;

import de.gurkenlabs.litiengine.graphics.RenderType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;

public interface ILayer extends ICustomPropertyProvider {

  /**
   * Gets the unique id of the layer.
   *
   * @return the layer id
   */
  int getId();

  /**
   * Gets the name.
   *
   * @return the name
   */
  String getName();

  /**
   * Sets the name of the layer.
   *
   * @param name the layer name
   */
  void setName(String name);

  /**
   * Gets the opacity.
   *
   * @return the opacity
   */
  float getOpacity();

  /**
   * Sets the opacity of the layer in the range {@code [0, 1]}.
   *
   * @param opacity the opacity to set
   */
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

  /**
   * Gets the map that owns this layer.
   *
   * @return the owning map
   */
  IMap getMap();

  /**
   * Checks if is visible.
   *
   * @return true, if is visible
   */
  boolean isVisible();

  /**
   * Sets whether the layer is visible.
   *
   * @param visible {@code true} to make the layer visible
   */
  void setVisible(boolean visible);

  /**
   * Gets the render type used when drawing this layer.
   *
   * @return the render type
   */
  RenderType getRenderType();

  /**
   * Sets the render type used when drawing this layer.
   *
   * @param renderType the render type
   */
  void setRenderType(RenderType renderType);

  /**
   * Gets the width of the layer, in tiles.
   *
   * @return the width in tiles
   */
  int getWidth();

  /**
   * Sets the width of the layer, in tiles.
   *
   * @param newWidth the new width in tiles
   */
  void setWidth(int newWidth);

  /**
   * Gets the height of the layer, in tiles.
   *
   * @return the height in tiles
   */
  int getHeight();

  /**
   * Sets the height of the layer, in tiles.
   *
   * @param newWidth the new height in tiles
   */
  void setHeight(int newWidth);
}
