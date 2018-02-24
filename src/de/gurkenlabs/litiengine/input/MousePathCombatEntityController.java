package de.gurkenlabs.litiengine.input;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.physics.IEntityNavigator;
import de.gurkenlabs.litiengine.physics.MovementController;

public class MousePathCombatEntityController extends MovementController<Creature> {
  /** The player is navigating. */
  private boolean navigating;

  private final IEntityNavigator navigator;

  public MousePathCombatEntityController(final IEntityNavigator navigator, final Creature entity) {
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
    if (!this.isMovementAllowed() || !this.getActiceForces().isEmpty()) {
      this.navigator.stop();
      return;
    }

    if (this.navigating && !this.getEntity().isDead()) {
      this.navigator.navigate(Input.mouse().getMapLocation());
    }
  }
}
