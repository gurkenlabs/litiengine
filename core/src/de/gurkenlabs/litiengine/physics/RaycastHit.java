package de.gurkenlabs.litiengine.physics;

import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import java.awt.geom.Point2D;

public class RaycastHit {
  private final Point2D point;
  private final ICollisionEntity entity;
  private final double distance;

  public RaycastHit(Point2D point, ICollisionEntity entity, double distance) {
    this.point = point;
    this.entity = entity;
    this.distance = distance;
  }

  public Point2D getPoint() {
    return this.point;
  }

  public ICollisionEntity getEntity() {
    return this.entity;
  }

  public double getDistance() {
    return this.distance;
  }
}
