package de.gurkenlabs.litiengine.gui.screens;

import java.io.Serial;
import java.util.EventObject;

/**
 * This event is fired when the screen changes in the game.
 */
public class ScreenChangedEvent extends EventObject {
  @Serial private static final long serialVersionUID = 6145911214616836674L;
  private final transient Screen previous;
  private final transient Screen changed;

  /**
   * Constructs a new ScreenChangedEvent.
   *
   * @param changed  the new screen that has been changed to
   * @param previous the previous screen that was displayed
   */
  public ScreenChangedEvent(Screen changed, Screen previous) {
    super(changed);
    this.previous = previous;
    this.changed = changed;
  }

  /**
   * Gets the previous screen that was displayed.
   *
   * @return the previous screen
   */
  public Screen getPrevious() {
    return this.previous;
  }

  /**
   * Gets the new screen that has been changed to.
   *
   * @return the new screen
   */
  public Screen getChanged() {
    return this.changed;
  }
}
