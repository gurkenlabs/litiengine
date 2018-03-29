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
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

@EntityInfo(renderType = RenderType.GROUND)
public class LightSource extends Entity implements IRenderable {
  public static final String ELLIPSE = "ellipse";
  public static final String RECTANGLE = "rectangle";
  public static final String TOGGLE_MESSAGE = "toggle";
  public static final int DEFAULT_INTENSITY = 100;

  private static final float OBSTRUCTED_VISION_RADIUS = 200f;
  private static final float SHADOW_GRADIENT_SIZE = 100f;
  /**
   * The fractions for our shadow gradient, going from 0.0 (black) to 1.0
   * (transparent).
   */
  private static final float[] SHADOW_GRADIENT_FRACTIONS = new float[] { 0f, 1f };
  /**
   * The colors for our shadow, going from opaque black to transparent black.
   */
  private static final Color[] SHADOW_GRADIENT_COLORS = new Color[] { new Color(0, 0, 0, .3f), new Color(0f, 0f, 0f, 0f) };

  private boolean activated;
  private Color color;
  private int intensity;
  private Shape lightShape;
  private String lightShapeType;
  private int radius;
  private double focusOffsetX;
  private double focusOffsetY;

  public LightSource(final int intensity, final Color lightColor, final String shapeType, boolean activated) {
    super();
    this.color = lightColor;
    this.intensity = intensity;

    this.lightShapeType = shapeType;
    this.activated = activated;
  }

  public void activate() {
    this.activated = true;
  }

  public void deactivate() {
    this.activated = false;
  }

  public Color getColor() {
    return this.color;
  }

  public double getFocusOffsetX() {
    return this.focusOffsetX;
  }

  public double getFocusOffsetY() {
    return this.focusOffsetY;
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

  public void setFocusOffsetX(double focusOffsetX) {
    this.focusOffsetX = focusOffsetX;
  }

  public void setFocusOffsetY(double focusOffsetY) {
    this.focusOffsetY = focusOffsetY;
  }

  public void setColor(final Color result) {
    this.color = result;
    this.updateAmbientLayers();
  }

  public void setIntensity(final int intensity) {
    this.intensity = intensity;
    this.updateAmbientLayers();
  }

  public void setLightShapeType(final String shapeType) {
    this.lightShapeType = shapeType;
  }

  @Override
  public void setX(double x) {
    super.setX(x);
    this.updateShape();
    this.updateAmbientLayers();
  }

  @Override
  public void setY(double y) {
    super.setY(y);
    this.updateShape();
    this.updateAmbientLayers();
  }

  @Override
  public void setWidth(float width) {
    super.setWidth(width);
    this.updateShape();
    this.updateAmbientLayers();
  }

  @Override
  public void setHeight(float height) {
    super.setHeight(height);
    this.updateShape();
    this.updateAmbientLayers();
  }

  @Override
  public void setLocation(final Point2D location) {
    super.setLocation(location);
    this.updateShape();
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
    this.updateAmbientLayers();
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

  private static Ellipse2D getShadowEllipse(final IEntity mob) {
    final int shadowHeight = (int) (mob.getHeight() / 4);
    final int shadowWidth = (int) (mob.getWidth() / 3);

    final int yOffset = (int) mob.getHeight();
    final double x = mob.getLocation().getX() + (mob.getWidth() - shadowWidth) / 2;
    final double y = mob.getLocation().getY() + yOffset - shadowHeight / 2.0;
    return new Ellipse2D.Double(x, y, shadowWidth, shadowHeight);
  }

  private static Predicate<? super IEntity> isInRange(final Point2D center, final float radius) {
    return mob -> new Ellipse2D.Double(center.getX() - radius, center.getY() - radius, radius * 2, radius * 2).contains(mob.getCenter());
  }

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
    if (!Game.getEnvironment().getCombatEntities().stream().anyMatch(isInRange(this.getCenter(), SHADOW_GRADIENT_SIZE))) {
      return;
    }

    // we'll use a radial gradient
    final Paint gradientPaint = new RadialGradientPaint(Game.getCamera().getViewPortDimensionCenter(this), SHADOW_GRADIENT_SIZE, SHADOW_GRADIENT_FRACTIONS, SHADOW_GRADIENT_COLORS);

    // old Paint object for resetting it later
    final Paint oldPaint = g.getPaint();
    g.setPaint(gradientPaint);

    // for each entity
    for (final ICombatEntity mob : Game.getEnvironment().getCombatEntities()) {
      if (mob.isDead() || !isInRange(this.getCenter(), SHADOW_GRADIENT_SIZE).test(mob)) {
        continue;
      }

      final Shape obstructedVision = this.getObstructedVisionArea(mob, Game.getCamera().getViewPortDimensionCenter(this));
      // fill the polygon with the gradient paint

      g.fill(obstructedVision);
    }

    // reset to old Paint object
    g.setPaint(oldPaint);
  }

  private void setRadius(final int radius) {
    this.radius = radius;
  }

  private void updateAmbientLayers() {
    if (Game.getEnvironment() != null && Game.getEnvironment().getAmbientLight() != null) {
      Game.getEnvironment().getAmbientLight().updateSection(this.getBoundingBox());
    }

    if (Game.getEnvironment() != null && Game.getEnvironment().getStaticShadowLayer() != null) {
      Game.getEnvironment().getStaticShadowLayer().updateSection(this.getBoundingBox());
    }
  }

  private void updateShape() {
    switch (this.getLightShapeType()) {
    case LightSource.ELLIPSE:
      this.lightShape = new Ellipse2D.Double(this.getX(), this.getY(), this.getWidth(), this.getHeight());
      break;
    case LightSource.RECTANGLE:
      this.lightShape = new Rectangle2D.Double(this.getX(), this.getY(), this.getWidth(), this.getHeight());
      break;
    default:
      this.lightShape = new Ellipse2D.Double(this.getX(), this.getY(), this.getWidth(), this.getHeight());
      break;
    }
  }
}
