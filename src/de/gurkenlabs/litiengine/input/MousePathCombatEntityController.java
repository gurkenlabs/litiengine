package de.gurkenlabs.litiengine.input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.litiengine.physics.IEntityNavigator;

public class MousePathCombatEntityController extends ClientEntityMovementController implements MouseListener {
  private final IEntityNavigator navigator;

  private final IMovableCombatEntity entity;
  /** The player is navigating. */
  private boolean navigating;

  public MousePathCombatEntityController(final IEntityNavigator navigator, final IMovableCombatEntity movableEntity) {
    super(movableEntity);
    this.navigator = navigator;
    this.entity = movableEntity;
    Input.MOUSE.registerMouseListener(this);
  }

  public IEntityNavigator getNavigator() {
    return this.navigator;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      this.getNavigator().rotateTowards(Input.MOUSE.getMapLocation());
      this.navigator.stop();
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      this.navigating = true;
    }
    
    if (SwingUtilities.isLeftMouseButton(e)) {
      this.getNavigator().rotateTowards(Input.MOUSE.getMapLocation());
      this.navigator.stop();
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      this.navigating = false;
    }
  }

  @Override
  public void update(final IGameLoop gameLoop) {
    super.update(gameLoop);

    // can only walk if no forces are active
    if (!this.isMovementAllowed() || this.getActiceForces().size() > 0) {
      this.navigator.stop();
      return;
    }

    if (this.navigating && !this.entity.isDead()) {
      this.navigator.navigate(Input.MOUSE.getMapLocation());
    }
  }
}
