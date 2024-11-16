package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.entities.behavior.EntityNavigator;
import de.gurkenlabs.litiengine.physics.MovementController;

/**
 * A controller that allows an entity to be controlled via mouse path input.
 */
public class MousePathController extends MovementController<IMobileEntity> {
  /** Indicates whether the player is navigating. */
  private boolean navigating;

  /** The navigator used to control the entity's movement. */
  private final EntityNavigator navigator;

  /**
   * Constructs a new MousePathController for the specified entity and navigator.
   *
   * @param navigator the navigator to control the entity's movement
   * @param entity the entity to be controlled by the mouse path
   */
  public MousePathController(final EntityNavigator navigator, final IMobileEntity entity) {
    super(entity);
    this.navigator = navigator;
  }

  /**
   * Gets the navigator used to control the entity's movement.
   *
   * @return the navigator
   */
  public EntityNavigator getNavigator() {
    return this.navigator;
  }

  /**
   * Updates the controller, handling the entity's movement based on mouse input.
   */
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
