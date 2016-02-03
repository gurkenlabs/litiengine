/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.tiled.tmx;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;

import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.ITile;
import de.gurkenlabs.tiled.tmx.ITileLayer;
import de.gurkenlabs.tiled.tmx.ITileset;
import de.gurkenlabs.tiled.tmx.MapOrientation;
import de.gurkenlabs.tiled.tmx.utilities.IMapRenderer;
import de.gurkenlabs.tiled.tmx.utilities.MapUtilities;
import de.gurkenlabs.util.image.ImageProcessing;

/**
 * The Class OrthogonalMapRenderer.
 */
public class OrthogonalMapRenderer implements IMapRenderer {

  private float renderProcess;
  private int totalTileCount;
  private int tilesRendered;

  /**
   * Gets the cache key.
   *
   * @param map
   *          the map
   * @return the cache key
   */
  public static String getCacheKey(final IMap map) {
    return MessageFormat.format("map_{0}_version_{1}", map.getName(), map.getCustomProperty("version") != null ? map.getCustomProperty("version") : map.getVersion());
  }

  public static Image getTile(final IMap map, final ITile tile) {
    final ITileset tileset = MapUtilities.FindTileSet(map, tile);
    if (tileset == null || tileset.getFirstGridId() > tile.getGridId()) {
      return null;
    }

    // get the grid id relative to the sprite sheet since we use a 0 based
    // approach to calculate the position
    final int index = tile.getGridId() - tileset.getFirstGridId();
    final Image img = new Spritesheet(tileset).getSprite(index);
    return ImageProcessing.applyAlphaChannel(img, tileset.getImage().getTransparentColor());
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.graphics.IMapRenderer#getMapImage(de.gurkenlabs.tiled.
   * tmx.IMap)
   */
  @Override
  public BufferedImage getMapImage(final IMap map) {
    if (ImageCache.MAPS.containsKey(getCacheKey(map))) {
      return ImageCache.MAPS.get(getCacheKey(map));
    }

    final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    final GraphicsDevice device = env.getDefaultScreenDevice();
    final GraphicsConfiguration config = device.getDefaultConfiguration();
    final BufferedImage img = config.createCompatibleImage((int) map.getSizeInPixles().getWidth(), (int) map.getSizeInPixles().getHeight(),
        Transparency.TRANSLUCENT);
    final Graphics g = img.createGraphics();

    this.renderProcess = 0;
    this.totalTileCount = 0;
    this.tilesRendered = 0;
    for (final ITileLayer layer : map.getTileLayers()) {
      this.totalTileCount += layer.getTiles().size();
    }
    for (final ITileLayer layer : map.getTileLayers()) {
      if (layer == null) {
        continue;
      }

      RenderEngine.renderImage(g, this.getLayerImage(layer, map), layer.getPosition());
    }

    g.dispose();

    ImageCache.MAPS.putPersistent(getCacheKey(map), img);
    return img;
  }

  @Override
  public float getRenderProgress() {
    return this.renderProcess;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.IMapRenderer#getSupportedOrientation()
   */
  @Override
  public MapOrientation getSupportedOrientation() {
    return MapOrientation.orthogonal;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.IMapRenderer#render(java.awt.Graphics,
   * de.gurkenlabs.tiled.tmx.IMap)
   */
  @Override
  public void render(final Graphics g, final Point2D offset, final IMap map) {
    // draw all tile layers to the graphics object
    final BufferedImage mapImage = this.getMapImage(map);
    RenderEngine.renderImage(g, mapImage, offset);
  }

  /**
   * Gets the layer image.
   *
   * @param layer
   *          the layer
   * @param map
   *          the map
   * @return the layer image
   */
  private BufferedImage getLayerImage(final ITileLayer layer, final IMap map) {
    // if we have already retrived the image, use the one from the cache to
    // draw the layer
    final String cacheKey = MessageFormat.format("{0}_{1}", getCacheKey(map), layer.getName());
    if (ImageCache.MAPS.containsKey(cacheKey)) {
      return ImageCache.MAPS.get(cacheKey);
    }
    final BufferedImage bufferedImage = new BufferedImage(layer.getSizeInTiles().width * map.getTileSize().width,
        layer.getSizeInTiles().height * map.getTileSize().height, BufferedImage.TYPE_INT_ARGB);

    // we need a graphics 2D object to work with transparency
    final Graphics2D imageGraphics = (Graphics2D) bufferedImage.getGraphics();

    // set alpha value of the tiles by the layers value
    final AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
    imageGraphics.setComposite(ac);
    for (int i = 0; i < layer.getTiles().size(); i++) {
      // get the tile from the tileset image
      final ITile tile = layer.getTiles().get(i);
      if (tile.getGridId() == 0) {
        this.tilesRendered++;
        continue;
      }

      final Image tileTexture = getTile(map, tile);

      // draw the tile on the map image
      final int x = i % layer.getSizeInTiles().width * map.getTileSize().width;
      final int y = i / layer.getSizeInTiles().width * map.getTileSize().height;
      imageGraphics.drawImage(tileTexture, x, y, null);
      this.tilesRendered++;
      this.renderProcess = this.tilesRendered / (float) this.totalTileCount;
    }

    ImageCache.MAPS.putPersistent(getCacheKey(map) + "_" + layer.getName(), bufferedImage);
    return bufferedImage;
  }
}
