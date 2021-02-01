package com.litiengine.input;

import com.litiengine.entities.IMobileEntity;
import com.litiengine.entities.behavior.EntityNavigator;
import com.litiengine.physics.MovementController;

public class MousePathController extends MovementController<IMobileEntity> {
  /** The player is navigating. */
  private boolean navigating;

  private final EntityNavigator navigator;

  public MousePathController(final EntityNavigator navigator, final IMobileEntity entity) {
    super(entity);
    this.navigator = navigator;
  }

  public EntityNavigator getNavigator() {
    return this.navigator;
  }
  @Override
  public void update() {
    super.update();
    // can only walk if no forces are active
    if (!this.isMovementAllowed() || !this.getActiveForces().isEmpty()) {
      this.navigator.stop();
      return;
    }

    this.navigating = Input.mouse().isRightButtonPressed();
    if (this.navigating) {
      this.navigator.navigate(Input.mouse().getMapLocation());
    }
  }
}
