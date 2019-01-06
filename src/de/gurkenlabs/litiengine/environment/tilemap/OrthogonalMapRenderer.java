package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Optional;

import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ImageProcessing;

public class OrthogonalMapRenderer extends MapRenderer {

  @Override
  public MapOrientation getSupportedOrientation() {
    return MapOrientation.ORTHOGONAL;
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
  @Override
  protected synchronized BufferedImage getLayerImage(final ITileLayer layer, final IMap map, boolean includeAnimationTiles) {
    // if we have already retrieved the image, use the one from the cache to
    // draw the layer
    final String cacheKey = getCacheKey(map) + "_" + layer.getName();
    Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      return opt.get();
    }

    final BufferedImage bufferedImage = ImageProcessing.getCompatibleImage(layer.getSizeInTiles().width * map.getTileSize().width, layer.getSizeInTiles().height * map.getTileSize().height);

    // we need a graphics 2D object to work with transparency
    final Graphics2D imageGraphics = bufferedImage.createGraphics();

    // set alpha value of the tiles by the layers value
    final AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
    imageGraphics.setComposite(ac);

    for (int x = 0; x < layer.getSizeInTiles().width; x++) {
      for (int y = 0; y < layer.getSizeInTiles().height; y++) {
        ITile tile = layer.getTile(x, y);
        if (tile == null || (!includeAnimationTiles && MapUtilities.hasAnimation(map, tile))) {
          continue;
        }

        final Image tileTexture = getTileImage(map, tile);
        ImageRenderer.render(imageGraphics, tileTexture, x * map.getTileWidth(), y * map.getTileHeight());
      }
    }

    Resources.images().add(cacheKey, bufferedImage);
    return bufferedImage;
  }

  /**
   * Renders the tiles from the specified layer that lie within the bounds of
   * the viewport. This rendering of static tiles is cached when when the
   * related graphics setting is enabled, which tremendously improves the
   * rendering performance.
   *
   * @param g
   *          The graphics object to render on.
   * @param layer
   *          That layer that will be rendered.
   * @param map
   *          The map that contains the layer.
   * @param viewport
   *          The area of the map that will be rendered.
   */
  @Override
  protected void renderTileLayerImage(final Graphics2D g, final ITileLayer layer, final IMap map, final Rectangle2D viewport) {
    // set alpha value of the tiles by the layers value
    final Composite oldComp = g.getComposite();
    final AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
    g.setComposite(ac);

    for (int x = 0; x < map.getWidth(); x++) {
      for (int y = 0; y < map.getHeight(); y++) {
        ITile tile = layer.getTile(x, y);
        int tileX = x * map.getTileWidth();
        int tileY = y * map.getTileHeight();

        // TODO: incorporate layer offsets, right now this may cut off layers with an offset
        Rectangle tileBounds = new Rectangle(tileX, tileY, map.getTileWidth(), map.getTileHeight());
        if (tile == null || !viewport.intersects(tileBounds)) {
          continue;
        }
        final Image tileTexture = getTileImage(map, tile);
        final double offsetX = -(viewport.getX()) + layer.getOffset().x;
        final double offsetY = -(viewport.getY()) + layer.getOffset().y;

        ImageRenderer.render(g, tileTexture, offsetX + tileX, offsetY + tileY);
      }
    }

    g.setComposite(oldComp);
  }
}