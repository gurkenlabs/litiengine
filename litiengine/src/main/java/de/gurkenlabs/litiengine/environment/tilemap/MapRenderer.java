package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.ResourceLoadException;
import de.gurkenlabs.litiengine.resources.Resources;
import java.util.logging.Logger;

public class MapRenderer {
  private static final Collection<LayerRenderedListener> layerRenderedListeners = ConcurrentHashMap.newKeySet();
  private static final Collection<LayerRenderCondition> layerRenderConditions = ConcurrentHashMap.newKeySet();
  private static final Collection<String> unavailableImageSources = ConcurrentHashMap.newKeySet();
  private static final Logger log = Logger.getLogger(MapRenderer.class.getName());

  static {
    Resources.images().addClearedListener(unavailableImageSources::clear);
  }

  private MapRenderer() {
    throw new UnsupportedOperationException();
  }

  /**
   * Adds the specified layer rendered listener to receive events when a layer has been rendered.
   *
   * @param listener The listener to add.
   */
  public static void onLayerRendered(LayerRenderedListener listener) {
    layerRenderedListeners.add(listener);
  }

  /**
   * Removes the specified layer rendered listener..
   *
   * @param listener The listener to remove.
   */
  public static void removeLayerRenderedListener(LayerRenderedListener listener) {
    layerRenderedListeners.remove(listener);
  }

  /**
   * Adds the specified layer render condition to control whether layers should be rendered.
   *
   * @param condition The condition to add.
   */
  public static void addLayerRenderCondition(LayerRenderCondition condition) {
    layerRenderConditions.add(condition);
  }

  /**
   * Removes the specified layer render condition.
   *
   * @param condition The condition to remove.
   */
  public static void removeLayerRenderCondition(LayerRenderCondition condition) {
    layerRenderConditions.remove(condition);
  }

  public static void render(Graphics2D g, IMap map, Rectangle2D viewport, RenderType... renderTypes) {
    renderLayers(g, map, map, viewport, null, renderTypes, 1f);
  }

  public static void render(final Graphics2D g, final IMap map, final Rectangle2D viewport, Environment env, RenderType... renderTypes) {
    renderLayers(g, map, map, viewport, env, renderTypes, 1f);
  }

  private static void renderLayers(final Graphics2D g, final IMap map, ILayerList layers, final Rectangle2D viewport, Environment env,
    RenderType[] renderTypes, float opacity) {
    final List<ILayer> renderLayers = layers.getRenderLayers();
    for (final ILayer layer : renderLayers) {
      if (layer == null || !shouldBeRendered(g, map, layer, renderTypes)) {
        continue;
      }

      float layerOpacity = layer.getOpacity() * opacity;

      if (layer instanceof ITileLayer itl) {
        renderTileLayer(g, itl, map, viewport, layerOpacity);
      }

      if (env != null && layer instanceof IMapObjectLayer imol) {
        Collection<IEntity> entities = env.getEntities(imol);
        if (entities != null) {
          Game.graphics().renderEntities(g, entities, layer.getRenderType() == RenderType.NORMAL);
        }
      }

      if (layer instanceof IImageLayer iil) {
        renderImageLayer(g, iil, map, viewport, layerOpacity);
      }

      if (layer instanceof IGroupLayer igl) {
        renderLayers(g, map, igl, viewport, env, renderTypes, layerOpacity);
      }
    }
  }

  private static void renderTileLayer(final Graphics2D g, final ITileLayer layer, final IMap map, final Rectangle2D viewport, float opacity) {
    // TODO: possibly implement the same render order that Tiled uses for staggered maps: undo the staggering, and then render it right-down
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

    int[] range = getVisibleTileRange(viewport, map);

    if (map.getRenderOrder().btt) {
      for (int y = range[3]; y >= range[1]; y--) {
        drawRow(g, layer, y, range[0], range[2], map, viewport);
      }
    } else {
      for (int y = range[1]; y <= range[3]; y++) {
        drawRow(g, layer, y, range[0], range[2], map, viewport);
      }
    }

    final LayerRenderEvent event = new LayerRenderEvent(g, map, layer);
    for (LayerRenderedListener listener : layerRenderedListeners) {
      listener.rendered(event);
    }
  }

  private static void drawRow(Graphics2D g, ITileLayer layer, int y, int minX, int maxX, IMap map, Rectangle2D viewport) {
    if (map.getRenderOrder().rtl) {
      for (int x = maxX; x >= minX; x--) {
        drawTile(g, layer, x, y, map, viewport);
      }
    } else {
      for (int x = minX; x <= maxX; x++) {
        drawTile(g, layer, x, y, map, viewport);
      }
    }
  }

  private static int[] getVisibleTileRange(Rectangle2D viewport, IMap map) {
    int minX = 0, minY = 0;
    int maxX = map.getWidth() - 1;
    int maxY = map.getHeight() - 1;

    if (viewport == null || viewport.isEmpty() || map.getWidth() <= 0 || map.getHeight() <= 0) {
      return new int[] { minX, minY, maxX, maxY };
    }

    if ("orthogonal".equals(map.getOrientation().getName())) {
      Rectangle2D mapBounds = new Rectangle2D.Double(
        0, 0, (double) map.getWidth() * map.getTileWidth(), (double) map.getHeight() * map.getTileHeight());
      Rectangle2D visible = viewport.createIntersection(mapBounds);
      if (!visible.isEmpty()) {
        minX = (int) Math.floor(visible.getMinX() / map.getTileWidth());
        minY = (int) Math.floor(visible.getMinY() / map.getTileHeight());
        maxX = (int) Math.floor(Math.nextDown(visible.getMaxX()) / map.getTileWidth());
        maxY = (int) Math.floor(Math.nextDown(visible.getMaxY()) / map.getTileHeight());
      }
    } else {
      Rectangle2D visible = viewport.createIntersection(
        new Rectangle2D.Double(0, 0, map.getWidth() * map.getTileWidth(), map.getHeight() * map.getTileHeight()));
      if (!visible.isEmpty()) {
        double sampleXs[] = { visible.getMinX(), (visible.getMinX() + visible.getMaxX()) / 2.0, Math.nextDown(visible.getMaxX()) };
        double sampleYs[] = { visible.getMinY(), (visible.getMinY() + visible.getMaxY()) / 2.0, Math.nextDown(visible.getMaxY()) };
        minX = Integer.MAX_VALUE;
        minY = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        maxY = Integer.MIN_VALUE;
        for (double sx : sampleXs) {
          for (double sy : sampleYs) {
            Point t = map.getOrientation().getTile(sx, sy, map);
            minX = Math.min(minX, t.x);
            minY = Math.min(minY, t.y);
            maxX = Math.max(maxX, t.x);
            maxY = Math.max(maxY, t.y);
          }
        }
      }
    }

    // expand by 1 tile for offsets/overlapping tiles, then clamp to map bounds
    return new int[] {
      Math.clamp(minX - 1, 0, map.getWidth() - 1),
      Math.clamp(minY - 1, 0, map.getHeight() - 1),
      Math.clamp(maxX + 1, 0, map.getWidth() - 1),
      Math.clamp(maxY + 1, 0, map.getHeight() - 1)
    };
  }

  private static void drawTile(Graphics2D g, ITileLayer layer, int x, int y, IMap map, Rectangle2D viewport) {
    ITile tile = layer.getTile(x, y);
    if (tile == null) {
      return;
    }
    BufferedImage image = tile.getImage();
    if (image != null) {
      Point p = map.getOrientation().getLocation(x, y, map);
      p.y -= image.getHeight();
      ITileOffset offset = tile.getTilesetEntry().getTileset().getTileOffset();
      if (offset != null) {
        p.x += offset.getX();
        p.y += offset.getY();
      }
      if (viewport.intersects(p.x, p.y, image.getWidth(), image.getHeight())) {
        ImageRenderer.render(g, image, p.x - viewport.getX(), p.y - viewport.getY());
      }
    }
  }

  protected static boolean shouldBeRendered(final Graphics2D g, final IMap map, ILayer layer, RenderType[] renderTypes) {
    final LayerRenderEvent event = new LayerRenderEvent(g, map, layer);
    for (LayerRenderCondition condition : layerRenderConditions) {
      if (!condition.canRender(event)) {
        return false;
      }
    }

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

  protected static void renderImageLayer(Graphics2D g, IImageLayer layer, final IMap map, Rectangle2D viewport, float opacity) {
    if (layer.getImage() == null || layer.getImage().getSource() == null) {
      return;
    }
    Spritesheet sprite = Resources.spritesheets().get(layer.getImage().getSource());
    BufferedImage img;
    if (sprite == null) {
      String source = layer.getImage().getAbsoluteSourcePath() != null
        ? layer.getImage().getAbsoluteSourcePath().toString()
        : layer.getImage().getSource();
      if (unavailableImageSources.contains(source)) {
        return;
      }
      try {
        img = layer.getImage().getAbsoluteSourcePath() != null
          ? Resources.images().get(layer.getImage().getAbsoluteSourcePath())
          : Resources.images().get(layer.getImage().getSource());
      } catch (ResourceLoadException e) {
        if (unavailableImageSources.add(source)) {
          log.warning("Skipping unavailable image layer source: " + source);
        }
        return;
      }
    } else {
      img = sprite.getImage();
    }
    if (img == null) {
      return;
    }

    final Composite oldComp = g.getComposite();
    final AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
    g.setComposite(ac);

    final double viewportOffsetX = layer.getOffset().getX() - viewport.getX();
    final double viewportOffsetY = layer.getOffset().getY() - viewport.getY();

    ImageRenderer.render(g, img, viewportOffsetX, viewportOffsetY);
    g.setComposite(oldComp);

    final LayerRenderEvent event = new LayerRenderEvent(g, map, layer);
    for (LayerRenderedListener listener : layerRenderedListeners) {
      listener.rendered(event);
    }
  }

  /**
   * This listener interface receives events when a layer was rendered.
   *
   * @see MapRenderer#onLayerRendered(LayerRenderedListener)
   */
  @FunctionalInterface
  public interface LayerRenderedListener extends EventListener {
    /**
     * Invoked when a layer has been rendered.
     *
     * @param event The layer render event.
     */
    void rendered(LayerRenderEvent event);
  }

  /**
   * This listener interface provides a condition callback to contol whether a layer should be rendered.
   *
   * @see MapRenderer#addLayerRenderCondition(LayerRenderCondition)
   */
  @FunctionalInterface
  public interface LayerRenderCondition extends EventListener {
    /**
     * Invoked before the rendering of a layer to determine if it should be rendered.
     *
     * @param event The layer render event.
     * @return Return true if the layer should be rendered; otherwise false.
     */
    boolean canRender(LayerRenderEvent event);
  }
}
