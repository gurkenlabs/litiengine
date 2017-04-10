/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.tilemap.IImageLayer;
import de.gurkenlabs.tilemap.ILayer;
import de.gurkenlabs.tilemap.IMap;
import de.gurkenlabs.tilemap.ITile;
import de.gurkenlabs.tilemap.ITileAnimation;
import de.gurkenlabs.tilemap.ITileAnimationFrame;
import de.gurkenlabs.tilemap.ITileLayer;
import de.gurkenlabs.tilemap.ITileset;
import de.gurkenlabs.tilemap.MapOrientation;
import de.gurkenlabs.tilemap.utilities.MapUtilities;
import de.gurkenlabs.util.MathUtilities;
import de.gurkenlabs.util.image.ImageProcessing;

/**
 * The Class OrthogonalMapRenderer.
 */
public class OrthogonalMapRenderer implements IMapRenderer {
  private static final String LAYER_RENDER_TYPE = "RENDERTYPE";

  /**
   * Gets the cache key.
   *
   * @param map
   *          the map
   * @return the cache key
   */
  public static String getCacheKey(final IMap map) {
    return MessageFormat.format("map_{0}_version_{1}", map.getFileName(), map.getCustomProperty("version") != null ? map.getCustomProperty("version") : map.getVersion());
  }

  private static Image getTileImage(final IMap map, final ITile tile) {
    if (tile == null) {
      return null;
    }

    final ITileset tileset = MapUtilities.findTileSet(map, tile);
    if (tileset == null || tileset.getFirstGridId() > tile.getGridId()) {
      return null;
    }

    Spritesheet sprite = Spritesheet.find(tileset.getImage().getSource());
    if (sprite == null) {
      sprite = Spritesheet.load(tileset);
      if (sprite == null) {
        return null;
      }
    }

    // get the grid id relative to the sprite sheet since we use a 0 based
    // approach to calculate the position
    int index = tile.getGridId() - tileset.getFirstGridId();

    // support for animated tiles
    final ITileAnimation animation = MapUtilities.getAnimation(map, index);
    if (animation != null && animation.getFrames().size() > 0) {
      final long playedMs = Game.getLoop().getTime().getMilliseconds();

      final int totalDuration = animation.getTotalDuration();
      final long animationsPlayed = playedMs / totalDuration;

      final long deltaTicks = playedMs - animationsPlayed * totalDuration;
      int currentPlayTime = 0;
      for (final ITileAnimationFrame frame : animation.getFrames()) {
        currentPlayTime += frame.getDuration();
        if (deltaTicks < currentPlayTime) {
          // found the current animation frame
          index = frame.getTileId();
          break;
        }
      }

    }

    final Image img = sprite.getSprite(index);
    return img;
  }

  public OrthogonalMapRenderer() {
  }

  @Override
  public BufferedImage getMapImage(final IMap map) {
    if (ImageCache.MAPS.containsKey(getCacheKey(map))) {
      return ImageCache.MAPS.get(getCacheKey(map));
    }

    final BufferedImage img = RenderEngine.createCompatibleImage((int) map.getSizeInPixels().getWidth(), (int) map.getSizeInPixels().getHeight());
    final Graphics2D g = img.createGraphics();

    for (final ITileLayer layer : map.getTileLayers()) {
      if (layer == null) {
        continue;
      }

      final String renderTypeProp = layer.getCustomProperty(LAYER_RENDER_TYPE);
      if (renderTypeProp != null && !renderTypeProp.isEmpty()) {
        final RenderType renderType = RenderType.valueOf(renderTypeProp);
        if (renderType == RenderType.OVERLAY) {
          continue;
        }
      }

      RenderEngine.renderImage(g, this.getLayerImage(layer, map), layer.getPosition());
    }

    g.dispose();

    ImageCache.MAPS.put(getCacheKey(map), img);
    return img;
  }

  @Override
  public MapOrientation getSupportedOrientation() {
    return MapOrientation.orthogonal;
  }

  @Override
  public void render(final Graphics2D g, final IMap map) {
    this.render(g, map, 0, 0);
  }

  @Override
  public void render(final Graphics2D g, final IMap map, final double offsetX, final double offsetY) {
    final BufferedImage mapImage = this.getMapImage(map);
    RenderEngine.renderImage(g, mapImage, offsetX, offsetY);
  }

  @Override
  public void render(final Graphics2D g, final IMap map, final Rectangle2D viewport) {

    for (final ILayer layer : this.getAllRenderLayers(map)) {
      if (layer == null) {
        continue;
      }

      final String renderTypeProp = layer.getCustomProperty(LAYER_RENDER_TYPE);
      if (renderTypeProp != null && !renderTypeProp.isEmpty()) {
        final RenderType renderType = RenderType.valueOf(renderTypeProp);
        if (renderType == RenderType.OVERLAY) {
          continue;
        }
      }

      if (layer instanceof ITileLayer) {
        this.renderTileLayerImage(g, (ITileLayer) layer, map, viewport);
      }

      if (layer instanceof IImageLayer) {
        this.renderImageLayer(g, (IImageLayer) layer, map, viewport);
      }
    }
  }

  @Override
  public void renderOverlay(final Graphics2D g, final IMap map, final Rectangle2D viewport) {
    for (final ITileLayer layer : map.getTileLayers()) {
      if (layer == null) {
        continue;
      }

      final String renderTypeProp = layer.getCustomProperty(LAYER_RENDER_TYPE);
      if (renderTypeProp == null || renderTypeProp.isEmpty()) {
        continue;
      }

      final RenderType renderType = RenderType.valueOf(renderTypeProp);
      if (renderType != RenderType.OVERLAY) {
        continue;
      }

      this.renderTileLayerImage(g, layer, map, viewport);
    }
  }

  private List<ILayer> getAllRenderLayers(IMap map) {
    ArrayList<ILayer> layers = new ArrayList<>();
    for (ITileLayer tileLayer : map.getTileLayers()) {
      layers.add(tileLayer);
    }

    for (IImageLayer imageLayer : map.getImageLayers()) {
      layers.add(imageLayer);
    }
    layers.sort(Comparator.comparing(ILayer::getOrder));

    return layers;
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
    final Graphics2D imageGraphics = bufferedImage.createGraphics();

    // set alpha value of the tiles by the layers value
    final AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
    imageGraphics.setComposite(ac);

    layer.getTiles().parallelStream().forEach((tile) -> {
      // get the tile from the tileset image
      final int index = layer.getTiles().indexOf(tile);
      if (tile.getGridId() == 0) {
        return;
      }

      final Image tileTexture = getTileImage(map, tile);

      // draw the tile on the layer image
      final int x = index % layer.getSizeInTiles().width * map.getTileSize().width;
      final int y = index / layer.getSizeInTiles().width * map.getTileSize().height;
      RenderEngine.renderImage(imageGraphics, tileTexture, x, y);
    });

    ImageCache.MAPS.put(getCacheKey(map) + "_" + layer.getName(), bufferedImage);
    return bufferedImage;
  }

  /**
   * Renders the tiles from the specified layer that lie within the bounds of
   * the viewport. This layer rendering is not cached and renders all tiles
   * directly onto the graphics object.
   *
   * @param g
   * @param layer
   * @param map
   * @param viewport
   */
  private void renderTileLayerImage(final Graphics2D g, final ITileLayer layer, final IMap map, final Rectangle2D viewport) {
    final Point startTile = MapUtilities.getTileLocation(map, new Point2D.Double(viewport.getX(), viewport.getY()));
    final Point endTile = MapUtilities.getTileLocation(map, new Point2D.Double(viewport.getMaxX(), viewport.getMaxY()));
    final double viewportOffsetX = -(viewport.getX() - startTile.x * map.getTileSize().width) + layer.getPosition().x;
    final double viewportOffsetY = -(viewport.getY() - startTile.y * map.getTileSize().height) + layer.getPosition().y;

    // set alpha value of the tiles by the layers value
    final Composite oldComp = g.getComposite();
    final AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
    g.setComposite(ac);

    final int startX = MathUtilities.clamp(startTile.x, 0, layer.getSizeInTiles().width);
    final int endX = MathUtilities.clamp(endTile.x, 0, layer.getSizeInTiles().width);
    final int startY = MathUtilities.clamp(startTile.y, 0, layer.getSizeInTiles().height);
    final int endY = MathUtilities.clamp(endTile.y, 0, layer.getSizeInTiles().height);

    final double offsetX = viewportOffsetX + (startX - startTile.x) * map.getTileSize().width;
    final double offsetY = viewportOffsetY + (startY - startTile.y) * map.getTileSize().height;

    IntStream.range(startX, endX + 1).parallel().forEach(x -> {
      for (int y = startY; y <= endY; y++) {
        final Image tileTexture = getTileImage(map, layer.getTile(x, y));
        RenderEngine.renderImage(g, tileTexture, offsetX + (x - startX) * map.getTileSize().width, offsetY + (y - startY) * map.getTileSize().height);
      }
    });

    g.setComposite(oldComp);
  }

  // TODO: load images of all image layers to the gamefile in order to be able
  // to render the image
  private void renderImageLayer(Graphics2D g, IImageLayer layer, IMap map, Rectangle2D viewport) {
    final Composite oldComp = g.getComposite();
    final AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
    g.setComposite(ac);

    final double viewportOffsetX = -viewport.getX() + layer.getPosition().x;
    final double viewportOffsetY = -viewport.getY() + layer.getPosition().y;

    Spritesheet sprite = Spritesheet.find(layer.getImage().getSource());
    if (sprite == null) {
      return;
    }

    RenderEngine.renderImage(g, sprite.getImage(), viewportOffsetX, viewportOffsetY);
    g.setComposite(oldComp);
  }
}