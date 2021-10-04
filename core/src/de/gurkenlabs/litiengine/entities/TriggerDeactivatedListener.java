package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

/** This listener provides a callback for when a {@code Trigger} was deactivated. */
@FunctionalInterface
public interface TriggerDeactivatedListener extends EventListener {

  /**
   * This method is called when a {@code Trigger} was deactivated.
   *
   * @param event The event data that contains information about the trigger.
   * @see Trigger#isActivated()
   */
  void deactivated(TriggerEvent event);
}
