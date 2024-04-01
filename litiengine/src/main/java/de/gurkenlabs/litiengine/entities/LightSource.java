package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxType;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
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

/**
 * This class represents a light source in an environment. It extends the Entity class and implements IRenderable interface. It provides methods to
 * render light sources and shadows.
 */
@EntityInfo(renderType = RenderType.OVERLAY)
@TmxType(MapObjectType.LIGHTSOURCE)
public class LightSource extends Entity implements IRenderable {
  /**
   * Enum representing the type of light source.
   */
  public enum Type {
    ELLIPSE,
    RECTANGLE
  }

  public static final String TOGGLE_MESSAGE = "toggle";
  public static final int DEFAULT_INTENSITY = 100;

  private static final float OBSTRUCTED_VISION_RADIUS = 200f;
  private static final float SHADOW_GRADIENT_SIZE = 100f;
  /**
   * The fractions for our shadow gradient, going from 0.0 (black) to 1.0 (transparent).
   */
  private static final float[] SHADOW_GRADIENT_FRACTIONS = new float[] {0f, 1f};
  /**
   * The colors for our shadow, going from opaque black to transparent black.
   */
  private static final Color[] SHADOW_GRADIENT_COLORS =
    new Color[] {new Color(0, 0, 0, .3f), new Color(0f, 0f, 0f, 0f)};

  @TmxProperty(name = MapObjectProperty.LIGHT_ACTIVE)
  private boolean activated;

  @TmxProperty(name = MapObjectProperty.LIGHT_INTENSITY)
  private int intensity;

  @TmxProperty(name = MapObjectProperty.LIGHT_SHAPE)
  private Type lightShapeType;

  private Color color;
  private Shape lightShape;

  /**
   * Constructor for the LightSource class.
   *
   * @param intensity  The intensity of this instance.
   * @param lightColor The color of this instance.
   * @param shapeType  The shape type of this instance.
   * @param activated  A flag indicating whether this light is activated by default.
   */
  public LightSource(final int intensity, final Color lightColor, final Type shapeType, boolean activated) {
    super();
    this.color = lightColor;
    this.intensity = intensity;

    this.lightShapeType = shapeType;
    this.activated = activated;
  }

  /**
   * Activates the light source.
   */
  public void activate() {
    if (isActive()) {
      return;
    }
    this.activated = true;
    updateAmbientLayers();
  }

  /**
   * Deactivates the light source.
   */
  public void deactivate() {
    if (!isActive()) {
      return;
    }

    this.activated = false;
    updateAmbientLayers();
  }

  public Color getColor() {
    return color;
  }

  public RadialGradientPaint getGradientPaint() {
    final Color[] transColors =
      new Color[] {
        getColor().brighter(),
        getColor(),
        new Color(
          getColor().getRed(), getColor().getGreen(), getColor().getBlue(), 0)
      };
    float[] colorFractions = new float[] {0.0f, 0.3f, 1.0f};
    return new RadialGradientPaint(getLightShape().getBounds2D(), colorFractions, transColors, CycleMethod.NO_CYCLE);
  }


  public int getIntensity() {
    return isActive() ? intensity : 0;
  }

  public Shape getLightShape() {
    return lightShape;
  }

  public Type getLightShapeType() {
    return lightShapeType;
  }

  public boolean isActive() {
    return activated;
  }

  public void setColor(final Color color) {
    this.color = color;
    updateAmbientLayers();
  }

  public void setIntensity(final int intensity) {
    this.intensity = intensity;
    updateAmbientLayers();
  }

  public void setLightShapeType(final Type shapeType) {
    this.lightShapeType = shapeType;
  }


  @Override public void setSize(double width, double height) {
    super.setSize(width, height);
    updateShape();
    updateAmbientLayers();
  }

  @Override
  public void setLocation(final Point2D location) {
    super.setLocation(location);
    updateShape();
    updateAmbientLayers();
  }

  /**
   * Toggles the light source between active and inactive states.
   */
  public void toggle() {
    this.activated = !this.activated;
    updateAmbientLayers();
  }

  @Override
  public String sendMessage(final Object sender, final String message) {
    if (message == null || message.isEmpty()) {
      return null;
    }

    if (message.equals(TOGGLE_MESSAGE)) {
      toggle();
      return Boolean.toString(isActive());
    }

    return null;
  }

  @Override
  public void render(final Graphics2D graphic) {
    if (Game.config().graphics().renderDynamicShadows()) {
      renderShadows(graphic);
    }
  }


  /**
   * Updates the ambient layers of the environment.
   */
  private void updateAmbientLayers() {
    if (!isLoaded()) {
      return;
    }

    if (Game.world().environment() != null
      && Game.world().environment().getAmbientLight() != null) {
      Game.world().environment().getAmbientLight().updateSection(getBoundingBox());
    }

    if (Game.world().environment() != null
      && Game.world().environment().getStaticShadowLayer() != null) {
      Game.world().environment().getStaticShadowLayer().updateSection(getBoundingBox());
    }
  }

  /**
   * Updates the shape of the light source based on its type.
   */
  private void updateShape() {
    if (getLightShapeType() == Type.RECTANGLE) {
      this.lightShape =
        new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
    } else {
      this.lightShape =
        new Ellipse2D.Double(getX(), getY(), getWidth(), getHeight());
    }
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
   *         extrude A + B away from mouse location
   *
   *     construct polygon with points A, B, C, D
   *
   *     render with RadialGradientPaint to give it a "fade-out" appearance
   * </pre>
   *
   * @param graphic the graphics to use for rendering
   */
  private void renderShadows(final Graphics2D graphic) {
    if (Game.world().environment().getCombatEntities().stream()
      .noneMatch(isInRange(getCenter()))) {
      return;
    }

    // we'll use a radial gradient
    final Paint gradientPaint =
      new RadialGradientPaint(
        Game.world().camera().getViewportDimensionCenter(this),
        SHADOW_GRADIENT_SIZE,
        SHADOW_GRADIENT_FRACTIONS,
        SHADOW_GRADIENT_COLORS);

    // old Paint object for resetting it later
    final Paint oldPaint = graphic.getPaint();
    graphic.setPaint(gradientPaint);

    // for each entity
    for (final ICombatEntity mob : Game.world().environment().getCombatEntities()) {
      if (mob.isDead() || !isInRange(getCenter()).test(mob)) {
        continue;
      }

      final Shape obstructedVision =
        getObstructedVisionArea(mob, Game.world().camera().getViewportDimensionCenter(this));
      // fill the polygon with the gradient paint

      ShapeRenderer.render(graphic, obstructedVision);
    }

    // reset to old Paint object
    graphic.setPaint(oldPaint);
  }

  private static Predicate<? super IEntity> isInRange(final Point2D center) {
    return mob -> new Ellipse2D.Double(center.getX() - LightSource.SHADOW_GRADIENT_SIZE, center.getY() - LightSource.SHADOW_GRADIENT_SIZE,
      LightSource.SHADOW_GRADIENT_SIZE
        * 2,
      LightSource.SHADOW_GRADIENT_SIZE * 2)
      .contains(mob.getCenter());
  }

  /**
   * Gets the area of obstructed vision for a given entity.
   *
   * @param entity The entity for which to get the obstructed vision area.
   * @param center The center point of the light source.
   * @return The area of obstructed vision.
   */
  private static Area getObstructedVisionArea(final IEntity entity, final Point2D center) {
    final Polygon shadowPolygon = new Polygon();

    final Ellipse2D shadowEllipse = getShadowEllipse(entity);

    final Rectangle2D bounds = shadowEllipse.getBounds2D();

    // radius of Entity's bounding circle
    final float r = (float) bounds.getWidth() / 2f;
    final float ry = (float) bounds.getHeight() / 2f;

    // get relative center of entity
    final Point2D relativeCenter =
      Game.world()
        .camera()
        .getViewportLocation(new Point((int) (bounds.getX() + r), (int) (bounds.getY() + ry)));
    final double cx = relativeCenter.getX();
    final double cy = relativeCenter.getY();

    // get direction from light to entity center
    final double dx = cx - center.getX();
    final double dy = cy - center.getY();

    // get Euclidean distance from entity to center
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

    final Point2D shadowRenderLocation =
      Game.world()
        .camera()
        .getViewportLocation(new Point2D.Double(shadowEllipse.getX(), shadowEllipse.getY()));
    final Ellipse2D relativeEllipse =
      new Ellipse2D.Double(
        shadowRenderLocation.getX(),
        shadowRenderLocation.getY(),
        shadowEllipse.getWidth(),
        shadowEllipse.getHeight());

    final Area ellipseArea = new Area(relativeEllipse);
    final Area shadowArea = new Area(shadowPolygon);
    shadowArea.add(ellipseArea);
    return shadowArea;
  }

  private static Ellipse2D getShadowEllipse(final IEntity entity) {
    final int shadowHeight = (int) (entity.getHeight() / 4);
    final int shadowWidth = (int) (entity.getWidth() / 3);

    final int yOffset = (int) entity.getHeight();
    final double x = entity.getX() + (entity.getWidth() - shadowWidth) / 2;
    final double y = entity.getY() + yOffset - shadowHeight / 2.0;
    return new Ellipse2D.Double(x, y, shadowWidth, shadowHeight);
  }
}
