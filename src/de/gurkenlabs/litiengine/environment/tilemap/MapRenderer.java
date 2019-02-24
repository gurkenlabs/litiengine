package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;

public abstract class MapRenderer implements IMapRenderer {

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
        renderImageLayer(g, (IImageLayer) layer, viewport);
      }
    }
  }

  @Override
  public BufferedImage getImage(IMap map, RenderType... renderTypes) {
    final String cacheKey = getCacheKey(map) + "_" + Arrays.toString(renderTypes);
    Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      return opt.get();
    }

    final BufferedImage img = Imaging.getCompatibleImage((int) map.getSizeInPixels().getWidth(), (int) map.getSizeInPixels().getHeight());
    final Graphics2D g = img.createGraphics();

    for (final ITileLayer layer : map.getTileLayers()) {
      if (layer == null || !shouldBeRendered(layer, renderTypes)) {
        continue;
      }

      ImageRenderer.render(g, this.getLayerImage(layer, map, true), layer.getOffset());
    }

    g.dispose();

    Resources.images().add(cacheKey, img);
    return img;
  }

  @Override
  public BufferedImage getImage(IMap map) {
    return this.getImage(map, RenderType.BACKGROUND, RenderType.GROUND, RenderType.SURFACE, RenderType.NORMAL, RenderType.OVERLAY);
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

  protected abstract BufferedImage getLayerImage(final ITileLayer layer, final IMap map, boolean includeAnimationTiles);

  protected abstract void renderTileLayerImage(final Graphics2D g, final ITileLayer layer, final IMap map, final Rectangle2D viewport);

  protected static boolean shouldBeRendered(ILayer layer, RenderType[] renderTypes) {
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

  protected static boolean isVisible(ILayer layer) {
    return layer.isVisible() && layer.getOpacity() > 0;
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
      final long playedMs = Game.time().sinceGameStart();

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
}