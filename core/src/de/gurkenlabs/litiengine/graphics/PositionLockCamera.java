package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.entities.IEntity;
import java.awt.geom.Point2D;

/** The Class LocalPlayerCamera. */
public class PositionLockCamera extends Camera {
  private final IEntity entity;

  /**
   * Initializes a new instance of the {@code PositionLockCamera}.
   *
   * @param entity The entity to which the focus will be locked.
   */
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

    this.setFocus(cameraLocation);
    super.updateFocus();
  }

  protected Point2D getLockedCameraLocation() {
    return this.getLockedEntity().getCenter();
  }
}
