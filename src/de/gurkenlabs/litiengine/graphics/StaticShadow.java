package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;

@EntityInfo(renderType = RenderType.OVERLAY)
public class StaticShadow extends MapArea {
  public static final int DEFAULT_ALPHA = 75;
  public static final Color DEFAULT_COLOR = Color.BLACK;
  public static final int DEFAULT_OFFSET = 10;

  private final CollisionBox origin;

  private StaticShadowType shadowType;
  private int shadowOffset;
  private Area area;

  public StaticShadow(StaticShadowType shadowType) {
    this.shadowType = shadowType;
    this.shadowOffset = DEFAULT_OFFSET;
    this.origin = null;
  }

  public StaticShadow(double x, double y, float width, float height, StaticShadowType shadowType) {
    this(0, null, x, y, width, height, shadowType);
  }

  public StaticShadow(int id, double x, double y, float width, float height, StaticShadowType shadowType) {
    this(id, null, x, y, width, height, shadowType);
  }

  public StaticShadow(int id, String name, double x, double y, float width, float height, StaticShadowType shadowType) {
    super(id, name, x, y, width, height);
    this.setShadowType(shadowType);
    this.origin = null;
    this.shadowOffset = DEFAULT_OFFSET;
  }

  public StaticShadow(CollisionBox box) {
    super(0, null, box.getLocation().getX(), box.getLocation().getY(), box.getWidth(), box.getHeight());
    this.setShadowType(StaticShadowType.NONE);
    this.origin = box;
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
  public void setWidth(final float width) {
    super.setWidth(width);
    this.area = null;
  }

  @Override
  public void setHeight(final float height) {
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
    if (shadowType.equals(StaticShadowType.DOWN)) {
      parallelogram.moveTo(this.getX(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX(), this.getY() + this.getHeight() + this.getOffset());
      parallelogram.closePath();
    } else if (shadowType.equals(StaticShadowType.DOWNLEFT)) {
      parallelogram.moveTo(this.getX(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY() + this.getHeight());
      parallelogram.lineTo(this.getX() + this.getWidth() - this.getOffset() / 2.0, this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX(), this.getY() + this.getHeight() + this.getOffset());
      parallelogram.closePath();
    } else if (shadowType.equals(StaticShadowType.DOWNRIGHT)) {
      parallelogram.moveTo(this.getX(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY() + this.getHeight());
      parallelogram.lineTo(this.getX() + this.getWidth() + this.getOffset() / 2.0, this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX(), this.getY() + this.getHeight() + this.getOffset());
      parallelogram.closePath();
    } else if (shadowType.equals(StaticShadowType.LEFT)) {
      parallelogram.moveTo(this.getX(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY() + this.getHeight());
      parallelogram.lineTo(this.getX() + this.getWidth() - this.getOffset() / 2.0, this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX() - this.getOffset() / 2.0, this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX(), this.getY() + this.getHeight());
      parallelogram.closePath();
    } else if (shadowType.equals(StaticShadowType.LEFTDOWN)) {
      parallelogram.moveTo(this.getX(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY() + this.getHeight());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX() - this.getOffset() / 2.0, this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX(), this.getY() + this.getHeight());
      parallelogram.closePath();
    } else if (shadowType.equals(StaticShadowType.LEFTRIGHT)) {
      parallelogram.moveTo(this.getX(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY() + this.getHeight());
      parallelogram.lineTo(this.getX() + this.getWidth() + this.getOffset() / 2.0, this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX() - this.getOffset() / 2.0, this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX(), this.getY() + this.getHeight());
      parallelogram.closePath();
    } else if (shadowType.equals(StaticShadowType.RIGHTLEFT)) {
      parallelogram.moveTo(this.getX(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY() + this.getHeight());
      parallelogram.lineTo(this.getX() + this.getWidth() - this.getOffset() / 2.0, this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX() + this.getOffset() / 2.0, this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX(), this.getY() + this.getHeight());
      parallelogram.closePath();
    } else if (shadowType.equals(StaticShadowType.RIGHT)) {
      parallelogram.moveTo(this.getX(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY() + this.getHeight());
      parallelogram.lineTo(this.getX() + this.getWidth() + this.getOffset() / 2.0, this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX() + this.getOffset() / 2.0, this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX(), this.getY() + this.getHeight());
      parallelogram.closePath();
    } else if (shadowType.equals(StaticShadowType.RIGHTDOWN)) {
      parallelogram.moveTo(this.getX(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY() + this.getHeight());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX() + this.getOffset() / 2.0, this.getY() + this.getHeight() + this.getOffset());
      parallelogram.lineTo(this.getX(), this.getY() + this.getHeight());
      parallelogram.closePath();
    } else if (shadowType.equals(StaticShadowType.NOOFFSET)) {
      parallelogram.moveTo(this.getX(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY());
      parallelogram.lineTo(this.getX() + this.getWidth(), this.getY() + this.getHeight());
      parallelogram.lineTo(this.getX(), this.getY() + this.getHeight());
      parallelogram.closePath();
    }

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