package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;

import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.graphics.RenderType;

@EntityInfo(renderType = RenderType.OVERLAY)
public class StaticShadow extends MapArea {
  public enum StaticShadowType {
    DOWN, DOWNLEFT, DOWNRIGHT, LEFT, LEFTDOWN, LEFTRIGHT, NONE, NOOFFSET, RIGHT, RIGHTDOWN, RIGHTLEFT;

    public static StaticShadowType get(final String mapObjectType) {
      if (mapObjectType == null || mapObjectType.isEmpty()) {
        return StaticShadowType.NOOFFSET;
      }

      try {
        return StaticShadowType.valueOf(mapObjectType);
      } catch (final IllegalArgumentException iae) {
        return StaticShadowType.NOOFFSET;
      }
    }
  }

  public static final int DEFAULT_ALPHA = 75;
  public static final Color DEFAULT_COLOR = Color.BLACK;
  public static final int DEFAULT_OFFSET = 10;

  private StaticShadowType shadowType;
  private int shadowOffset;
  private final CollisionBox origin;

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

  public int getOffset() {
    return shadowOffset;
  }

  public void setOffset(int shadowOffset) {
    this.shadowOffset = shadowOffset;
  }
}