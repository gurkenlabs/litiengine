/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.tilemap.IMap;
import de.gurkenlabs.tilemap.MapOrientation;

/**
 * The Interface IMapRenderer.
 */
public interface IMapRenderer {
  /**
   * Gets the map image.
   *
   * @param map
   *          the map
   * @return the map image
   */
  public BufferedImage getMapImage(IMap map);

  /**
   * Gets the supported orientation.
   *
   * @return the supported orientation
   */
  public MapOrientation getSupportedOrientation();

  /**
   * Renders the entire map (without overlay layers) onto the specified graphics
   * object.
   *
   * @param g
   *          the g
   * @param map
   *          the map
   */
  public void render(Graphics2D g, IMap map);

  public void render(Graphics2D g, IMap map, double offsetX, double offsetY);

  /*
   * Renders all layers (without the overlay layers) of the specified map. The
   * viewport defines the region of the map that is about to be rendered.
   */
  public void render(Graphics2D g, IMap map, Rectangle2D viewport);

  public void renderOverlay(Graphics2D g, IMap map, Rectangle2D viewport);
}
