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
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
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
    this.tiles = new Image[env.getMap().getSizeInTiles().width][env.getMap().getSizeInTiles().height];
    this.updateSection(this.environment.getMap().getBounds());
  }

  @Override
  public void render(Graphics2D g) {
    final Rectangle2D viewport = Game.getCamera().getViewPort();

    final IMap map = this.getEnvironment().getMap();
    final Point startTile = MapUtilities.getTile(map, new Point2D.Double(viewport.getX(), viewport.getY()));
    final Point endTile = MapUtilities.getTile(map, new Point2D.Double(viewport.getMaxX(), viewport.getMaxY()));
    final int startX = MathUtilities.clamp(startTile.x, 0, tiles.length - 1);
    final int endX = MathUtilities.clamp(endTile.x, 0, tiles.length - 1);
    final int startY = MathUtilities.clamp(startTile.y, 0, tiles[0].length - 1);
    final int endY = MathUtilities.clamp(endTile.y, 0, tiles[0].length - 1);

    final Point2D origin = Game.getCamera().getViewPortLocation(0, 0);

    // draw the tile on the layer image
    for (int x = startX; x <= endX; x++) {
      for (int y = startY; y <= endY; y++) {
        ImageRenderer.render(g, tiles[x][y], origin.getX() + x * map.getTileSize().width, origin.getY() + y * map.getTileSize().height);
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

    final int endX = MathUtilities.clamp(endTile.x, 0,  Math.min(startTile.x + (endTile.x - startTile.x), tiles.length) - 1);
    final int endY = MathUtilities.clamp(endTile.y, 0, Math.min(startTile.y + (endTile.y - startTile.y), tiles[0].length) - 1);

    for (int x = startX; x <= endX; x++) {
      for (int y = startY; y <= endY; y++) {
        final int subX = (x - startX) * map.getTileSize().width;
        final int subY = (y - startY) * map.getTileSize().height;

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
