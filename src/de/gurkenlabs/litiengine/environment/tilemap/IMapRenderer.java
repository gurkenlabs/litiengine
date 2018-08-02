package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.graphics.RenderType;

public interface IMapRenderer {

  /**
   * Gets a bufferedImage of the current map containing all tile layers.
   * 
   * @param map
   *          the map
   * @param renderTypes
   *          the renderTypes we want to be displayed in our image
   * @return an image containing all the map's tile layers with the specified {@link RenderType}s
   */
  public BufferedImage getImage(IMap map, RenderType... renderTypes);

  /**
   * Gets a bufferedImage of the current map containing all tile layers.
   * 
   * @param map
   *          the map
   * @return an image containing all the map's tile layers
   */
  public BufferedImage getImage(IMap map);

  /**
   * Gets the supported orientation.
   *
   * @return the supported orientation
   */
  public MapOrientation getSupportedOrientation();

  /**
   * Renders all layers of the specified <code>Map</code> onto the graphics object that meet the specified render types.
   *
   * @param g
   *          the graphics object
   * @param map
   *          the map
   * @param renderTypes
   *          All layers that have one of the specified render types will be rendered.
   *          If nothing is specified, layers will be rendered independently of their render type.
   * 
   * @see ILayer#getRenderType()
   * 
   */
  public void render(Graphics2D g, IMap map, RenderType... renderTypes);

  /**
   * Renders all layers of the specified <code>Map</code> onto the graphics object that meet the specified render types.
   *
   * @param g
   *          the graphics object
   * @param map
   *          the map
   * @param offsetX
   *          The horizontal offset in pixels for the map image that is applied
   *          when rendering on the graphics object.
   * @param offsetY
   *          The vertical offset in pixels for the map image that is applied
   *          when rendering on the graphics object.
   * @param renderTypes
   *          All layers that have one of the specified render types will be rendered.
   *          If nothing is specified, layers will be rendered independently of their render type.
   * 
   * @see ILayer#getRenderType()
   */
  public void render(Graphics2D g, IMap map, double offsetX, double offsetY, RenderType... renderTypes);

  /**
   * Renders all layers of the specified <code>Map</code> onto the graphics object that meet the specified render types.
   * 
   * @param g
   *          the graphics object
   * @param map
   *          the map
   * @param viewport
   *          The viewport that defines the bound for the tiles to be rendered.
   * @param renderTypes
   *          All layers that have one of the specified render types will be rendered.
   *          If nothing is specified, layers will be rendered independently of their render type.
   * 
   * @see ILayer#getRenderType()
   */
  public void render(Graphics2D g, IMap map, Rectangle2D viewport, RenderType... renderTypes);
}
