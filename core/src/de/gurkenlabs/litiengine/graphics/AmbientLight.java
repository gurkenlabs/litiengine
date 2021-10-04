package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import de.gurkenlabs.litiengine.util.geom.Vector2D;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class AmbientLight extends ColorLayer {
  public static final Color DEFAULT_COLOR = new Color(0, 0, 0, 0);

  /**
   * Instantiates a new {@code AmbientLight} instance.
   *
   * @param environment The environment to which this instance is assigned.
   * @param ambientColor The color of this instance.
   */
  public AmbientLight(final Environment environment, final Color ambientColor) {
    super(environment, ambientColor);
  }

  /**
   * @see <a href="https://docs.oracle.com/javase/tutorial/2d/advanced/compositing.html">Compositing
   *     Graphics</a>
   */
  @Override
  protected void renderSection(Graphics2D g, Rectangle2D section) {
    this.renderAmbient(g, section);

    // carve out the lights that will be added
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, 1));
    for (final LightSource light : this.getEnvironment().getLightSources()) {
      if (!light.getBoundingBox().intersects(section) || !light.isActive()) {
        continue;
      }

      this.renderLightSource(g, light, section);
    }

    // render the actual lights, depending on their intensity
    for (final LightSource light : this.getEnvironment().getLightSources()) {
      if (!light.getBoundingBox().intersects(section)
          || !light.isActive()
          || light.getIntensity() <= 0) {
        continue;
      }

      final float intensity = MathUtilities.clamp((float) light.getIntensity() / 255, 0, 1);
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, intensity));
      this.renderLightSource(g, light, section);
    }
  }

  @Override
  protected void clearSection(Graphics2D g, Rectangle2D section) {
    g.setColor(new Color(0, 0, 0, 0));
    g.clearRect(
        (int) section.getX(),
        (int) section.getY(),
        (int) section.getWidth(),
        (int) section.getHeight());
  }

  private void renderAmbient(Graphics2D g, Rectangle2D section) {
    // create large rectangle and crop lights from it
    final double width = section.getWidth();
    final double height = section.getHeight();

    // render the basic am
    final Area ambientArea = new Area(new Rectangle2D.Double(0, 0, width, height));
    g.setColor(this.getColor());
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 1));
    g.fill(ambientArea);
  }

  private void renderLightSource(final Graphics2D g, final LightSource light, Rectangle2D section) {
    final double mapWidth = this.getEnvironment().getMap().getSizeInPixels().width;
    final double mapHeight = this.getEnvironment().getMap().getSizeInPixels().height;
    double longerDimension = mapWidth < mapHeight ? mapHeight : mapWidth;

    final Point2D lightCenter = light.getCenter();
    final Point2D lightFocus =
        new Point2D.Double(
            lightCenter.getX() + light.getBoundingBox().getWidth() * light.getFocusOffsetX(),
            lightCenter.getY() + light.getBoundingBox().getHeight() * light.getFocusOffsetY());
    Shape fillShape;

    Area lightArea = null;
    if (light.getLightShapeType() == LightSource.Type.RECTANGLE) {
      g.setColor(
          new Color(
              light.getColor().getRed(),
              light.getColor().getGreen(),
              light.getColor().getBlue(),
              light.getColor().getAlpha()));
      fillShape =
          new Rectangle2D.Double(
              light.getBoundingBox().getX() - section.getX(),
              light.getBoundingBox().getY() - section.getY(),
              light.getBoundingBox().getWidth(),
              light.getBoundingBox().getHeight());
      g.fill(fillShape);
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
        final Vector2D lightVector = new Vector2D(lightFocus, line.getP1());

        if (light.getCenter().getY() < line.getY1()
                && light.getCenter().getY() < line.getY2()
                && col.getBoundingBox().contains(light.getCenter())
            || lineVector.normalVector().dotProduct(lightVector) >= 0) {
          continue;
        }

        final Path2D shadowParallelogram = new Path2D.Double();
        final Point2D shadowPoint1 =
            GeometricUtilities.project(lightFocus, line.getP1(), longerDimension);
        final Point2D shadowPoint2 =
            GeometricUtilities.project(lightFocus, line.getP2(), longerDimension);

        // construct a shape from our points
        shadowParallelogram.moveTo(line.getP1().getX(), line.getP1().getY());
        shadowParallelogram.lineTo(shadowPoint1.getX(), shadowPoint1.getY());
        shadowParallelogram.lineTo(shadowPoint2.getX(), shadowPoint2.getY());
        shadowParallelogram.lineTo(line.getP2().getX(), line.getP2().getY());
        shadowParallelogram.closePath();

        final Area shadowArea = new Area(shadowParallelogram);
        if (light.getCenter().getY() < col.getBoundingBox().getMaxY()
            && !col.getBoundingBox().contains(light.getCenter())) {
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

    final double radius =
        lightShape.getBounds2D().getWidth() > lightShape.getBounds2D().getHeight()
            ? lightShape.getBounds2D().getWidth()
            : lightShape.getBounds2D().getHeight();
    final Color[] transColors =
        new Color[] {
          light.getColor(),
          new Color(
              light.getColor().getRed(), light.getColor().getGreen(), light.getColor().getBlue(), 0)
        };
    final Point2D center =
        new Point2D.Double(
            lightShape.getBounds2D().getCenterX() - section.getX(),
            lightShape.getBounds2D().getCenterY() - section.getY());
    final Point2D focus =
        new Point2D.Double(
            center.getX() + lightShape.getBounds2D().getWidth() * light.getFocusOffsetX(),
            center.getY() + lightShape.getBounds2D().getHeight() * light.getFocusOffsetY());
    RadialGradientPaint paint =
        new RadialGradientPaint(
            center,
            (float) (radius / 2d),
            focus,
            new float[] {0.0f, 1.00f},
            transColors,
            CycleMethod.NO_CYCLE);

    g.setPaint(paint);

    if (lightArea != null) {
      lightArea.transform(AffineTransform.getTranslateInstance(-section.getX(), -section.getY()));
      fillShape = lightArea;
    } else {
      fillShape =
          new Rectangle2D.Double(
              light.getBoundingBox().getX() - section.getX(),
              light.getBoundingBox().getY() - section.getY(),
              light.getBoundingBox().getWidth(),
              light.getBoundingBox().getHeight());
    }

    g.fill(fillShape);
    g.setPaint(oldPaint);
  }
}
