package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TmxProperty;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.StaticShadowType;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.awt.geom.Point2D;

@EntityInfo(renderType = RenderType.OVERLAY)
public class StaticShadow extends MapArea {
  public static final Color DEFAULT_COLOR = new Color(0, 0, 0, 75);
  public static final int DEFAULT_OFFSET = 10;

  @TmxProperty(name = MapObjectProperty.SHADOW_TYPE) private StaticShadowType shadowType;

  @TmxProperty(name = MapObjectProperty.SHADOW_OFFSET) private int shadowOffset;

  private final CollisionBox origin;
  private Area area;

  /**
   * Instantiates a new {@code StaticShadow} entity.
   *
   * @param shadowType The type of the static shadow.
   * @param offset     The offset for the shadow.
   */
  public StaticShadow(StaticShadowType shadowType, int offset) {
    this.shadowType = shadowType;
    this.shadowOffset = offset;
    this.origin = null;
  }

  /**
   * Instantiates a new {@code StaticShadow} entity.
   *
   * @param shadowType The type of the static shadow.
   */
  public StaticShadow(StaticShadowType shadowType) {
    this.shadowType = shadowType;
    this.shadowOffset = DEFAULT_OFFSET;
    this.origin = null;
  }

  /**
   * Instantiates a new {@code StaticShadow} entity.
   *
   * @param x          The x-coordinate of this instance.
   * @param y          The y-coordinate of this instance.
   * @param width      The width of this instance.
   * @param height     The height of this instance.
   * @param shadowType The type of the static shadow.
   */
  public StaticShadow(double x, double y, float width, float height, StaticShadowType shadowType) {
    this(0, null, x, y, width, height, shadowType);
  }

  /**
   * Instantiates a new {@code StaticShadow} entity.
   *
   * @param id         The id of this entity.
   * @param x          The x-coordinate of this instance.
   * @param y          The y-coordinate of this instance.
   * @param width      The width of this instance.
   * @param height     The height of this instance.
   * @param shadowType The type of the static shadow.
   */
  public StaticShadow(int id, double x, double y, float width, float height, StaticShadowType shadowType) {
    this(id, null, x, y, width, height, shadowType);
  }

  /**
   * Instantiates a new {@code StaticShadow} entity.
   *
   * @param id         The id of this entity.
   * @param name       The name of this entity.
   * @param x          The x-coordinate of this instance.
   * @param y          The y-coordinate of this instance.
   * @param width      The width of this instance.
   * @param height     The height of this instance.
   * @param shadowType The type of the static shadow.
   */
  public StaticShadow(int id, String name, double x, double y, float width, float height, StaticShadowType shadowType) {
    super(id, name, x, y, width, height);
    this.setShadowType(shadowType);
    this.origin = null;
    this.shadowOffset = DEFAULT_OFFSET;
  }

  /**
   * Instantiates a new {@code StaticShadow} entity.
   *
   * @param collisionBox The collision box from which this shadow instance originates from.
   */
  public StaticShadow(CollisionBox collisionBox) {
    super(0, null, collisionBox.getX(), collisionBox.getY(), collisionBox.getWidth(), collisionBox.getHeight());
    this.setShadowType(StaticShadowType.NONE);
    this.origin = collisionBox;
    this.shadowOffset = DEFAULT_OFFSET;
  }

  /**
   * Gets the type of the static shadow.
   *
   * @return the current shadow type.
   */
  public StaticShadowType getShadowType() {
    return this.shadowType;
  }

  /**
   * Sets the type of the static shadow and invalidates the current area.
   *
   * @param shadowType the new shadow type to set.
   */
  public void setShadowType(final StaticShadowType shadowType) {
    this.shadowType = shadowType;
    this.area = null;
  }

  @Override public void setX(double x) {
    super.setX(x);
    this.area = null;
  }

  @Override public void setY(double y) {
    super.setY(y);
    this.area = null;
  }

  @Override public void setWidth(final double width) {
    super.setWidth(width);
    this.area = null;
  }

  @Override public void setHeight(final double height) {
    super.setHeight(height);
    this.area = null;
  }

  @Override public void setLocation(final Point2D location) {
    super.setLocation(location);
    this.area = null;
  }

  /**
   * Gets the origin collision box of this static shadow.
   *
   * @return the origin collision box, or null if there is no origin.
   */
  public CollisionBox getOrigin() {
    return this.origin;
  }

  /**
   * Returns a string representation of this static shadow. If the origin is not null, the string representation includes the origin's string
   * representation.
   *
   * @return a string representation of this static shadow.
   */
  @Override public String toString() {
    if (this.getOrigin() == null) {
      return super.toString();
    }

    return "[" + this.getOrigin().toString() + "] -> " + super.toString();
  }

  /**
   * Gets the area of the static shadow. If the shadow type is NONE, returns null. If the area is not already created, it calls the
   * {@link StaticShadow#createArea()} method to generate it.
   *
   * @return the area of the static shadow, or null if the shadow type is NONE.
   */
  public Area getArea() {
    if (getShadowType() == StaticShadowType.NONE) {
      return null;
    }

    if (area == null) {
      createArea();
    }
    return area;
  }

  /**
   * Creates the area for the static shadow based on its type and dimensions. This method constructs a parallelogram shape and sets it as the area.
   */
  private void createArea() {
    if (getShadowType() == StaticShadowType.NONE) {
      return;
    }

    final Path2D parallelogram = new Double();
    final double right = getX() + getWidth();
    final double bottom = getY() + getHeight();
    final double halfOffset = getOffset() / 2.0;

    parallelogram.moveTo(this.getX(), this.getY());
    parallelogram.lineTo(right, this.getY());
    switch (shadowType) {
      case DOWN -> {
        parallelogram.lineTo(right, bottom + getOffset());
        parallelogram.lineTo(getX(), bottom + getOffset());
      }
      case DOWNLEFT -> {
        parallelogram.lineTo(right, bottom);
        parallelogram.lineTo(right - halfOffset, bottom + getOffset());
        parallelogram.lineTo(getX(), bottom + getOffset());
      }
      case DOWNRIGHT -> {
        parallelogram.lineTo(right, bottom);
        parallelogram.lineTo(right + halfOffset, bottom + getOffset());
        parallelogram.lineTo(getX(), bottom + getOffset());
      }
      case LEFT -> {
        parallelogram.lineTo(right, bottom);
        parallelogram.lineTo(right - halfOffset, bottom + getOffset());
        parallelogram.lineTo(getX() - halfOffset, bottom + getOffset());
        parallelogram.lineTo(getX(), bottom);
      }
      case LEFTDOWN -> {
        parallelogram.lineTo(right, bottom);
        parallelogram.lineTo(right, bottom + getOffset());
        parallelogram.lineTo(getX() - halfOffset, bottom + getOffset());
        parallelogram.lineTo(getX(), bottom);
      }
      case LEFTRIGHT -> {
        parallelogram.lineTo(right, bottom);
        parallelogram.lineTo(right + halfOffset, bottom + getOffset());
        parallelogram.lineTo(getX() - halfOffset, bottom + getOffset());
        parallelogram.lineTo(getX(), bottom);
      }
      case NONE -> {
        //        do nothing if no shadow type is set
      }
      case NOOFFSET -> {
        parallelogram.lineTo(right, bottom);
        parallelogram.lineTo(getX(), bottom);
      }
      case RIGHT -> {
        parallelogram.lineTo(right, bottom);
        parallelogram.lineTo(right + halfOffset, bottom + getOffset());
        parallelogram.lineTo(getX() + halfOffset, bottom + getOffset());
        parallelogram.lineTo(getX(), bottom);
      }
      case RIGHTDOWN -> {
        parallelogram.lineTo(right, bottom);
        parallelogram.lineTo(right, bottom + getOffset());
        parallelogram.lineTo(getX() + halfOffset, bottom + getOffset());
        parallelogram.lineTo(getX(), bottom);
      }
      case RIGHTLEFT -> {
        parallelogram.lineTo(right, bottom);
        parallelogram.lineTo(right - halfOffset, bottom + getOffset());
        parallelogram.lineTo(getX() + halfOffset, bottom + getOffset());
        parallelogram.lineTo(getX(), bottom);
      }
      default -> throw new IllegalStateException("Unexpected shadow type: " + shadowType);
    }

    parallelogram.closePath();
    this.area = new Area(parallelogram);
  }

  /**
   * Gets the offset for the shadow.
   *
   * @return the shadow offset.
   */
  public int getOffset() {
    return shadowOffset;
  }

  /**
   * Sets the offset for the shadow and invalidates the current area.
   *
   * @param shadowOffset the new shadow offset to set.
   */
  public void setOffset(int shadowOffset) {
    this.shadowOffset = shadowOffset;
    this.area = null;
  }
}
