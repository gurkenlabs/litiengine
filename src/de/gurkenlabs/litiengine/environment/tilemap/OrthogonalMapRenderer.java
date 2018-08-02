package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.ImageProcessing;
import de.gurkenlabs.litiengine.util.MathUtilities;

public class OrthogonalMapRenderer implements IMapRenderer {

  @Override
  public BufferedImage getImage(IMap map, RenderType... renderTypes) {
    final String cacheKey = getCacheKey(map) + "_" + renderTypes;
    if (ImageCache.MAPS.containsKey(cacheKey)) {
      return ImageCache.MAPS.get(cacheKey);
    }

    final BufferedImage img = ImageProcessing.getCompatibleImage((int) map.getSizeInPixels().getWidth(), (int) map.getSizeInPixels().getHeight());
    final Graphics2D g = img.createGraphics();

    for (final ITileLayer layer : map.getTileLayers()) {
      if (layer == null || !shouldBeRendered(layer, renderTypes)) {
        continue;
      }

      ImageRenderer.render(g, this.getLayerImage(layer, map, true), layer.getPosition());
    }

    g.dispose();

    ImageCache.MAPS.put(cacheKey, img);
    return img;
  }

  @Override
  public MapOrientation getSupportedOrientation() {
    return MapOrientation.ORTHOGONAL;
  }

  @Override
  public void render(final Graphics2D g, final IMap map, RenderType... renderTypes) {
    this.render(g, map, 0, 0, renderTypes);
  }

  @Override
  public void render(final Graphics2D g, final IMap map, final double offsetX, final double offsetY, RenderType... renderTypes) {
    final BufferedImage mapImage = this.getImage(map, renderTypes);
    ImageRenderer.render(g, mapImage, offsetX, offsetY);
  }

  @Override
  public void render(final Graphics2D g, final IMap map, final Rectangle2D viewport, RenderType... renderTypes) {
    for (final ILayer layer : map.getRenderLayers()) {
      if (layer == null || !shouldBeRendered(layer, renderTypes)) {
        continue;
      }

      if (layer instanceof ITileLayer) {
        this.renderTileLayerImage(g, (ITileLayer) layer, map, viewport);
      }

      if (layer instanceof IImageLayer) {
        this.renderImageLayer(g, (IImageLayer) layer, viewport);
      }
    }
  }

  private static boolean shouldBeRendered(ILayer layer, RenderType[] renderTypes) {
    if (renderTypes == null || renderTypes.length == 0) {
      return isVisible(layer);
    }

    for (RenderType alloc : renderTypes) {
      if (alloc == layer.getRenderType()) {
        return isVisible(layer);
      }
    }

    return false;
  }

  private static boolean isVisible(ILayer layer) {
    return layer.isVisible() && layer.getOpacity() > 0;
  }

  /**
   * Gets the cache key.
   *
   * @param map
   *          the map
   * @return the cache key
   */
  private static String getCacheKey(final IMap map) {
    return "map_" + map.getName();
  }

  private static Image getTileImage(final IMap map, final ITile tile) {
    if (tile == null) {
      return null;
    }

    final ITileset tileset = MapUtilities.findTileSet(map, tile);
    if (tileset == null || tileset.getFirstGridId() > tile.getGridId()) {
      return null;
    }

    Spritesheet sprite = tileset.getSpritesheet();
    if (sprite == null) {
      return null;
    }

    // get the grid id relative to the sprite sheet since we use a 0 based
    // approach to calculate the position
    int index = tile.getGridId() - tileset.getFirstGridId();

    // support for animated tiles
    final ITileAnimation animation = MapUtilities.getAnimation(map, index);
    if (animation != null && !animation.getFrames().isEmpty()) {
      final long playedMs = Game.getTime().sinceGameStart();

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

    BufferedImage tileImage = sprite.getSprite(index);
    if (tile.isFlipped()) {
      if (tile.isFlippedDiagonally()) {
        tileImage = ImageProcessing.rotate(tileImage, Math.toRadians(90));
        tileImage = ImageProcessing.verticalFlip(tileImage);
      }

      if (tile.isFlippedHorizontally()) {
        tileImage = ImageProcessing.horizontalFlip(tileImage);
      }

      if (tile.isFlippedVertically()) {
        tileImage = ImageProcessing.verticalFlip(tileImage);
      }
    }

    return tileImage;
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
  private synchronized BufferedImage getLayerImage(final ITileLayer layer, final IMap map, boolean includeAnimationTiles) {
    // if we have already retrived the image, use the one from the cache to
    // draw the layer
    final String cacheKey = getCacheKey(map) + "_" + layer.getName();
    if (ImageCache.MAPS.containsKey(cacheKey)) {
      return ImageCache.MAPS.get(cacheKey);
    }
    final BufferedImage bufferedImage = ImageProcessing.getCompatibleImage(layer.getSizeInTiles().width * map.getTileSize().width, layer.getSizeInTiles().height * map.getTileSize().height);

    // we need a graphics 2D object to work with transparency
    final Graphics2D imageGraphics = bufferedImage.createGraphics();

    // set alpha value of the tiles by the layers value
    final AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
    imageGraphics.setComposite(ac);

    layer.getTiles().parallelStream().forEach(tile -> {
      // get the tile from the tileset image
      final int index = layer.getTiles().indexOf(tile);
      if (tile.getGridId() == 0) {
        return;
      }

      if (!includeAnimationTiles && MapUtilities.hasAnimation(map, tile)) {
        return;
      }

      final Image tileTexture = getTileImage(map, tile);

      // draw the tile on the layer image
      final int x = index % layer.getSizeInTiles().width * map.getTileSize().width;
      final int y = index / layer.getSizeInTiles().width * map.getTileSize().height;
      ImageRenderer.render(imageGraphics, tileTexture, x, y);
    });

    ImageCache.MAPS.put(cacheKey, bufferedImage);
    return bufferedImage;
  }

  /**
   * Renders the tiles from the specified layer that lie within the bounds of
   * the viewport. This rendering of static tiles is cached when when the
   * related graphics setting is enabled, which tremendously improves the
   * rendering performance.
   *
   * @param g
   * @param layer
   * @param map
   * @param viewport
   */
  private void renderTileLayerImage(final Graphics2D g, final ITileLayer layer, final IMap map, final Rectangle2D viewport) {
    final Point startTile = MapUtilities.getTile(map, new Point2D.Double(viewport.getX(), viewport.getY()));
    final Point endTile = MapUtilities.getTile(map, new Point2D.Double(viewport.getMaxX(), viewport.getMaxY()));
    final double viewportOffsetX = -(viewport.getX() - startTile.x * map.getTileSize().width) + layer.getPosition().x;
    final double viewportOffsetY = -(viewport.getY() - startTile.y * map.getTileSize().height) + layer.getPosition().y;

    // set alpha value of the tiles by the layers value
    final Composite oldComp = g.getComposite();
    final AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
    g.setComposite(ac);

    final int startX = MathUtilities.clamp(startTile.x, 0, layer.getSizeInTiles().width);
    final int endX = MathUtilities.clamp(endTile.x, 0, layer.getSizeInTiles().width);
    final int startY = MathUtilities.clamp(startTile.y, 0, layer.getSizeInTiles().height);
    final int endY = MathUtilities.clamp(endTile.y, 0, layer.getSizeInTiles().height);

    final double offsetX = viewportOffsetX + (startX - startTile.x) * map.getTileSize().width;
    final double offsetY = viewportOffsetY + (startY - startTile.y) * map.getTileSize().height;

    for (int x = startX; x <= endX; x++) {
      for (int y = startY; y <= endY; y++) {
        ITile tile = layer.getTile(x, y);
        if (tile == null) {
          continue;
        }

        final Image tileTexture = getTileImage(map, tile);
        ImageRenderer.render(g, tileTexture, offsetX + (x - startX) * map.getTileSize().width, offsetY + (y - startY) * map.getTileSize().height);
      }
    }

    g.setComposite(oldComp);
  }

  private void renderImageLayer(Graphics2D g, IImageLayer layer, Rectangle2D viewport) {
    Spritesheet sprite = Spritesheet.find(layer.getImage().getSource());
    if (sprite == null) {
      return;
    }

    final Composite oldComp = g.getComposite();
    final AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
    g.setComposite(ac);

    final double viewportOffsetX = -viewport.getX() + layer.getPosition().x;
    final double viewportOffsetY = -viewport.getY() + layer.getPosition().y;

    ImageRenderer.render(g, sprite.getImage(), viewportOffsetX, viewportOffsetY);
    g.setComposite(oldComp);
  }
}