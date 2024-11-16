package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.physics.MovementController;
import de.gurkenlabs.litiengine.util.ListUtilities;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A controller that allows an entity to be controlled via keyboard input.
 *
 * @param <T> the type of the mobile entity to be controlled
 */
public class KeyboardEntityController<T extends IMobileEntity> extends MovementController<T> {
  private final List<Integer> up;
  private final List<Integer> down;
  private final List<Integer> left;
  private final List<Integer> right;

  /**
   * Constructs a new KeyboardEntityController for the specified entity.
   *
   * @param entity the entity to be controlled by the keyboard
   */
  public KeyboardEntityController(final T entity) {
    this(entity, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D);
  }

  /**
   * Constructs a new KeyboardEntityController for the specified entity.
   *
   * @param entity the entity to be controlled by the keyboard
   * @param up     the key code for moving up
   * @param down   the key code for moving down
   * @param left   the key code for moving left
   * @param right  the key code for moving right
   */
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

  /**
   * Handles the key pressed event and updates the entity's movement direction based on the key code.
   *
   * @param keyCode the key event containing the key code that was pressed
   */
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

  /**
   * Adds a key code to the list of keys for moving up.
   *
   * @param keyCode the key code to add for moving up
   */
  public void addUpKey(int keyCode) {
    if (this.up.contains(keyCode)) {
      return;
    }

    this.up.add(keyCode);
  }

  /**
   * Adds a key code to the list of keys for moving down.
   *
   * @param keyCode the key code to add for moving down
   */
  public void addDownKey(int keyCode) {
    if (this.down.contains(keyCode)) {
      return;
    }

    this.down.add(keyCode);
  }

  /**
   * Adds a key code to the list of keys for moving left.
   *
   * @param keyCode the key code to add for moving left
   */
  public void addLeftKey(int keyCode) {
    if (this.left.contains(keyCode)) {
      return;
    }

    this.left.add(keyCode);
  }

  /**
   * Adds a key code to the list of keys for moving right.
   *
   * @param keyCode the key code to add for moving right
   */
  public void addRightKey(int keyCode) {
    if (this.right.contains(keyCode)) {
      return;
    }

    this.right.add(keyCode);
  }

  /**
   * Gets the list of key codes for moving up.
   *
   * @return the list of key codes for moving up
   */
  public List<Integer> getUpKeys() {
    return this.up;
  }

  /**
   * Gets the list of key codes for moving down.
   *
   * @return the list of key codes for moving down
   */
  public List<Integer> getDownKeys() {
    return this.down;
  }

  /**
   * Gets the list of key codes for moving left.
   *
   * @return the list of key codes for moving left
   */
  public List<Integer> getLeftKeys() {
    return this.left;
  }

  /**
   * Gets the list of key codes for moving right.
   *
   * @return the list of key codes for moving right
   */
  public List<Integer> getRightKeys() {
    return this.right;
  }

  /**
   * Sets the key codes for moving up.
   *
   * @param up the key codes for moving up
   */
  public void setUpKeys(int... up) {
    this.setUpKeys(ListUtilities.getIntList(up));
  }

  /**
   * Sets the key codes for moving up.
   *
   * @param up the list of key codes for moving up
   */
  public void setUpKeys(List<Integer> up) {
    set(this.up, up);
  }

  /**
   * Sets the key codes for moving down.
   *
   * @param down the key codes for moving down
   */
  public void setDownKeys(int... down) {
    this.setDownKeys(ListUtilities.getIntList(down));
  }

  /**
   * Sets the key codes for moving down.
   *
   * @param down the list of key codes for moving down
   */
  public void setDownKeys(List<Integer> down) {
    set(this.down, down);
  }

  /**
   * Sets the key codes for moving left.
   *
   * @param left the key codes for moving left
   */
  public void setLeftKeys(int... left) {
    this.setLeftKeys(ListUtilities.getIntList(left));
  }

  /**
   * Sets the key codes for moving left.
   *
   * @param left the list of key codes for moving left
   */
  public void setLeftKeys(List<Integer> left) {
    set(this.left, left);
  }

  /**
   * Sets the key codes for moving right.
   *
   * @param right the key codes for moving right
   */
  public void setRightKeys(int... right) {
    this.setRightKeys(ListUtilities.getIntList(right));
  }

  /**
   * Sets the key codes for moving right.
   *
   * @param right the list of key codes for moving right
   */
  public void setRightKeys(List<Integer> right) {
    set(this.right, right);
  }

  /**
   * Sets the key codes for a specified direction.
   *
   * @param keyList the list of key codes for the direction
   * @param keys    the new key codes for the direction
   */
  private static void set(List<Integer> keyList, List<Integer> keys) {
    keyList.clear();
    keyList.addAll(keys);
  }
}
