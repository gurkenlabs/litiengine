/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.tiled.tmx;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.MapOrientation;

// TODO: Auto-generated Javadoc
/**
 * The Interface IMapRenderer.
 */
public interface IMapRenderer {
  public void setPartitionsX(int partitions);
  public void setPartitionsY(int partitions);
  
  /**
   * Gets the map image.
   *
   * @param map
   *          the map
   * @return the map image
   */
  public BufferedImage getMapImage(IMap map);
  
  public BufferedImage getLayerImage(IMap map, RenderType type);

  public float getRenderProgress();

  /**
   * Gets the supported orientation.
   *
   * @return the supported orientation
   */
  public MapOrientation getSupportedOrientation();

  /**
   * Render.
   *
   * @param g
   *          the g
   * @param map
   *          the map
   */
  public void render(Graphics2D g, Point2D offset, IMap map);

  public void renderLayers(Graphics2D g, Point2D offset, IMap map, RenderType type);
}
