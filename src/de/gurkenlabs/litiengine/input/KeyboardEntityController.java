package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.physics.AccelerationMovementController;

public class KeyboardEntityController<T extends IMovableEntity> extends AccelerationMovementController<T> implements IKeyObserver {
  private final List<Integer> up;
  private final List<Integer> down;
  private final List<Integer> left;
  private final List<Integer> right;

  public KeyboardEntityController(final T entity) {
    this(entity, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D);
  }

  public KeyboardEntityController(final T entity, final int up, final int down, final int left, final int right) {
    super(entity);
    this.up = new ArrayList<>();
    this.down = new ArrayList<>();
    this.left = new ArrayList<>();
    this.right = new ArrayList<>();

    this.up.add(up);
    this.down.add(down);
    this.left.add(left);
    this.right.add(right);
    Input.keyboard().registerForKeyEvents(this);
  }

  @Override
  public void handlePressedKey(final KeyEvent keyCode) {

    if (this.up.contains(keyCode.getKeyCode())) {
      this.setDy(this.getDy() - 1);
      this.setMovedY(true);
    } else if (this.down.contains(keyCode.getKeyCode())) {
      this.setMovedY(true);
      this.setDy(this.getDy() + 1);
    } else if (this.left.contains(keyCode.getKeyCode())) {
      this.setDx(this.getDx() - 1);
      this.setMovedX(true);
    } else if (this.right.contains(keyCode.getKeyCode())) {
      this.setDx(this.getDx() + 1);
      this.setMovedX(true);
    }
  }

  public void addUpKey(int keyCode) {
    this.up.add(keyCode);
  }

  public void addDownKey(int keyCode) {
    this.down.add(keyCode);
  }

  public void addLeftKey(int keyCode) {
    this.left.add(keyCode);
  }

  public void addRightKey(int keyCode) {
    this.right.add(keyCode);
  }

  @Override
  public void handleReleasedKey(final KeyEvent keyCode) {

  }

  @Override
  public void handleTypedKey(final KeyEvent keyCode) {

  }
}
