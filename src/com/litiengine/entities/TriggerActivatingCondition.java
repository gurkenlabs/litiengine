package com.litiengine.entities;

import java.util.EventListener;

/**
 * This listener provides a callback that allows to check conditions for activating a {@code Trigger} and prevent the activation if necessary.
 */
@FunctionalInterface
public interface TriggerActivatingCondition extends EventListener {

  /**
   * Allows to register functions that contain additional checks for the trigger
   * activation. The return value of the function is considered the reason why
   * the trigger cannot be activated. If the function returns anything else than
   * null, the activation is cancelled and the result of the function is send to
   * the activator entity.
   * 
   * @param event
   *          The event data that contains information about the trigger.
   * @return The reason why the trigger cannot be activated or null if it can be activated.
   */
  String canActivate(TriggerEvent event);
}
