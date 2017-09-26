package de.gurkenlabs.litiengine.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.util.MathUtilities;
import de.gurkenlabs.util.geom.GeometricUtilities;
import de.gurkenlabs.util.geom.Vector2D;

public class AmbientLight {
  private int alpha;
  private Color color;
  private final IEnvironment environment;
  private Image image;

  public AmbientLight(final IEnvironment env, final Color ambientColor, final int ambientAlpha) {
    this.environment = env;
    this.color = ambientColor;
    this.alpha = ambientAlpha;
    this.createImage();
  }

  public void createImage() {
    final String cacheKey = this.getCacheKey();
    if (ImageCache.IMAGES.containsKey(cacheKey)) {
      this.image = ImageCache.IMAGES.get(cacheKey);
      return;
    }

    final Color colorWithAlpha = new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), this.getAlpha());
    final BufferedImage img = ImageProcessing.getCompatibleImage((int) this.environment.getMap().getSizeInPixels().getWidth(), (int) this.environment.getMap().getSizeInPixels().getHeight());
    final Graphics2D g = img.createGraphics();

    // create large rectangle and crop lights from it
    final double mapWidth = this.environment.getMap().getSizeInPixels().getWidth();
    final double mapHeight = this.environment.getMap().getSizeInPixels().getHeight();
    double longerDimension = mapWidth;
    if (mapWidth < mapHeight) {
      longerDimension = mapHeight;
    }
    final Area darkArea = new Area(new Rectangle2D.Double(0, 0, mapWidth, mapHeight));

    for (final LightSource light : this.environment.getLightSources()) {
      if (!light.isActive()) {
        continue;
      }

      this.renderLightSource(g, light, longerDimension);
    }

    g.setColor(colorWithAlpha);
    final Composite comp = g.getComposite();

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 1.0f));
    g.fill(darkArea);

    for (final LightSource light : this.environment.getLightSources()) {
      if (light.getIntensity() <= 0) {
        continue;
      }

      final float intensity = MathUtilities.clamp((float) light.getIntensity() / 255, 0, 1);
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, intensity));
      this.renderLightSource(g, light, longerDimension);
    }

    g.setComposite(comp);
    g.dispose();
    this.image = img;

    if (!Game.getInfo().getName().equals("utiLITI")) {
      ImageCache.IMAGES.put(cacheKey, img);
    }
  }

  public int getAlpha() {
    return this.alpha;
  }

  public Color getColor() {
    return this.color;
  }

  public Image getImage() {
    this.createImage();
    return this.image;
  }

  public void setAlpha(int ambientAlpha) {
    this.alpha = MathUtilities.clamp(ambientAlpha, 0, 255);
    this.createImage();
  }

  public void setColor(final Color color) {
    this.color = color;
    this.createImage();
  }

  private String getCacheKey() {
    final StringBuilder sb = new StringBuilder();
    sb.append(this.getColor());
    sb.append(this.getAlpha());

    for (final LightSource light : this.environment.getLightSources()) {
      sb.append(light.getBrightness());
      sb.append(light.getIntensity());
      sb.append(light.getColor());
      sb.append(light.getLocation());
      sb.append(light.getRadius());
      sb.append(light.getLightShapeType());
      sb.append(light.getWidth());
      sb.append(light.getHeight());
    }

    sb.append(this.environment.getMap().getSizeInPixels());

    final int key = sb.toString().hashCode();
    return "ambientlight-" + this.environment.getMap().getFileName() + "-" + Integer.toString(key);
  }

  private void renderLightSource(final Graphics2D g, final LightSource light, final double longerDimension) {
    final Point2D lightCenter = light.getDimensionCenter();

    final Area lightArea = new Area(light.getLightShape());
    if (light.getLightShapeType().equals(LightSource.RECTANGLE)) {
      g.setColor(light.getColor());
      g.fill(lightArea);
      return;
    }

    // cut the light area where shadow Boxes are (this simulates light falling
    // into and out of rooms)
    for (final StaticShadow col : this.environment.getStaticShadows()) {
      if (!GeometricUtilities.shapeIntersects(light.getLightShape(), col.getBounds2D())) {
        continue;
      }
      final Area boxInLight = new Area(col.getBounds2D());
      boxInLight.intersect(lightArea);

      final Line2D[] bounds = GeometricUtilities.getLines(col.getBounds2D());
      for (final Line2D line : bounds) {
        if (light.getDimensionCenter().getY() < line.getY1() && light.getDimensionCenter().getY() < line.getY2() && col.getBounds2D().contains(light.getDimensionCenter())) {
          continue;
        }
        final Vector2D lineVector = new Vector2D(line.getP1(), line.getP2());
        final Vector2D lightVector = new Vector2D(lightCenter, line.getP1());

        if (lineVector.normalVector().dotProduct(lightVector) >= 0) {
          continue;
        }

        final Path2D shadowParallelogram = new Path2D.Double();
        final Point2D shadowPoint1 = GeometricUtilities.project(lightCenter, line.getP1(), longerDimension);
        final Point2D shadowPoint2 = GeometricUtilities.project(lightCenter, line.getP2(), longerDimension);

        // construct a shape from our points
        shadowParallelogram.moveTo(line.getP1().getX(), line.getP1().getY());
        shadowParallelogram.lineTo(shadowPoint1.getX(), shadowPoint1.getY());
        shadowParallelogram.lineTo(shadowPoint2.getX(), shadowPoint2.getY());
        shadowParallelogram.lineTo(line.getP2().getX(), line.getP2().getY());
        shadowParallelogram.closePath();

        final Area shadowArea = new Area(shadowParallelogram);
        if (light.getDimensionCenter().getY() < col.getBounds2D().getMaxY() && !col.getBounds2D().contains(light.getDimensionCenter())) {
          shadowArea.add(boxInLight);
        }
        shadowArea.intersect(lightArea);
        lightArea.subtract(shadowArea);
      }
    }

    final Paint oldPaint = g.getPaint();

    // render parts that lie within the shadow with a gradient from the light
    // color to transparent
    final Area lightRadiusArea = new Area(light.getLightShape());
    final Color[] transColors = new Color[] { new Color(light.getColor().getRed(), light.getColor().getGreen(), light.getColor().getBlue(), light.getBrightness()), new Color(light.getColor().getRed(), light.getColor().getGreen(), light.getColor().getBlue(), 0) };
    try {
      g.setPaint(new RadialGradientPaint(new Point2D.Double(lightRadiusArea.getBounds2D().getCenterX(), lightRadiusArea.getBounds2D().getCenterY()), (float) (lightRadiusArea.getBounds2D().getWidth() / 2), new float[] { 0.0f, 1.00f }, transColors));
    } catch (final Exception e) {
      g.setColor(light.getColor());
    }
    g.fill(lightArea);
    g.setPaint(oldPaint);
  }
}