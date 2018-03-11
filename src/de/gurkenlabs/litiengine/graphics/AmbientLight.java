package de.gurkenlabs.litiengine.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import de.gurkenlabs.litiengine.util.geom.Vector2D;

public class AmbientLight extends ColorLayer {

  public AmbientLight(final IEnvironment env, final Color ambientColor, final int ambientAlpha) {
    super(env, ambientColor, ambientAlpha);
    this.createImage();
  }

  @Override
  protected void renderLayer(Graphics2D g) {
    final Color colorWithAlpha = this.getColorWithAlpha();

    // create large rectangle and crop lights from it
    final double mapWidth = this.getEnvironment().getMap().getSizeInPixels().getWidth();
    final double mapHeight = this.getEnvironment().getMap().getSizeInPixels().getHeight();
    double longerDimension = mapWidth;
    if (mapWidth < mapHeight) {
      longerDimension = mapHeight;
    }
    final Area darkArea = new Area(new Rectangle2D.Double(0, 0, mapWidth, mapHeight));

    for (final LightSource light : this.getEnvironment().getLightSources()) {
      if (!light.isActive()) {
        continue;
      }

      this.renderLightSource(g, light, longerDimension);
    }

    g.setColor(colorWithAlpha);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 1.0f));
    g.fill(darkArea);

    for (final LightSource light : this.getEnvironment().getLightSources()) {
      if (light.getIntensity() <= 0) {
        continue;
      }

      final float intensity = MathUtilities.clamp((float) light.getIntensity() / 255, 0, 1);
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, intensity));
      this.renderLightSource(g, light, longerDimension);
    }
  }

  @Override
  protected String getCacheKey() {
    final StringBuilder sb = new StringBuilder();
    sb.append(this.getColor());
    sb.append(this.getAlpha());

    for (final LightSource light : this.getEnvironment().getLightSources()) {
      sb.append(light.getIntensity());
      sb.append(light.getColor());
      sb.append(light.getLocation());
      sb.append(light.getRadius());
      sb.append(light.getLightShapeType());
      sb.append(light.getWidth());
      sb.append(light.getHeight());
      sb.append(light.isActive());
    }

    sb.append(this.getEnvironment().getMap().getSizeInPixels());

    final int key = sb.toString().hashCode();
    return "ambientlight-" + this.getEnvironment().getMap().getFileName() + "-" + Integer.toString(key);
  }

  private void renderLightSource(final Graphics2D g, final LightSource light, final double longerDimension) {
    final Point2D lightCenter = light.getCenter();

    Area lightArea = null;
    if (light.getLightShapeType().equals(LightSource.RECTANGLE)) {
      g.setColor(light.getColor());
      g.fill(light.getBoundingBox());
      return;
    }

    // cut the light area where shadow Boxes are (this simulates light falling
    // into and out of rooms)
    for (final StaticShadow col : this.getEnvironment().getStaticShadows()) {
      if (!light.getBoundingBox().intersects(col.getBoundingBox())) {
        continue;
      }

      if (lightArea == null) {
        lightArea = new Area(light.getLightShape());
      }

      if (!lightArea.intersects(col.getBoundingBox())) {
        continue;
      }

      final Area boxInLight = new Area(col.getBoundingBox());

      final Line2D[] bounds = GeometricUtilities.getLines(col.getBoundingBox());
      for (final Line2D line : bounds) {
        final Vector2D lineVector = new Vector2D(line.getP1(), line.getP2());
        final Vector2D lightVector = new Vector2D(lightCenter, line.getP1());

        if (light.getCenter().getY() < line.getY1() && light.getCenter().getY() < line.getY2() && col.getBoundingBox().contains(light.getCenter()) || lineVector.normalVector().dotProduct(lightVector) >= 0) {
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
        if (light.getCenter().getY() < col.getBoundingBox().getMaxY() && !col.getBoundingBox().contains(light.getCenter())) {
          shadowArea.add(boxInLight);
        }
        shadowArea.intersect(lightArea);
        lightArea.subtract(shadowArea);
      }
    }

    final Paint oldPaint = g.getPaint();

    // render parts that lie within the shadow with a gradient from the light
    // color to transparent
    final Shape lightShape = light.getLightShape();
    final Color[] transColors = new Color[] { light.getColor(), new Color(light.getColor().getRed(), light.getColor().getGreen(), light.getColor().getBlue(), 0) };
    g.setPaint(new RadialGradientPaint(new Point2D.Double(lightShape.getBounds2D().getCenterX(), lightShape.getBounds2D().getCenterY()), (float) (lightShape.getBounds2D().getWidth() / 2), new float[] { 0.0f, 1.00f }, transColors));
    g.fill(lightArea == null ? light.getBoundingBox() : lightArea);
    g.setPaint(oldPaint);
  }
}