package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.physics.MovementController;
import de.gurkenlabs.litiengine.util.ListUtilities;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class KeyboardEntityController<T extends IMobileEntity> extends MovementController<T> {
  private final List<Integer> up;
  private final List<Integer> down;
  private final List<Integer> left;
  private final List<Integer> right;

  public KeyboardEntityController(final T entity) {
    this(entity, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D);
  }

  public KeyboardEntityController(
      final T entity, final int up, final int down, final int left, final int right) {
    super(entity);
    this.up = new ArrayList<>();
    this.down = new ArrayList<>();
    this.left = new ArrayList<>();
    this.right = new ArrayList<>();

    this.up.add(up);
    this.down.add(down);
    this.left.add(left);
    this.right.add(right);
    Input.keyboard().onKeyPressed(this::handlePressedKey);
  }

  public void handlePressedKey(final KeyEvent keyCode) {
    if (this.up.contains(keyCode.getKeyCode())) {
      this.setDy(this.getDy() - 1);
    } else if (this.down.contains(keyCode.getKeyCode())) {
      this.setDy(this.getDy() + 1);
    } else if (this.left.contains(keyCode.getKeyCode())) {
      this.setDx(this.getDx() - 1);
    } else if (this.right.contains(keyCode.getKeyCode())) {
      this.setDx(this.getDx() + 1);
    }
  }

  public void addUpKey(int keyCode) {
    if (this.up.contains(keyCode)) {
      return;
    }

    this.up.add(keyCode);
  }

  public void addDownKey(int keyCode) {
    if (this.down.contains(keyCode)) {
      return;
    }

    this.down.add(keyCode);
  }

  public void addLeftKey(int keyCode) {
    if (this.left.contains(keyCode)) {
      return;
    }

    this.left.add(keyCode);
  }

  public void addRightKey(int keyCode) {
    if (this.right.contains(keyCode)) {
      return;
    }

    this.right.add(keyCode);
  }

  public List<Integer> getUpKeys() {
    return this.up;
  }

  public List<Integer> getDownKeys() {
    return this.down;
  }

  public List<Integer> getLeftKeys() {
    return this.left;
  }

  public List<Integer> getRightKeys() {
    return this.right;
  }

  public void setUpKeys(int... up) {
    this.setUpKeys(ListUtilities.getIntList(up));
  }

  public void setUpKeys(List<Integer> up) {
    set(this.up, up);
  }

  public void setDownKeys(int... down) {
    this.setDownKeys(ListUtilities.getIntList(down));
  }

  public void setDownKeys(List<Integer> down) {
    set(this.down, down);
  }

  public void setLeftKeys(int... left) {
    this.setLeftKeys(ListUtilities.getIntList(left));
  }

  public void setLeftKeys(List<Integer> left) {
    set(this.left, left);
  }

  public void setRightKeys(int... right) {
    this.setRightKeys(ListUtilities.getIntList(right));
  }

  public void setRightKeys(List<Integer> right) {
    set(this.right, right);
  }

  private static void set(List<Integer> keyList, List<Integer> keys) {
    keyList.clear();
    for (int key : keys) {
      keyList.add(key);
    }
  }
}
