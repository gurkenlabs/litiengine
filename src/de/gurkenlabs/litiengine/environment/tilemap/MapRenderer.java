package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.GameTime;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;

public class MapRenderer {

  public static void render(final Graphics2D g, final IMap map, final Rectangle2D viewport, RenderType... renderTypes) {
    renderLayers(g, map, map, viewport, renderTypes);
  }

  private static void renderLayers(final Graphics2D g, final IMap map, ILayerList layers, final Rectangle2D viewport, RenderType[] renderTypes) {
    for (final ILayer layer : layers.getRenderLayers()) {
      if (layer == null || !shouldBeRendered(layer, renderTypes)) {
        continue;
      }

      if (layer instanceof ITileLayer) {
        renderTileLayerImage(g, (ITileLayer) layer, map, viewport);
      }

      if (layer instanceof IImageLayer) {
        renderImageLayer(g, (IImageLayer) layer, viewport);
      }

      if (layer instanceof IGroupLayer) {
        renderLayers(g, map, (IGroupLayer)layer, viewport, renderTypes);
      }
    }
  }

  private static void renderTileLayerImage(final Graphics2D g, final ITileLayer layer, final IMap map, final Rectangle2D viewport) {
    // TODO: possibly implement the same render order that Tiled uses for staggered maps: undo the staggering, and then render it right-down
    if (map.getRenderOrder().btt) {
      for (int y = map.getHeight() - 1; y >= 0; y--) {
        drawRow(g, layer, y, map, viewport);
      }
    } else {
      for (int y = 0; y < map.getHeight(); y++) {
        drawRow(g, layer, y, map, viewport);
      }
    }
  }

  private static void drawRow(Graphics2D g, ITileLayer layer, int y, IMap map, Rectangle2D viewport) {
    if (map.getRenderOrder().rtl) {
      for (int x = map.getWidth() - 1; x >= 0; x--) {
        drawTile(g, layer, x, y, map, viewport);
      }
    } else {
      for (int x = 0; x < map.getWidth(); x++) {
        drawTile(g, layer, x, y, map, viewport);
      }
    }
  }

  private static void drawTile(Graphics2D g, ITileLayer layer, int x, int y, IMap map, Rectangle2D viewport) {
    ITile tile = layer.getTile(x, y);
    Image image = getTileImage(map, tile);
    if (image != null) {
      Point p = map.getOrientation().getLocation(x, y, map);
      p.y -= image.getHeight(null);
      if (viewport.intersects(p.x, p.y, image.getWidth(null), image.getHeight(null))) {
        ImageRenderer.render(g, image, p.x - viewport.getX(), p.y - viewport.getY());
      }
    }
  }

  protected static boolean shouldBeRendered(ILayer layer, RenderType[] renderTypes) {
    if (renderTypes == null || renderTypes.length == 0 || layer instanceof IGroupLayer) {
      return isVisible(layer);
    }

    for (RenderType alloc : renderTypes) {
      if (alloc == layer.getRenderType()) {
        return isVisible(layer);
      }
    }

    return false;
  }

  protected static boolean isVisible(ILayer layer) {
    return layer.isVisible() && layer.getOpacity() > 0f;
  }

  protected static void renderImageLayer(Graphics2D g, IImageLayer layer, Rectangle2D viewport) {
    Spritesheet sprite = Resources.spritesheets().get(layer.getImage().getSource());
    if (sprite == null) {
      return;
    }

    final Composite oldComp = g.getComposite();
    final AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
    g.setComposite(ac);

    final double viewportOffsetX = -viewport.getX() + layer.getOffset().x;
    final double viewportOffsetY = -viewport.getY() + layer.getOffset().y;

    ImageRenderer.render(g, sprite.getImage(), viewportOffsetX, viewportOffsetY);
    g.setComposite(oldComp);
  }

  /**
   * Gets the cache key.
   *
   * @param map
   *          the map
   * @return the cache key
   */
  protected static String getCacheKey(final IMap map) {
    return "map_" + map.getName();
  }

  protected static Image getTileImage(final IMap map, final ITile tile) {
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
      final long playedMs = GameTime.sinceGameStart();

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

    BufferedImage tileImage = sprite.getSprite(index, tileset.getMargin(), tileset.getSpacing());
    if (tile.isFlipped()) {
      if (tile.isFlippedDiagonally()) {
        tileImage = Imaging.rotate(tileImage, -Math.PI / 2);
        tileImage = Imaging.verticalFlip(tileImage);
      }

      if (tile.isFlippedHorizontally()) {
        tileImage = Imaging.horizontalFlip(tileImage);
      }

      if (tile.isFlippedVertically()) {
        tileImage = Imaging.verticalFlip(tileImage);
      }
    }

    return tileImage;
  }

  private MapRenderer() {
    throw new UnsupportedOperationException();
  }
}
