package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;
import de.gurkenlabs.tiled.tmx.IMapObject;
import de.gurkenlabs.util.geom.GeometricUtilities;
import de.gurkenlabs.util.geom.Vector2D;
import de.gurkenlabs.util.image.ImageProcessing;

public class AmbientLight {
  private final IEnvironment environment;
  private Image image;
  private Color color;
  private int alpha;

  public AmbientLight(final IEnvironment environment, final Color ambientColor, final int ambientAlpha) {
    this.environment = environment;
    this.color = ambientColor;
    this.alpha = ambientAlpha;
    this.createImage();
  }

  private void createImage() {
    final Color col = new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), this.getAlpha());
    final StringBuilder sb = new StringBuilder();
    final BufferedImage img = ImageProcessing.getCompatibleImage((int) this.environment.getMap().getSizeInPixles().getWidth(), (int) this.environment.getMap().getSizeInPixles().getHeight());
    final Graphics2D g = (Graphics2D) img.getGraphics();
    for (final LightSource light : this.environment.getLightSources()) {
      sb.append(light.getRadius() + "_" + light.getLocation().getX() + "_" + light.getLocation().getY());
    }

    // build map specific cache key, respecting the lights and color
    final String cacheKey = "AMBIENT_" + this.environment.getMap().getName().replaceAll("[\\/]", "-") + "_" + sb.toString().hashCode() + "_" + this.getColor().getRed() + "_" + this.getColor().getGreen() + "_" + this.getColor().getBlue() + "_" + this.getAlpha();
    final Image cachedImg = ImageCache.IMAGES.get(cacheKey);
    if (cachedImg != null) {
      this.image = cachedImg;
      return;
    }

    // create large rectangle and crop lights from it
    final double mapWidth = this.environment.getMap().getSizeInPixles().getWidth();
    final double mapHeight = this.environment.getMap().getSizeInPixles().getHeight();
    double longerDimension = mapWidth;
    if (mapWidth < mapHeight) {
      longerDimension = mapHeight;
    }
    final Area darkArea = new Area(new Rectangle2D.Double(0, 0, mapWidth, mapHeight));

    for (final LightSource light : this.environment.getLightSources()) {
      final Point2D lightCenter = light.getDimensionCenter();

      final Area large = new Area(light.getLargeLightShape());
      final Area mid = new Area(light.getMidLightShape());
      final Area small = new Area(light.getSmallLightShape());

      // cut the light area where shadow Boxes are (this simulates light falling
      // into and out of rooms)
      for (final IMapObject obj : this.environment.getShadowBoxes()) {
        if (!GeometricUtilities.shapeIntersects(light.getLargeLightShape(), obj.getCollisionBox())) {
          continue;
        }
        final Area boxInLight = new Area(obj.getCollisionBox());
        boxInLight.intersect(large);

        final Line2D[] bounds = GeometricUtilities.getLines(obj.getCollisionBox());
        for (final Line2D line : bounds) {
          if (light.getDimensionCenter().getY() < line.getY1() && light.getDimensionCenter().getY() < line.getY2() && obj.getCollisionBox().contains(light.getDimensionCenter())) {
            continue;
          }
          final Vector2D lineVector = new Vector2D(line.getP1(), line.getP2());
          final Vector2D lightVector = new Vector2D(lightCenter, line.getP1());

          if (lineVector.normalVector().dotProduct(lightVector) >= 0) {
            continue;
          }

          final Path2D shadowParallelogram = new Path2D.Double();
          final Point2D S1 = GeometricUtilities.project(lightCenter, line.getP1(), longerDimension);
          final Point2D S2 = GeometricUtilities.project(lightCenter, line.getP2(), longerDimension);

          // construct a shape from our points
          shadowParallelogram.moveTo(line.getP1().getX(), line.getP1().getY());
          shadowParallelogram.lineTo(S1.getX(), S1.getY());
          shadowParallelogram.lineTo(S2.getX(), S2.getY());
          shadowParallelogram.lineTo(line.getP2().getX(), line.getP2().getY());
          shadowParallelogram.closePath();

          final Area shadowArea = new Area(shadowParallelogram);
          if (light.getDimensionCenter().getY() < obj.getCollisionBox().getMaxY() && !obj.getCollisionBox().contains(light.getDimensionCenter())) {
            shadowArea.add(boxInLight);
          }
          shadowArea.intersect(large);
          large.subtract(shadowArea);
          mid.subtract(shadowArea);
          small.subtract(shadowArea);

        }
      }
      darkArea.subtract(large);

      g.setColor(new Color(light.getColor().getRed(), light.getColor().getGreen(), light.getColor().getBlue(), light.getBrightness()));
      g.fill(large);

      large.subtract(mid);

      g.setColor(new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), (int) (this.getAlpha() * 0.5)));
      g.fill(large);

      mid.subtract(small);
      g.setColor(new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), (int) (this.getAlpha() * 0.25)));
      g.fill(mid);

    }
    g.setColor(col);
    g.fill(darkArea);
    g.dispose();
    this.image = img;
    ImageCache.IMAGES.putPersistent(cacheKey, img);
  }

  public int getAlpha() {
    return this.alpha;
  }

  public Color getColor() {
    return this.color;
  }

  public Image getImage() {
    return this.image;
  }

  public void setAlpha(int ambientAlpha) {
    if (ambientAlpha < 0) {
      ambientAlpha = 0;
    }

    this.alpha = Math.min(ambientAlpha, 255);
    this.createImage();
  }

  public void setColor(final Color color) {
    this.color = color;
    this.createImage();
  }
}