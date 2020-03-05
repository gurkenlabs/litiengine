package de.gurkenlabs.litiengine.entities;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.StaticShadowType;

@EntityInfo(renderType = RenderType.OVERLAY)
public class StaticShadow extends MapArea {
  public static final Color DEFAULT_COLOR = new Color(0, 0, 0, 75);
  public static final int DEFAULT_OFFSET = 10;

  @TmxProperty(name = MapObjectProperty.SHADOW_TYPE)
  private StaticShadowType shadowType;

  @TmxProperty(name = MapObjectProperty.SHADOW_OFFSET)
  private int shadowOffset;

  private final CollisionBox origin;
  private Area area;

  /**
   * Instantiates a new <code>StaticShadow</code> entity.
   *
   * @param shadowType
   *          The type of the static shadow.
   * @param offset
   *          The offset for the shadow.
   */
  public StaticShadow(StaticShadowType shadowType, int offset) {
    this.shadowType = shadowType;
    this.shadowOffset = offset;
    this.origin = null;
  }

  /**
   * Instantiates a new <code>StaticShadow</code> entity.
   *
   * @param shadowType
   *          The type of the static shadow.
   */
  public StaticShadow(StaticShadowType shadowType) {
    this.shadowType = shadowType;
    this.shadowOffset = DEFAULT_OFFSET;
    this.origin = null;
  }

  /**
   * Instantiates a new <code>StaticShadow</code> entity.
   *
   * @param x
   *          The x-coordinate of this instance.
   * @param y
   *          The y-coordinate of this instance.
   * @param width
   *          The width of this instance.
   * @param height
   *          The height of this instance.
   * @param shadowType
   *          The type of the static shadow.
   */
  public StaticShadow(double x, double y, float width, float height, StaticShadowType shadowType) {
    this(0, null, x, y, width, height, shadowType);
  }

  /**
   * Instantiates a new <code>StaticShadow</code> entity.
   *
   * @param id
   *          The id of this entity.
   * @param x
   *          The x-coordinate of this instance.
   * @param y
   *          The y-coordinate of this instance.
   * @param width
   *          The width of this instance.
   * @param height
   *          The height of this instance.
   * @param shadowType
   *          The type of the static shadow.
   */
  public StaticShadow(int id, double x, double y, float width, float height, StaticShadowType shadowType) {
    this(id, null, x, y, width, height, shadowType);
  }

  /**
   * Instantiates a new <code>StaticShadow</code> entity.
   *
   * @param id
   *          The id of this entity.
   * @param name
   *          The name of this entity.
   * @param x
   *          The x-coordinate of this instance.
   * @param y
   *          The y-coordinate of this instance.
   * @param width
   *          The width of this instance.
   * @param height
   *          The height of this instance.
   * @param shadowType
   *          The type of the static shadow.
   */
  public StaticShadow(int id, String name, double x, double y, float width, float height, StaticShadowType shadowType) {
    super(id, name, x, y, width, height);
    this.setShadowType(shadowType);
    this.origin = null;
    this.shadowOffset = DEFAULT_OFFSET;
  }

  /**
   * Instantiates a new <code>StaticShadow</code> entity.
   *
   * @param collisionBox
   *          The collision box from which this shadow instance originates from.
   */
  public StaticShadow(CollisionBox collisionBox) {
    super(0, null, collisionBox.getX(), collisionBox.getY(), collisionBox.getWidth(), collisionBox.getHeight());
    this.setShadowType(StaticShadowType.NONE);
    this.origin = collisionBox;
    this.shadowOffset = DEFAULT_OFFSET;
  }

  public StaticShadowType getShadowType() {
    return this.shadowType;
  }

  public void setShadowType(final StaticShadowType shadowType) {
    this.shadowType = shadowType;
    this.area = null;
  }

  @Override
  public void setX(double x) {
    super.setX(x);
    this.area = null;
  }

  @Override
  public void setY(double y) {
    super.setY(y);
    this.area = null;
  }

  @Override
  public void setWidth(final double width) {
    super.setWidth(width);
    this.area = null;
  }

  @Override
  public void setHeight(final double height) {
    super.setHeight(height);
    this.area = null;
  }

  @Override
  public void setLocation(final Point2D location) {
    super.setLocation(location);
    this.area = null;
  }

  public CollisionBox getOrigin() {
    return this.origin;
  }

  @Override
  public String toString() {
    if (this.getOrigin() == null) {
      return super.toString();
    }

    return "[" + this.getOrigin().toString() + "] -> " + super.toString();
  }

  @Override
  public Rectangle2D getBoundingBox() {
    if (this.getArea() == null) {
      return super.getBoundingBox();
    }

    return this.getArea().getBounds2D();
  }

  public Area getArea() {
    if (this.getShadowType() == StaticShadowType.NONE) {
      return null;
    }

    if (this.area == null) {
      this.createArea();
    }
    return this.area;
  }

  private void createArea() {
    if (this.getShadowType() == StaticShadowType.NONE) {
      return;
    }

    final Path2D parallelogram = new Path2D.Double();
    final double right = this.getX() + this.getWidth();
    final double bottom = this.getY() + this.getHeight();

    parallelogram.moveTo(this.getX(), this.getY());
    parallelogram.lineTo(right, this.getY());
    if (shadowType.equals(StaticShadowType.DOWN)) {
      parallelogram.lineTo(right, bottom + this.getOffset());
      parallelogram.lineTo(this.getX(), bottom + this.getOffset());
    } else if (shadowType.equals(StaticShadowType.DOWNLEFT)) {
      parallelogram.lineTo(right, bottom);
      parallelogram.lineTo(right - this.getOffset() / 2.0, bottom + this.getOffset());
      parallelogram.lineTo(this.getX(), bottom + this.getOffset());
    } else if (shadowType.equals(StaticShadowType.DOWNRIGHT)) {
      parallelogram.lineTo(right, bottom);
      parallelogram.lineTo(right + this.getOffset() / 2.0, bottom + this.getOffset());
      parallelogram.lineTo(this.getX(), bottom + this.getOffset());
    } else if (shadowType.equals(StaticShadowType.LEFT)) {
      parallelogram.lineTo(right, bottom);
      parallelogram.lineTo(right - this.getOffset() / 2.0, bottom + this.getOffset());
      parallelogram.lineTo(this.getX() - this.getOffset() / 2.0, bottom + this.getOffset());
      parallelogram.lineTo(this.getX(), bottom);
    } else if (shadowType.equals(StaticShadowType.LEFTDOWN)) {
      parallelogram.lineTo(right, bottom);
      parallelogram.lineTo(right, bottom + this.getOffset());
      parallelogram.lineTo(this.getX() - this.getOffset() / 2.0, bottom + this.getOffset());
      parallelogram.lineTo(this.getX(), bottom);
    } else if (shadowType.equals(StaticShadowType.LEFTRIGHT)) {
      parallelogram.lineTo(right, bottom);
      parallelogram.lineTo(right + this.getOffset() / 2.0, bottom + this.getOffset());
      parallelogram.lineTo(this.getX() - this.getOffset() / 2.0, bottom + this.getOffset());
      parallelogram.lineTo(this.getX(), bottom);
    } else if (shadowType.equals(StaticShadowType.RIGHTLEFT)) {
      parallelogram.lineTo(right, bottom);
      parallelogram.lineTo(right - this.getOffset() / 2.0, bottom + this.getOffset());
      parallelogram.lineTo(this.getX() + this.getOffset() / 2.0, bottom + this.getOffset());
      parallelogram.lineTo(this.getX(), bottom);
    } else if (shadowType.equals(StaticShadowType.RIGHT)) {
      parallelogram.lineTo(right, bottom);
      parallelogram.lineTo(right + this.getOffset() / 2.0, bottom + this.getOffset());
      parallelogram.lineTo(this.getX() + this.getOffset() / 2.0, bottom + this.getOffset());
      parallelogram.lineTo(this.getX(), bottom);
    } else if (shadowType.equals(StaticShadowType.RIGHTDOWN)) {
      parallelogram.lineTo(right, bottom);
      parallelogram.lineTo(right, bottom + this.getOffset());
      parallelogram.lineTo(this.getX() + this.getOffset() / 2.0, bottom + this.getOffset());
      parallelogram.lineTo(this.getX(), bottom);
    } else if (shadowType.equals(StaticShadowType.NOOFFSET)) {
      parallelogram.lineTo(right, bottom);
      parallelogram.lineTo(this.getX(), bottom);
    }

    parallelogram.closePath();
    this.area = new Area(parallelogram);
  }

  public int getOffset() {
    return shadowOffset;
  }

  public void setOffset(int shadowOffset) {
    this.shadowOffset = shadowOffset;
    this.area = null;
  }
}