package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.util.geom.GeometricUtilities;

/**
 * The Class LightSource.
 */
@EntityInfo(renderType = RenderType.GROUND)
public class LightSource extends Entity implements IRenderable {
  public static final String ELLIPSE = "ellipse";

  public static final String RECTANGLE = "rectangle";

  public static final String TOGGLE_MESSAGE = "toggle";

  /** The gradient radius for our shadow. */
  private static final float OBSTRUCTED_VISION_RADIUS = 200f;

  /** The gradient radius for our shadow. */
  private static final float SHADOW_GRADIENT_SIZE = 100f;

  /**
   * } The fractions for our shadow gradient, going from 0.0 (black) to 1.0
   * (transparent).
   */
  private static final float[] SHADOW_GRADIENT_FRACTIONS = new float[] { 0f, 1f };

  /**
   * The colors for our shadow, going from opaque black to transparent black.
   */
  private static final Color[] SHADOW_GRADIENT_COLORS = new Color[] { new Color(0, 0, 0, .3f), new Color(0f, 0f, 0f, 0f) };

  private boolean activated;
  /** The brightness. */
  private int brightness;

  /** The color. */
  private Color color;
  private int intensity;

  private Shape lightShape;

  private String lightShapeType;

  /** The radius. */
  private int radius;

  /**
   * Instantiates a new light source.
   *
   * @param brightness
   *          the brightness
   * @param lightColor
   *          the light color
   */
  public LightSource(final int brightness, final int intensity, final Color lightColor, final String shapeType, boolean activated) {
    super();
    this.color = lightColor;
    this.intensity = intensity;

    this.setBrightness(brightness);
    this.lightShapeType = shapeType;
    this.activated = activated;
  }

  public void activate() {
    this.activated = true;
  }

  public void deactivate() {
    this.activated = false;
  }

  /**
   * Gets the brightness.
   *
   * @return the brightness
   */
  public int getBrightness() {
    return this.activated ? this.brightness : 0;
  }

  /**
   * Gets the color.
   *
   * @return the color
   */
  public Color getColor() {
    return this.color;
  }

  public int getIntensity() {
    return this.activated ? this.intensity : 0;
  }

  public Shape getLightShape() {
    return this.lightShape;
  }

  public String getLightShapeType() {
    return this.lightShapeType;
  }

  /**
   * Gets the radius.
   *
   * @return the radius
   */
  public int getRadius() {
    return this.radius;
  }

  public boolean isActive() {
    return this.activated;
  }

  @Override
  public void render(final Graphics2D g) {
    if (Game.getConfiguration().graphics().renderDynamicShadows()) {
      this.renderShadows(g);
    }
  }

  /**
   * Sets the brightness.
   *
   * @param brightness
   *          the new brightness
   */
  public void setBrightness(final int brightness) {
    this.brightness = brightness;
  }

  public void setColor(final Color result) {
    this.color = result;
  }

  public void setIntensity(final int intensity) {
    this.intensity = intensity;
  }

  public void setLightShapeType(final String shapeType) {
    this.lightShapeType = shapeType;
  }

  @Override
  public void setLocation(final Point2D location) {
    super.setLocation(location);
    switch (this.getLightShapeType()) {
    case LightSource.ELLIPSE:
      this.lightShape = new Ellipse2D.Double(location.getX(), location.getY(), this.getWidth(), this.getHeight());
      break;
    case LightSource.RECTANGLE:
      this.lightShape = new Rectangle2D.Double(location.getX(), location.getY(), this.getWidth(), this.getHeight());
      break;
    default:
      this.lightShape = new Ellipse2D.Double(location.getX(), location.getY(), this.getWidth(), this.getHeight());
      break;
    }
  }

  @Override
  public void setSize(final float width, final float height) {
    super.setSize(width, height);
    double shorterDimension = width;
    if (width > height) {
      shorterDimension = height;
    }
    this.setRadius((int) shorterDimension / 2);
  }

  public void toggle() {
    this.activated = !this.activated;
  }

  @Override
  public String sendMessage(final Object sender, final String message) {
    if (message == null || message.isEmpty()) {
      return null;
    }

    if (message.equals(TOGGLE_MESSAGE)) {
      this.toggle();
      return Boolean.toString(this.activated);
    }

    return null;
  }

  /**
   * Gets the shadow ellipse.
   *
   * @param mob
   *          the mob
   * @return the shadow ellipse
   */
  private static Ellipse2D getShadowEllipse(final IEntity mob) {
    final int shadowHeight = (int) (mob.getHeight() / 4);
    final int shadowWidth = (int) (mob.getWidth() / 3);

    final int yOffset = (int) mob.getHeight();
    final double x = mob.getLocation().getX() + (mob.getWidth() - shadowWidth) / 2;
    final double y = mob.getLocation().getY() + yOffset - shadowHeight / 2.0;
    return new Ellipse2D.Double(x, y, shadowWidth, shadowHeight);
  }

  /**
   * Checks if is in range.
   *
   * @param center
   *          the center
   * @param radius
   *          the radius
   * @return the predicate<? super mob>
   */
  private static Predicate<? super IEntity> isInRange(final Point2D center, final float radius) {
    return mob -> new Ellipse2D.Double(center.getX() - radius, center.getY() - radius, radius * 2, radius * 2).contains(mob.getDimensionCenter());
  }

  /**
   * Gets the obstructed vision area.
   *
   * @param mob
   *          the mob
   * @param center
   *          the center
   * @return the obstructed vision area
   */
  private Area getObstructedVisionArea(final IEntity mob, final Point2D center) {
    final Polygon shadowPolygon = new Polygon();

    final Ellipse2D shadowEllipse = getShadowEllipse(mob);

    final Rectangle2D bounds = shadowEllipse.getBounds2D();

    // radius of Entity's bounding circle
    final float r = (float) bounds.getWidth() / 2f;
    final float ry = (float) bounds.getHeight() / 2f;

    // get relative center of entity
    final Point2D relativeCenter = Game.getCamera().getViewPortLocation(new Point((int) (bounds.getX() + r), (int) (bounds.getY() + ry)));
    final double cx = relativeCenter.getX();
    final double cy = relativeCenter.getY();

    // get direction from light to entity center
    final double dx = cx - center.getX();
    final double dy = cy - center.getY();

    // get euclidean distance from entity to center
    final double distSq = dx * dx + dy * dy; // avoid sqrt for performance

    // normalize the direction to a unit vector
    final float len = (float) Math.sqrt(distSq);
    double nx = dx;
    double ny = dy;
    if (len != 0) { // avoid division by 0
      nx /= len;
      ny /= len;
    }

    // get perpendicular of unit vector
    final double px = -ny;
    final double py = nx;

    // our perpendicular points in either direction from radius
    final Point2D.Double pointA = new Point2D.Double(cx - px * r, cy - py * ry);
    final Point2D.Double pointB = new Point2D.Double(cx + px * r, cy + py * ry);

    // project the points by our SHADOW_EXTRUDE amount
    final Point2D pointC = GeometricUtilities.project(center, pointA, OBSTRUCTED_VISION_RADIUS);
    final Point2D pointD = GeometricUtilities.project(center, pointB, OBSTRUCTED_VISION_RADIUS);

    // construct a polygon from our points
    shadowPolygon.reset();
    shadowPolygon.addPoint((int) pointA.getX(), (int) pointA.getY());
    shadowPolygon.addPoint((int) pointB.getX(), (int) pointB.getY());
    shadowPolygon.addPoint((int) pointD.getX(), (int) pointD.getY());
    shadowPolygon.addPoint((int) pointC.getX(), (int) pointC.getY());

    final Point2D shadowRenderLocation = Game.getCamera().getViewPortLocation(new Point2D.Double(shadowEllipse.getX(), shadowEllipse.getY()));
    final Ellipse2D relativeEllipse = new Ellipse2D.Double(shadowRenderLocation.getX(), shadowRenderLocation.getY(), shadowEllipse.getWidth(), shadowEllipse.getHeight());

    final Area ellipseArea = new Area(relativeEllipse);
    final Area shadowArea = new Area(shadowPolygon);
    shadowArea.add(ellipseArea);
    return shadowArea;
  }

  /**
   * Renders the shadows using simple vector math. The steps are as follows:
   *
   * <pre>
   * for each entity
   *     if entity is not moving:
   *         ignore entity
   *     if entity is too far from mouse:
   *         ignore entity
   *
   *     determine unit vector from mouse to entity center
   *     get perpendicular of unit vector
   *
   *     Create Points A + B:
   *         extrude perpendicular in either direction, by the half-size of the entity
   *     Create Points C + D:
   *         extrude A + B away from mouse position
   *
   *     construct polygon with points A, B, C, D
   *
   *     render with RadialGradientPaint to give it a "fade-out" appearance
   * </pre>
   *
   * @param g
   *          the graphics to use for rendering
   * @param center
   *          the center
   */
  private void renderShadows(final Graphics2D g) {
    if (!Game.getEnvironment().getCombatEntities().stream().anyMatch(isInRange(this.getDimensionCenter(), SHADOW_GRADIENT_SIZE))) {
      return;
    }

    // we'll use a radial gradient
    final Paint gradientPaint = new RadialGradientPaint(Game.getCamera().getViewPortDimensionCenter(this), SHADOW_GRADIENT_SIZE, SHADOW_GRADIENT_FRACTIONS, SHADOW_GRADIENT_COLORS);

    // old Paint object for resetting it later
    final Paint oldPaint = g.getPaint();
    g.setPaint(gradientPaint);

    // for each entity
    for (final ICombatEntity mob : Game.getEnvironment().getCombatEntities()) {
      if (mob.isDead() || !isInRange(this.getDimensionCenter(), SHADOW_GRADIENT_SIZE).test(mob)) {
        continue;
      }

      final Shape obstructedVision = this.getObstructedVisionArea(mob, Game.getCamera().getViewPortDimensionCenter(this));
      // fill the polygon with the gradient paint

      g.fill(obstructedVision);
    }

    // reset to old Paint object
    g.setPaint(oldPaint);
  }

  /**
   * Sets the radius.
   *
   * @param radius
   *          the new radius
   */
  private void setRadius(final int radius) {
    this.radius = radius;
  }
}
