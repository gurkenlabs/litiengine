package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.EventListener;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;

public class MapRenderer {
  private static Collection<LayerRenderedListener> layerRenderedListeners = ConcurrentHashMap.newKeySet();
  private static Collection<LayerRenderCondition> layerRenderConditions = ConcurrentHashMap.newKeySet();

  private MapRenderer() {
    throw new UnsupportedOperationException();
  }

  public static void onLayerRendered(LayerRenderedListener listener) {
    layerRenderedListeners.add(listener);
  }

  public static void removeLayerRenderedListener(LayerRenderedListener listener) {
    layerRenderedListeners.remove(listener);
  }

  public static void addLayerRenderCondition(LayerRenderCondition condition) {
    layerRenderConditions.add(condition);
  }

  public static void removeLayerRenderCondition(LayerRenderCondition condition) {
    layerRenderConditions.remove(condition);
  }

  public static void render(Graphics2D g, IMap map, Rectangle2D viewport, RenderType... renderTypes) {
    renderLayers(g, map, map, viewport, null, renderTypes, 1f);
  }

  public static void render(final Graphics2D g, final IMap map, final Rectangle2D viewport, Environment env, RenderType... renderTypes) {
    renderLayers(g, map, map, viewport, env, renderTypes, 1f);
  }

  private static void renderLayers(final Graphics2D g, final IMap map, ILayerList layers, final Rectangle2D viewport, Environment env, RenderType[] renderTypes, float opacity) {
    for (final ILayer layer : layers.getRenderLayers()) {
      if (layer == null || !shouldBeRendered(g, map, layer, renderTypes)) {
        continue;
      }

      float layerOpacity = layer.getOpacity() * opacity;

      if (layer instanceof ITileLayer) {
        renderTileLayer(g, (ITileLayer) layer, map, viewport, layerOpacity);
      }

      if (env != null && layer instanceof IMapObjectLayer) {
        env.renderEntitiesOnLayer(g, (IMapObjectLayer) layer);
      }

      if (layer instanceof IImageLayer) {
        renderImageLayer(g, (IImageLayer) layer, map, viewport, layerOpacity);
      }

      if (layer instanceof IGroupLayer) {
        renderLayers(g, map, (IGroupLayer) layer, viewport, env, renderTypes, layerOpacity);
      }
    }
  }

  private static void renderTileLayer(final Graphics2D g, final ITileLayer layer, final IMap map, final Rectangle2D viewport, float opacity) {
    // TODO: possibly implement the same render order that Tiled uses for staggered maps: undo the staggering, and then render it right-down
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
    if (map.getRenderOrder().btt) {
      for (int y = map.getHeight() - 1; y >= 0; y--) {
        drawRow(g, layer, y, map, viewport);
      }
    } else {
      for (int y = 0; y < map.getHeight(); y++) {
        drawRow(g, layer, y, map, viewport);
      }
    }

    final LayerRenderEvent event = new LayerRenderEvent(g, map, layer);
    for (LayerRenderedListener listener : layerRenderedListeners) {
      listener.rendered(event);
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
    Spritesheet sprite = Resources.spritesheets().get(layer.getImage().getSource());
    if (sprite == null) {
      return;
    }

    final Composite oldComp = g.getComposite();
    final AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
    g.setComposite(ac);

    final double viewportOffsetX = layer.getOffset().x - viewport.getX();
    final double viewportOffsetY = layer.getOffset().y - viewport.getY();

    ImageRenderer.render(g, sprite.getImage(), viewportOffsetX, viewportOffsetY);
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
     * @param event
     *          The layer render event.
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
     * @param event
     *          The layer render event.
     */
    boolean canRender(LayerRenderEvent event);
  }
}
