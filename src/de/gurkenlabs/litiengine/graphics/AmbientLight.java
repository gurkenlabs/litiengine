package de.gurkenlabs.litiengine.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
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
  }

  @Override
  protected void renderSection(Graphics2D g, Rectangle2D section) {
    final Color colorWithAlpha = this.getColorWithAlpha();

    // create large rectangle and crop lights from it
    final double width = section.getWidth();
    final double height = section.getHeight();

    final double mapWidth = this.getEnvironment().getMap().getSizeInPixels().width;
    final double mapHeight = this.getEnvironment().getMap().getSizeInPixels().height;
    double longerDimension = mapWidth;
    if (mapWidth < mapHeight) {
      longerDimension = mapHeight;
    }
    final Area darkArea = new Area(new Rectangle2D.Double(0, 0, width, height));

    for (final LightSource light : this.getEnvironment().getLightSources()) {
      if (!light.getBoundingBox().intersects(section) || !light.isActive()) {
        continue;
      }

      this.renderLightSource(g, light, longerDimension, section);
    }

    g.setColor(colorWithAlpha);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 1.0f));
    g.fill(darkArea);

    for (final LightSource light : this.getEnvironment().getLightSources()) {
      if (!light.getBoundingBox().intersects(section) || !light.isActive() || light.getIntensity() <= 0) {
        continue;
      }

      final float intensity = MathUtilities.clamp((float) light.getIntensity() / 255, 0, 1);
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, intensity));
      this.renderLightSource(g, light, longerDimension, section);
    }
  }

  private void renderLightSource(final Graphics2D g, final LightSource light, final double longerDimension, Rectangle2D section) {
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
    g.setPaint(new RadialGradientPaint(new Point2D.Double(lightShape.getBounds2D().getCenterX() - section.getX(), lightShape.getBounds2D().getCenterY() - section.getY()), (float) (lightShape.getBounds2D().getWidth() / 2), new float[] { 0.0f, 1.00f }, transColors));

    Shape fillShape;
    if (lightArea != null) {
      lightArea.transform(AffineTransform.getTranslateInstance(-section.getX(), -section.getY()));
      fillShape = lightArea;
    } else {
      fillShape = new Rectangle2D.Double(light.getBoundingBox().getX() - section.getX(), light.getBoundingBox().getY() - section.getY(), light.getBoundingBox().getWidth(), light.getBoundingBox().getHeight());
    }

    g.fill(fillShape);
    g.setPaint(oldPaint);
  }
}