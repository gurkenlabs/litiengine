/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.tiled.tmx;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.ITile;
import de.gurkenlabs.tiled.tmx.ITileLayer;
import de.gurkenlabs.tiled.tmx.ITileset;
import de.gurkenlabs.tiled.tmx.MapOrientation;
import de.gurkenlabs.tiled.tmx.utilities.IMapRenderer;
import de.gurkenlabs.tiled.tmx.utilities.LayerRenderType;
import de.gurkenlabs.tiled.tmx.utilities.MapUtilities;
import de.gurkenlabs.util.image.ImageProcessing;

/**
 * The Class OrthogonalMapRenderer.
 */
public class OrthogonalMapRenderer implements IMapRenderer {
  private static final String LAYER_RENDER_TYPE = "RENDERTYPE";
  private float renderProcess;
  private int totalTileCount;
  private int tilesRendered;
  private int partitionsX;
  private int partitionsY;
  private Map<IMap, BufferedImage[][]> imageGrids;

  public OrthogonalMapRenderer() {
    this.partitionsX = 1;
    this.partitionsY = 1;
    this.imageGrids = new ConcurrentHashMap<>();
  }

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
    final ITileset tileset = MapUtilities.findTileSet(map, tile);
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

    final BufferedImage img = RenderEngine.createCompatibleImage((int) map.getSizeInPixles().getWidth(), (int) map.getSizeInPixles().getHeight());
    final Graphics2D g = img.createGraphics();

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

      String renderTypeProp = layer.getCustomProperty(LAYER_RENDER_TYPE);
      if (renderTypeProp != null && !renderTypeProp.isEmpty()) {
        LayerRenderType renderType = LayerRenderType.valueOf(renderTypeProp);
        if (renderType == LayerRenderType.OVERLAY) {
          continue;
        }
      }

      RenderEngine.renderImage(g, this.getLayerImage(layer, map), layer.getPosition());
    }

    g.dispose();

    ImageCache.MAPS.putPersistent(getCacheKey(map), img);
    return img;
  }

  @Override
  public BufferedImage getLayerImage(IMap map, LayerRenderType type) {
    if (ImageCache.MAPS.containsKey(getCacheKey(map) + type)) {
      return ImageCache.MAPS.get(getCacheKey(map) + type);
    }

    final BufferedImage img = RenderEngine.createCompatibleImage((int) map.getSizeInPixles().getWidth(), (int) map.getSizeInPixles().getHeight());
    final Graphics2D g = img.createGraphics();

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

      String renderTypeProp = layer.getCustomProperty(LAYER_RENDER_TYPE);

      if (renderTypeProp == null || renderTypeProp.isEmpty()) {
        continue;
      }
      
      LayerRenderType renderType = LayerRenderType.valueOf(renderTypeProp);
      if (renderType != type) {
        continue;
      }

      RenderEngine.renderImage(g, this.getLayerImage(layer, map), layer.getPosition());
    }

    g.dispose();

    ImageCache.MAPS.putPersistent(getCacheKey(map) + type, img);
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
  public void render(final Graphics2D g, final Point2D offset, final IMap map) {
    // draw all tile layers to the graphics object
    if (!this.imageGrids.containsKey(map)) {
      final BufferedImage mapImage = this.getMapImage(map);
      this.imageGrids.put(map, ImageProcessing.getSubImages(mapImage, this.getPartitionsY(), this.getPartitionsX()));
    }

    Rectangle2D viewPort = Game.getScreenManager().getCamera().getViewPort();
    final BufferedImage[][] imageGrid = this.imageGrids.get(map);
    double cellWidth = map.getSizeInPixles().getWidth() / this.getPartitionsX();
    double cellHeight = map.getSizeInPixles().getHeight() / this.getPartitionsY();
    for (int y = 0; y < this.getPartitionsY(); y++) {
      for (int x = 0; x < this.getPartitionsX(); x++) {
        double cellX = x * cellWidth;
        double cellY = y * cellHeight;
        if (viewPort.intersects(new Rectangle2D.Double(cellX, cellY, cellWidth, cellHeight))) {
          RenderEngine.renderImage(g, imageGrid[y][x], Game.getScreenManager().getCamera().getViewPortLocation(new Point2D.Double(cellX, cellY)));
        }
      }
    }
  }

  @Override
  public void renderLayers(Graphics2D g, Point2D offset, IMap map, LayerRenderType type) {
    final BufferedImage mapImage = this.getLayerImage(map, type);
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
  private synchronized BufferedImage getLayerImage(final ITileLayer layer, final IMap map) {
    // if we have already retrived the image, use the one from the cache to
    // draw the layer
    final String cacheKey = MessageFormat.format("{0}_{1}", getCacheKey(map), layer.getName());
    if (ImageCache.MAPS.containsKey(cacheKey)) {
      return ImageCache.MAPS.get(cacheKey);
    }
    final BufferedImage bufferedImage = ImageProcessing.getCompatibleImage(layer.getSizeInTiles().width * map.getTileSize().width, layer.getSizeInTiles().height * map.getTileSize().height);

    // we need a graphics 2D object to work with transparency
    final Graphics2D imageGraphics = (Graphics2D) bufferedImage.getGraphics();

    // set alpha value of the tiles by the layers value
    final AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
    imageGraphics.setComposite(ac);

    layer.getTiles().parallelStream().forEach((tile) -> {
      // get the tile from the tileset image
      final int index = layer.getTiles().indexOf(tile);
      if (tile.getGridId() == 0) {
        this.tilesRendered++;
        return;
      }

      final Image tileTexture = getTile(map, tile);

      // draw the tile on the map image
      final int x = index % layer.getSizeInTiles().width * map.getTileSize().width;
      final int y = index / layer.getSizeInTiles().width * map.getTileSize().height;
      imageGraphics.drawImage(tileTexture, x, y, null);
      this.tilesRendered++;
      this.renderProcess = this.tilesRendered / (float) this.totalTileCount;
    });

    ImageCache.MAPS.putPersistent(

        getCacheKey(map) + "_" + layer.getName(), bufferedImage);
    return bufferedImage;
  }

  @Override
  public void setPartitionsX(int partitions) {
    this.partitionsX = partitions;
  }

  public int getPartitionsX() {
    return this.partitionsX;
  }

  @Override
  public void setPartitionsY(int partitions) {
    this.partitionsY = partitions;
  }

  public int getPartitionsY() {
    return this.partitionsY;
  }
}
