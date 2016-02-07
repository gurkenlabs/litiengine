package de.gurkenlabs.litiengine.input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.litiengine.physics.IEntityNavigator;

public class MousePathCombatEntityController extends MovementController implements IUpdateable, MouseListener {
  private final IEntityNavigator navigator;

  private final IMovableCombatEntity entity;
  /** The player is navigating. */
  private boolean navigating;

  public MousePathCombatEntityController(final IEntityNavigator navigator, final IMovableCombatEntity movableEntity) {
    super(movableEntity);
    this.navigator = navigator;
    this.entity = movableEntity;
    Input.MOUSE.registerMouseListener(this);
    Game.getLoop().registerForUpdate(this);
  }

  public IEntityNavigator getNavigator() {
    return this.navigator;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
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
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      this.navigating = false;
    }
  }

  @Override
  public void update() {
    if (this.navigating && !this.entity.isDead()) {
      this.navigator.navigate(Input.MOUSE.getMapLocation());
    }
  }
}
