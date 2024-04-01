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
import java.awt.Paint;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * This class represents the ambient light in an environment. It extends the ColorLayer class and provides methods to render light sources and
 * shadows.
 */
public class AmbientLight extends ColorLayer {
  /**
   * The default color for the ambient light.
   */
  public static final Color DEFAULT_COLOR = new Color(0, 0, 0, 0);

  /**
   * Constructor for the AmbientLight class.
   *
   * @param environment  The environment to which this instance is assigned.
   * @param ambientColor The color of this instance.
   */
  public AmbientLight(final Environment environment, final Color ambientColor) {
    super(environment, ambientColor);
  }

  /**
   * Renders a section of the environment with the ambient light and light sources.
   *
   * @param g       The Graphics2D object to render on.
   * @param section The section of the environment to render.
   */
  @Override
  protected void renderSection(Graphics2D g, Rectangle2D section) {
    renderAmbient(g, section);

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, 1));
    getEnvironment().getLightSources().forEach(light -> carveOutLight(g, light, section));

    getEnvironment().getLightSources().forEach(light -> renderActualLight(g, light, section));
  }

  /**
   * Clears a section of the environment.
   *
   * @param g       The Graphics2D object to clear on.
   * @param section The section of the environment to clear.
   */
  @Override
  protected void clearSection(Graphics2D g, Rectangle2D section) {
    g.setColor(new Color(0, 0, 0, 0));
    g.clearRect(
      (int) section.getX(),
      (int) section.getY(),
      (int) section.getWidth(),
      (int) section.getHeight());
  }

  /**
   * Carves out a light source from the ambient light.
   *
   * @param g       The Graphics2D object to carve on.
   * @param light   The light source to carve out.
   * @param section The section of the environment to carve from.
   */
  private void carveOutLight(Graphics2D g, LightSource light, Rectangle2D section) {
    if (!light.getBoundingBox().intersects(section) || !light.isActive()) {
      return;
    }
    renderLightSource(g, light, section);
  }

  /**
   * Renders an actual light source on the environment.
   *
   * @param g       The Graphics2D object to render on.
   * @param light   The light source to render.
   * @param section The section of the environment to render on.
   */
  private void renderActualLight(Graphics2D g, LightSource light, Rectangle2D section) {
    if (!light.getBoundingBox().intersects(section)
      || !light.isActive()
      || light.getIntensity() <= 0) {
      return;
    }

    final float intensity = MathUtilities.clamp((float) light.getIntensity() / 255, 0, 1);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, intensity));
    renderLightSource(g, light, section);
  }

  /**
   * Renders the ambient light on the environment.
   *
   * @param g       The Graphics2D object to render on.
   * @param section The section of the environment to render.
   */
  private void renderAmbient(Graphics2D g, Rectangle2D section) {
    g.setColor(getColor());
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 1));
    ShapeRenderer.render(g, section);
  }

  /**
   * Subtracts a shadow from a light area.
   *
   * @param lightArea The light area to subtract from.
   * @param shadow    The shadow to subtract.
   */
  private void subtractShadow(Area lightArea, StaticShadow shadow) {
    if (!lightArea.intersects(shadow.getBoundingBox())) {
      return;
    }

    final double mapWidth = getEnvironment().getMap().getSizeInPixels().width;
    final double mapHeight = getEnvironment().getMap().getSizeInPixels().height;
    double longerDimension = Math.max(mapWidth, mapHeight);
    Point2D center = new Point2D.Double(lightArea.getBounds2D().getCenterX(), lightArea.getBounds2D().getCenterY());


    final Area boxInLight = new Area(shadow.getBoundingBox());

    final Line2D[] bounds = GeometricUtilities.getLines(shadow.getBoundingBox());
    for (final Line2D line : bounds) {
      final Vector2D lineVector = new Vector2D(line.getP1(), line.getP2());
      final Vector2D lightVector = new Vector2D(center, line.getP1());

      if (center.getY() < line.getY1()
        && center.getY() < line.getY2()
        && shadow.getBoundingBox().contains(center)
        || lineVector.normalVector().dotProduct(lightVector) >= 0) {
        continue;
      }

      final Path2D shadowParallelogram = new Path2D.Double();
      final Point2D shadowPoint1 =
        GeometricUtilities.project(center, line.getP1(), longerDimension);
      final Point2D shadowPoint2 =
        GeometricUtilities.project(center, line.getP2(), longerDimension);

      // construct a shape from our points
      shadowParallelogram.moveTo(line.getP1().getX(), line.getP1().getY());
      shadowParallelogram.lineTo(shadowPoint1.getX(), shadowPoint1.getY());
      shadowParallelogram.lineTo(shadowPoint2.getX(), shadowPoint2.getY());
      shadowParallelogram.lineTo(line.getP2().getX(), line.getP2().getY());
      shadowParallelogram.closePath();

      final Area shadowArea = new Area(shadowParallelogram);
      if (center.getY() < shadow.getBoundingBox().getMaxY()
        && !shadow.getBoundingBox().contains(center)) {
        shadowArea.add(boxInLight);
      }
      shadowArea.intersect(lightArea);
      lightArea.subtract(shadowArea);
    }
  }

  /**
   * Renders a light source on the environment.
   *
   * @param g       The Graphics2D object to render on.
   * @param light   The light source to render.
   * @param section The section of the environment to render on.
   */
  private void renderLightSource(final Graphics2D g, final LightSource light, Rectangle2D section) {

    Area lightArea = new Area(light.getLightShape());
    if (light.getLightShapeType() == LightSource.Type.RECTANGLE) {
      g.setColor(light.getColor());
      ShapeRenderer.render(g, new Rectangle2D.Double(
        light.getBoundingBox().getX() - section.getX(),
        light.getBoundingBox().getY() - section.getY(),
        light.getBoundingBox().getWidth(),
        light.getBoundingBox().getHeight()));
      return;
    }

    // cut the light area where shadow Boxes are (this simulates light falling into and out of rooms)
    getEnvironment().getStaticShadows().forEach(shadow -> subtractShadow(lightArea, shadow));

    final Paint oldPaint = g.getPaint();

    g.setPaint(light.getGradientPaint());
    ShapeRenderer.render(g, lightArea);
    g.setPaint(oldPaint);
  }
}
