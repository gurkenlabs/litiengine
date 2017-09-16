package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.IEntity;

/**
 * The Class LocalPlayerCamera.
 */
public class PositionLockCamera extends Camera {
  private final IEntity entity;

  public PositionLockCamera(final IEntity entity) {
    super();
    this.entity = entity;
    this.updateFocus();
  }

  public IEntity getLockedEntity() {
    return this.entity;
  }

  @Override
  public void updateFocus() {
    final Point2D cameraLocation = this.getLockedCameraLocation();

    this.setFocus(new Point2D.Double(cameraLocation.getX(), cameraLocation.getY()));
    super.updateFocus();
  }

  protected Point2D getLockedCameraLocation() {
    return this.getLockedEntity().getDimensionCenter();
  }
}
