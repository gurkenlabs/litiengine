package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.entities.Collider;
import de.gurkenlabs.litiengine.environment.IEnvironment;
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

  public void createImage() {
    final Color color = new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), this.getAlpha());
    final BufferedImage img = ImageProcessing.getCompatibleImage((int) this.environment.getMap().getSizeInPixels().getWidth(), (int) this.environment.getMap().getSizeInPixels().getHeight());
    final Graphics2D g = img.createGraphics();

    // final StringBuilder sb = new StringBuilder();

    // for (final LightSource light : this.environment.getLightSources()) {
    // sb.append(light.getRadius() + "_" + light.getLocation().getX() + "_" +
    // light.getLocation().getY());
    // }

    // build map specific cache key, respecting the lights and color
    // final String cacheKey = "AMBIENT_" +
    // this.environment.getMap().getName().replaceAll("[\\/]", "-") + "_" +
    // sb.toString().hashCode() + "_" + this.getColor().getRed() + "_" +
    // this.getColor().getGreen() + "_" + this.getColor().getBlue() + "_" +
    // this.getAlpha() + ".png";
    // final Image cachedImg = ImageCache.IMAGES.get(cacheKey);
    // if (cachedImg != null) {
    // this.image = cachedImg;
    // return;
    // }

    // create large rectangle and crop lights from it
    final double mapWidth = this.environment.getMap().getSizeInPixels().getWidth();
    final double mapHeight = this.environment.getMap().getSizeInPixels().getHeight();
    double longerDimension = mapWidth;
    if (mapWidth < mapHeight) {
      longerDimension = mapHeight;
    }
    final Area darkArea = new Area(new Rectangle2D.Double(0, 0, mapWidth, mapHeight));

    for (final LightSource light : this.environment.getLightSources()) {
      final Point2D lightCenter = light.getDimensionCenter();

      final Area lightArea = new Area(light.getLightShape());

      // cut the light area where shadow Boxes are (this simulates light falling
      // into and out of rooms)
      for (final Collider col : this.environment.getColliders()) {
        if (!GeometricUtilities.shapeIntersects(light.getLightShape(), col.getBoundingBox())) {
          continue;
        }
        final Area boxInLight = new Area(col.getCollisionBox());
        boxInLight.intersect(lightArea);

        final Line2D[] bounds = GeometricUtilities.getLines(col.getCollisionBox());
        for (final Line2D line : bounds) {
          if (light.getDimensionCenter().getY() < line.getY1() && light.getDimensionCenter().getY() < line.getY2() && col.getCollisionBox().contains(light.getDimensionCenter())) {
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
          if (light.getDimensionCenter().getY() < col.getCollisionBox().getMaxY() && !col.getCollisionBox().contains(light.getDimensionCenter())) {
            shadowArea.add(boxInLight);
          }
          shadowArea.intersect(lightArea);
          lightArea.subtract(shadowArea);
        }
      }
      darkArea.subtract(lightArea);

      Color[] colors = new Color[] { new Color(light.getColor().getRed(), light.getColor().getGreen(), light.getColor().getBlue(), light.getBrightness()), new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), (int) (this.getAlpha())) };
      try {
        g.setPaint(new RadialGradientPaint(new Point2D.Double(lightArea.getBounds2D().getCenterX(), lightArea.getBounds2D().getCenterY()), (float) (lightArea.getBounds2D().getWidth() / 2), new float[] { 0.0f, 1.00f }, colors));
      } catch (Exception e) {
        g.setColor(light.getColor());
      }
      g.fill(lightArea);
    }
    g.setColor(color);
    g.fill(darkArea);
    g.dispose();
    this.image = img;

    // ImageCache.IMAGES.put(cacheKey, img);
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