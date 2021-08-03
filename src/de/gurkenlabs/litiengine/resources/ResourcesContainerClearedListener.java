package de.gurkenlabs.litiengine.resources;

import java.util.EventListener;

/**
 * This listener provides a callback for when a {@code ResourcesContainer} was cleared.
 *
 * @see ResourcesContainer
 */
public interface ResourcesContainerClearedListener extends EventListener {
  /**
   * This method gets called after the {@code ResourcesContainer.clear} method was executed.
   *
   * @see ResourcesContainer#clear()
   */
  void cleared();
}
