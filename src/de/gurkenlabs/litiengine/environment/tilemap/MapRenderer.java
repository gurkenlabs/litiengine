package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.resources.Resources;

public class MapRenderer {

  public static void render(Graphics2D g, IMap map, Rectangle2D viewport, RenderType... renderTypes) {
    renderLayers(g, map, map, viewport, null, renderTypes, 1f);
  }

  public static void render(final Graphics2D g, final IMap map, final Rectangle2D viewport, Environment env, RenderType... renderTypes) {
    renderLayers(g, map, map, viewport, env, renderTypes, 1f);
  }

  private static void renderLayers(final Graphics2D g, final IMap map, ILayerList layers, final Rectangle2D viewport, Environment env, RenderType[] renderTypes, float opacity) {
    for (final ILayer layer : layers.getRenderLayers()) {
      if (layer == null || !shouldBeRendered(layer, renderTypes)) {
        continue;
      }

      float layerOpacity = layer.getOpacity() * opacity;

      if (layer instanceof ITileLayer) {
        renderTileLayer(g, (ITileLayer) layer, map, viewport, layerOpacity);
      }

      if (env != null && layer instanceof IMapObjectLayer) {
        env.renderLayer(g, (IMapObjectLayer) layer);
      }

      if (layer instanceof IImageLayer) {
        renderImageLayer(g, (IImageLayer) layer, viewport, layerOpacity);
      }

      if (layer instanceof IGroupLayer) {
        renderLayers(g, map, (IGroupLayer)layer, viewport, env, renderTypes, layerOpacity);
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

  protected static void renderImageLayer(Graphics2D g, IImageLayer layer, Rectangle2D viewport, float opacity) {
    BufferedImage sprite = Resources.images().get(layer.getImage().getAbsoluteSourcePath());
    if (sprite == null) {
      return;
    }

    final Composite oldComp = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

    final double viewportOffsetX = layer.getOffset().x - viewport.getX();
    final double viewportOffsetY = layer.getOffset().y - viewport.getY();

    ImageRenderer.render(g, sprite, viewportOffsetX, viewportOffsetY);
    g.setComposite(oldComp);
  }

  private MapRenderer() {
    throw new UnsupportedOperationException();
  }
}
