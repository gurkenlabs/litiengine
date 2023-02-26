package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.entities.IEntity;
import java.awt.geom.Point2D;

/** The Class LocalPlayerCamera. */
public class LocationLockCamera extends Camera {
  private final IEntity entity;

  /**
   * Initializes a new instance of the {@code LocationLockCamera}.
   *
   * @param entity
   *          The entity to which the focus will be locked.
   */
  public LocationLockCamera(final IEntity entity) {
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

    this.setFocus(cameraLocation);
    super.updateFocus();
  }

  protected Point2D getLockedCameraLocation() {
    return this.getLockedEntity().getCenter();
  }
}
