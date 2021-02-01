package de.gurkenlabs.utiliti.renderers;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.utiliti.components.Editor;

public class GridRenderer implements IEditorRenderer {
  private final Map<String, GridImages> gridCache = new ConcurrentHashMap<>();

  @Override
  public String getName() {
    return "GRID";
  }

  @Override
  public void render(Graphics2D g) {
    // render the grid
    final ICamera camera = Game.world().camera();
    if (Editor.preferences().showGrid() && camera.getRenderScale() >= 1 && Game.world().environment() != null) {
      final IMap map = Game.world().environment().getMap();

      if (map == null || map.getName() == null) {
        return;
      }

      if (!gridCache.containsKey(map.getName())) {
        gridCache.put(map.getName(), new GridImages(map));
      }

      Point2D viewPortLocation = Game.world().camera().getViewportLocation(0, 0);

      final float scale = camera.getRenderScale() > GridImages.RENDERSCALE_LARGE ?
          GridImages.LARGE_GRID_SCALE :
          camera.getRenderScale() > GridImages.RENDERSCALE_MID ? GridImages.MID_GRID_SCALE : GridImages.SMALL_GRID_SCALE;
      final GridImages images = gridCache.get(map.getName());
      final BufferedImage image = camera.getRenderScale() > GridImages.RENDERSCALE_LARGE ?
          images.getLargeImage() :
          camera.getRenderScale() > GridImages.RENDERSCALE_MID ? images.getMidImage() : images.getSmallImage();
      ImageRenderer
          .renderScaled(g, image, viewPortLocation.getX() * camera.getRenderScale(), viewPortLocation.getY() * Game.world().camera().getRenderScale(),
              camera.getRenderScale() / scale);
    }
  }

  public void clearCache() {
    gridCache.clear();
  }

  private static class GridImages {
    private static final float RENDERSCALE_LARGE = 6;
    private static final float RENDERSCALE_MID = 1.5f;

    private static final float SMALL_GRID_SCALE = 1f;
    private static final float MID_GRID_SCALE = 2;
    private static final float LARGE_GRID_SCALE = 6;
    private final BufferedImage smallImage;
    private final BufferedImage midImage;
    private final BufferedImage largeImage;

    private GridImages(IMap map) {
      this.smallImage = createImage(map, SMALL_GRID_SCALE);
      this.midImage = createImage(map, MID_GRID_SCALE);
      this.largeImage = createImage(map, LARGE_GRID_SCALE);
    }

    public BufferedImage getSmallImage() {
      return smallImage;
    }

    public BufferedImage getMidImage() {
      return midImage;
    }

    public BufferedImage getLargeImage() {
      return largeImage;
    }

    private static BufferedImage createImage(IMap map, float scale) {
      BufferedImage image = Imaging
          .getCompatibleImage((int) (map.getSizeInPixels().width * scale) + 1, (int) (map.getSizeInPixels().height * scale) + 1);
      Graphics2D graphics = (Graphics2D) image.getGraphics();

      final float lineSize = Editor.preferences().getGridLineWidth() / scale;
      final Stroke stroke = new BasicStroke(lineSize);
      graphics.setColor(Editor.preferences().getGridColor());
      for (int x = 0; x < map.getWidth(); x++) {
        for (int y = 0; y < map.getHeight(); y++) {
          Shape tile = map.getOrientation().getShape(x, y, map);
          ShapeRenderer.renderOutlineTransformed(graphics, tile, AffineTransform.getScaleInstance(scale, scale), stroke);
        }
      }

      graphics.dispose();

      return image;
    }
  }
}
