package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.ITileOffset;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

public abstract class ColorLayer implements IRenderable {
  private final Environment environment;
  private final Image[][] tiles;

  private Color color;

  protected ColorLayer(Environment env, final Color color) {
    this.environment = env;
    this.color = color;
    this.tiles = new Image[env.getMap().getWidth()][env.getMap().getHeight()];
    this.updateSection(this.environment.getMap().getBounds());
  }

  @Override
  public void render(Graphics2D g) {
    final Rectangle2D viewport = Game.world().camera().getViewport();

    final IMap map = this.getEnvironment().getMap();

    // draw the tile on the layer image
    for (int x = 0; x < map.getWidth(); x++) {
      for (int y = 0; y < map.getHeight(); y++) {
        Rectangle2D tileBounds = map.getOrientation().getShape(x, y, map).getBounds2D();
        ITile tile = map.getTileLayers().get(0).getTile(x, y);
        if (!viewport.intersects(tileBounds)) {
          continue;
        }
        final double offsetX = -(viewport.getX());
        final double offsetY = -(viewport.getY());

        int tileOffsetX = 0;
        int tileOffsetY = 0;
        ITileset tileset = MapUtilities.findTileSet(map, tile);
        if (tileset != null) {
          final ITileOffset tileOffset = tileset.getTileOffset();
          if (tileOffset != null) {
            tileOffsetX = tileOffset.getX();
            tileOffsetY = tileOffset.getY();
          }
        }
        ImageRenderer.render(g, tiles[x][y], offsetX + tileBounds.getX() + tileOffsetX, offsetY + tileBounds.getY() + tileOffsetY);
      }
    }
  }

  public Color getColor() {
    return this.color;
  }

  public void setAlpha(int ambientAlpha) {
    this.setColor(new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), MathUtilities.clamp(ambientAlpha, 0, 255)));
    this.updateSection(this.environment.getMap().getBounds());
  }

  public void setColor(final Color color) {
    this.color = color;
    this.updateSection(this.environment.getMap().getBounds());
  }

  public void updateSection(Rectangle2D section) {
    if (this.getColor() == null) {
      return;
    }

    final IMap map = this.getEnvironment().getMap();

    final Rectangle2D tileSection = MapUtilities.getTileBoundingBox(map, section);
    if (tileSection == null || (tileSection.getWidth() == 0 && tileSection.getHeight() == 0)) {
      return;
    }
    final BufferedImage img = Imaging.getCompatibleImage((int) tileSection.getWidth(), (int) tileSection.getHeight());
    final Graphics2D g = img.createGraphics();

    this.renderSection(g, tileSection);

    g.dispose();

    this.setTiles(img, tileSection);
  }

  private void setTiles(BufferedImage img, Rectangle2D section) {
    if (img == null) {
      return;
    }
    final IMap map = this.getEnvironment().getMap();
    final Point startTile = map.getOrientation().getTile(section.getX(), section.getY(), map);
    final Point endTile = map.getOrientation().getTile(section.getMaxX(), section.getMaxY(), map);
    final int startX = MathUtilities.clamp(startTile.x, 0, Math.min(startTile.x + (endTile.x - startTile.x), tiles.length) - 1);
    final int startY = MathUtilities.clamp(startTile.y, 0, Math.min(startTile.y + (endTile.y - startTile.y), tiles[0].length) - 1);
    final int endX = MathUtilities.clamp(endTile.x, 0, Math.min(startTile.x + (endTile.x - startTile.x), tiles.length) - 1);
    final int endY = MathUtilities.clamp(endTile.y, 0, Math.min(startTile.y + (endTile.y - startTile.y), tiles[0].length) - 1);

    final Shape startTileShape = map.getOrientation().getShape(startX, startY, map);
    for (int x = startX; x <= endX; x++) {
      for (int y = startY; y <= endY; y++) {
        Shape tile = map.getOrientation().getShape(x, y, map);
        Shape translatedTile = GeometricUtilities.translateShape(tile, new Point2D.Double(0, 0));
        int subX = MathUtilities.clamp((int) (tile.getBounds().getX() - startTileShape.getBounds().getX()), 0, img.getWidth() - map.getTileWidth());
        int subY = MathUtilities.clamp((int) (tile.getBounds().getY() - startTileShape.getBounds().getY()), 0, img.getHeight() - map.getTileHeight());
        final BufferedImage smallImage = img.getSubimage(subX, subY, map.getTileWidth(), map.getTileHeight());
        final BufferedImage clippedImage = Imaging.getCompatibleImage(smallImage.getWidth(), smallImage.getHeight());
        Graphics2D g = clippedImage.createGraphics();
        g.clip(translatedTile);
        g.drawImage(smallImage, 0, 0, null);
        g.dispose();
        this.tiles[x][y] = clippedImage;
      }
    }
  }

  protected abstract void renderSection(Graphics2D g, Rectangle2D section);

  protected Environment getEnvironment() {
    return this.environment;
  }
}
