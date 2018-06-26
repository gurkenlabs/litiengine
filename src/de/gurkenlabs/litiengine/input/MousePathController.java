package de.gurkenlabs.litiengine.input;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.pathfinding.IEntityNavigator;
import de.gurkenlabs.litiengine.physics.MovementController;

public class MousePathController extends MovementController<IMobileEntity> {
  /** The player is navigating. */
  private boolean navigating;

  private final IEntityNavigator navigator;

  public MousePathController(final IEntityNavigator navigator, final IMobileEntity entity) {
    super(entity);
    this.navigator = navigator;
    Input.mouse().onPressed(this::mousePressed);
    Input.mouse().onReleased(this::mouseReleased);
  }

  public IEntityNavigator getNavigator() {
    return this.navigator;
  }

  public void mousePressed(final MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      this.navigating = true;
    }
  }

  public void mouseReleased(final MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      this.navigating = false;
    }
  }

  @Override
  public void update() {
    super.update();
    // can only walk if no forces are active
    if (!this.isMovementAllowed() || !this.getActiveForces().isEmpty()) {
      this.navigator.stop();
      return;
    }

    if (this.navigating) {
      this.navigator.navigate(Input.mouse().getMapLocation());
    }
  }
}
