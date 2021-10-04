package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.litiengine.entities.Action;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.util.ListUtilities;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A movement controller that supports keyboard input for horizontal entity movement.
 *
 * @param <T> The type of the controlled entity.
 */
public class PlatformingMovementController<T extends IMobileEntity>
    extends KeyboardEntityController<T> {
  /**
   * The identifier that is used by this controller to execute the jumping {@code EntityAction} on
   * the related entity.
   *
   * <p><i>Note that the entity needs to either specify a method with an {@code Action} annotation
   * that corresponds to this identifier or it needs to explicitly register an {@code
   * EntityAction}.</i>
   *
   * @see IEntity#register(String, Runnable)
   * @see Action
   */
  public static final String JUMP_ACTION = "jump";

  /** The list of jump keys, represented by their integer values. */
  private final List<Integer> jump;

  /**
   * Instantiates a new platforming movement controller.
   *
   * @param entity the entity
   */
  public PlatformingMovementController(final T entity) {
    this(entity, KeyEvent.VK_SPACE);
  }

  /**
   * Instantiates a new platforming movement controller.
   *
   * @param entity the entity
   * @param jump the jump
   */
  public PlatformingMovementController(T entity, final int jump) {
    super(entity);
    this.getUpKeys().clear();
    this.getDownKeys().clear();
    this.jump = new ArrayList<>();
    this.addJumpKey(jump);
    Input.keyboard().onKeyPressed(this::handlePressedKey);
  }

  @Override
  public void handlePressedKey(KeyEvent keyCode) {
    super.handlePressedKey(keyCode);
    if (this.jump.contains(keyCode.getKeyCode())) {
      this.getEntity().perform(JUMP_ACTION);
    }
  }

  /**
   * Adds a jump key.
   *
   * @param keyCode the key code for the newly added jump key
   */
  public void addJumpKey(int keyCode) {
    if (this.jump.contains(keyCode)) {
      return;
    }

    this.jump.add(keyCode);
  }

  /**
   * Gets the list of jump key codes in this controller.
   *
   * @return the jump keys
   */
  public List<Integer> getJumpKeys() {
    return this.jump;
  }

  /**
   * Initializes the jump keys with a given array of key codes.
   *
   * @param jump the new jump keys
   */
  public void setJumpKeys(int... jump) {
    this.setUpKeys(ListUtilities.getIntList(jump));
  }
}
