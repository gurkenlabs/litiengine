package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;
import de.gurkenlabs.tiled.tmx.IMapObject;
import de.gurkenlabs.util.geom.GeometricUtilities;
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
    final Area ar = new Area(new Rectangle2D.Double(0, 0, this.environment.getMap().getSizeInPixles().getWidth(), this.environment.getMap().getSizeInPixles().getHeight()));
    for (final LightSource light : this.environment.getLightSources()) {
      final double LIGHT_GRADIENT_STEP = light.getRadius() * 0.15;
      Point2D lightCenter = light.getDimensionCenter();
      Point2D lightLocation = light.getLocation();
      final Ellipse2D lightCircle = new Ellipse2D.Double(lightLocation.getX(), lightLocation.getY(), light.getRadius() * 2, light.getRadius() * 2);
      final Ellipse2D midLightCircle = new Ellipse2D.Double(lightLocation.getX() + LIGHT_GRADIENT_STEP, lightLocation.getY() + LIGHT_GRADIENT_STEP, (light.getRadius() - LIGHT_GRADIENT_STEP) * 2, (light.getRadius() - LIGHT_GRADIENT_STEP) * 2);
      final Ellipse2D smallLightCircle = new Ellipse2D.Double(lightLocation.getX() + LIGHT_GRADIENT_STEP * 2, lightLocation.getY() + LIGHT_GRADIENT_STEP * 2, (light.getRadius() - LIGHT_GRADIENT_STEP * 2) * 2, (light.getRadius() - LIGHT_GRADIENT_STEP * 2) * 2);

      Area large = new Area(lightCircle);
      Area mid = new Area(midLightCircle);
      Area small = new Area(smallLightCircle);

      // cut the light area where shadow Boxes are (this simulates light falling
      // into and out of rooms)
      for (IMapObject obj : this.environment.getShadowBoxes()) {
        if (!GeometricUtilities.shapeIntersects(lightCircle, obj.getCollisionBox())) {
          continue;
        }
        Area boxInLight = new Area(obj.getCollisionBox());
        boxInLight.intersect(large);

        Line2D[] bounds = GeometricUtilities.getLines(obj.getCollisionBox());

        /** The gradient radius for our shadow. */
        for (Line2D line : bounds) {
          Path2D shadowParallelogram = new Path2D.Double();

          final Point2D S1 = GeometricUtilities.project(lightCenter, line.getP1(), light.getRadius() * 2);
          final Point2D S2 = GeometricUtilities.project(lightCenter, line.getP2(), light.getRadius() * 2);

          // construct a shape from our points
          shadowParallelogram.moveTo(line.getP1().getX(), line.getP1().getY());
          shadowParallelogram.lineTo(S1.getX(), S1.getY());
          shadowParallelogram.lineTo(S2.getX(), S2.getY());
          shadowParallelogram.lineTo(line.getP2().getX(), line.getP2().getY());
          shadowParallelogram.closePath();

          Area shadowArea = new Area(shadowParallelogram);

          shadowArea.add(boxInLight);
          shadowArea.intersect(large);

          large.subtract(shadowArea);

          mid.subtract(shadowArea);

          small.subtract(shadowArea);

        }
      }
      ar.subtract(large);

      g.setColor(new Color(light.getColor().getRed(), light.getColor().getGreen(), light.getColor().getBlue(), (int) (light.getBrightness())));
      g.fill(large);

      large.subtract(mid);

      g.setColor(new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), (int) (this.getAlpha() * 0.5)));
      g.fill(large);

      mid.subtract(small);
      g.setColor(new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), (int) (this.getAlpha() * 0.25)));
      g.fill(mid);

    }
    g.setColor(col);
    g.fill(ar);
    g.dispose();
    this.image = img;
    ImageCache.IMAGES.putPersistent(cacheKey, img);
  }
}