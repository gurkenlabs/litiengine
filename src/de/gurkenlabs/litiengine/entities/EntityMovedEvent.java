package de.gurkenlabs.litiengine.entities;

import java.util.EventObject;

public class EntityMovedEvent extends EventObject {
  private static final long serialVersionUID = 2931711179495514204L;
  private final transient IMobileEntity entity;
  private final double deltaX;
  private final double deltaY;
  private final double distance;

  public EntityMovedEvent(IMobileEntity entity, double deltaX, double deltaY) {
    super(entity);
    this.entity = entity;
    this.deltaX = deltaX;
    this.deltaY = deltaY;
    this.distance = Math.sqrt(Math.pow(this.deltaX, 2) + Math.pow(this.deltaY, 2));
  }

  public IMobileEntity getEntity() {
    return this.entity;
  }

  public double getDeltaX() {
    return this.deltaX;
  }

  public double getDeltaY() { return this.deltaY; }

  public double getDistance() { return this.distance; }
}
