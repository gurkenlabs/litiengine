package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientation;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerAxis;
import de.gurkenlabs.litiengine.util.ImageProcessing;
import de.gurkenlabs.litiengine.util.MathUtilities;

public abstract class ColorLayer implements IRenderable {
  private final IEnvironment environment;
  private final Image[][] tiles;

  private int alpha;
  private Color color;

  protected ColorLayer(IEnvironment env, final Color color, final int alpha) {
    this.environment = env;
    this.color = color;
    this.alpha = alpha;
    this.tiles = new Image[env.getMap().getWidth()][env.getMap().getHeight()];
    this.updateSection(this.environment.getMap().getBounds());
  }

  @Override
  public void render(Graphics2D g) {
    final Rectangle2D viewport = Game.getCamera().getViewPort();

    final IMap map = this.getEnvironment().getMap();

    // draw the tile on the layer image
    for (int x = 0; x < map.getWidth(); x++) {
      for (int y = 0; y < map.getHeight(); y++) {
        Rectangle2D tileBounds = map.getTileGrid()[x][y].getBounds2D();
        if (!viewport.intersects(tileBounds)) {
          continue;
        }
        final double offsetX = -(viewport.getX());
        final double offsetY = -(viewport.getY());
        ImageRenderer.render(g, tiles[x][y], offsetX + tileBounds.getX(), offsetY + tileBounds.getY());
      }
    }
  }

  public int getAlpha() {
    return this.alpha;
  }

  public Color getColor() {
    return this.color;
  }

  public Color getColorWithAlpha() {
    return new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), this.getAlpha());
  }

  public void setAlpha(int ambientAlpha) {
    this.alpha = MathUtilities.clamp(ambientAlpha, 0, 255);
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
    if (tileSection == null) {
      return;
    }
    final BufferedImage img = ImageProcessing.getCompatibleImage((int) tileSection.getWidth(), (int) tileSection.getHeight());
    final Graphics2D g = img.createGraphics();

    this.renderSection(g, tileSection);

    g.dispose();

    this.setTiles(img, tileSection);
  }

  private void setTiles(BufferedImage img, Rectangle2D section) {
    final IMap map = this.getEnvironment().getMap();
    final Point startTile = MapUtilities.getTile(map, new Point2D.Double(section.getX(), section.getY()));
    final Point endTile = MapUtilities.getTile(map, new Point2D.Double(section.getMaxX(), section.getMaxY()));
    final int startX = MathUtilities.clamp(startTile.x, 0, Math.min(startTile.x + (endTile.x - startTile.x), tiles.length) - 1);
    final int startY = MathUtilities.clamp(startTile.y, 0, Math.min(startTile.y + (endTile.y - startTile.y), tiles[0].length) - 1);

    final int endX = MathUtilities.clamp(endTile.x, 0, Math.min(startTile.x + (endTile.x - startTile.x), tiles.length) - 1);
    final int endY = MathUtilities.clamp(endTile.y, 0, Math.min(startTile.y + (endTile.y - startTile.y), tiles[0].length) - 1);

    for (int x = startX; x <= endX; x++) {
      for (int y = startY; y <= endY; y++) {

        int jumpWidth = map.getTileWidth();
        int jumpHeight = map.getTileHeight();

        //for staggered maps, we must adjust our jump size for cropping the subImages since tiles are not aligned orthogonally.
        if (map.getOrientation() == MapOrientation.HEXAGONAL) {
          //the t parameter describes the distance between one end of the flat hex side to the bounding box.
          int t = map.getStaggerAxis() == StaggerAxis.X ? (map.getTileWidth() - map.getHexSideLength()) / 2 : (map.getTileHeight() - map.getHexSideLength()) / 2;
          jumpWidth = map.getStaggerAxis() == StaggerAxis.X ? t + map.getHexSideLength() : map.getTileWidth() / 2;
          jumpHeight = map.getStaggerAxis() == StaggerAxis.Y ? t + map.getHexSideLength() : map.getTileHeight() / 2;
        }
        final int subX = (x - startX) * jumpWidth;
        final int subY = (y - startY) * jumpHeight;
        final BufferedImage smallImage = img.getSubimage(subX, subY, map.getTileSize().width, map.getTileSize().height);
        this.tiles[x][y] = smallImage;
      }
    }
  }

  protected abstract void renderSection(Graphics2D g, Rectangle2D section);

  protected IEnvironment getEnvironment() {
    return this.environment;
  }
}
