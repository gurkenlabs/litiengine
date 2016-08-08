package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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
      final Ellipse2D lightCircle = new Ellipse2D.Double(light.getLocation().getX(), light.getLocation().getY(), light.getRadius() * 2, light.getRadius() * 2);
      final Ellipse2D midLightCircle = new Ellipse2D.Double(light.getLocation().getX() + LIGHT_GRADIENT_STEP, light.getLocation().getY() + LIGHT_GRADIENT_STEP, (light.getRadius() - LIGHT_GRADIENT_STEP) * 2, (light.getRadius() - LIGHT_GRADIENT_STEP) * 2);
      final Ellipse2D smallLightCircle = new Ellipse2D.Double(light.getLocation().getX() + LIGHT_GRADIENT_STEP * 2, light.getLocation().getY() + LIGHT_GRADIENT_STEP * 2, (light.getRadius() - LIGHT_GRADIENT_STEP * 2) * 2, (light.getRadius() - LIGHT_GRADIENT_STEP * 2) * 2);

      Area lightArea = new Area(lightCircle);
      Area mid = new Area(midLightCircle);
      Area small = new Area(smallLightCircle);

      for (IMapObject obj : this.environment.getShadowBoxes()) {
        if (GeometricUtilities.shapeIntersects(lightCircle, obj.getCollisionBox().getBounds2D())) {
          // magic happens here
        }
      }
      ar.subtract(lightArea);

      lightArea.subtract(mid);
      g.setColor(new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), (int) (this.getAlpha() * 0.5)));
      g.fill(lightArea);

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