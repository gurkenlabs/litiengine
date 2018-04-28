package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

public interface TriggerActivatingCondition extends EventListener {
  
  /**
   * Allows to register functions that contain additional checks for the trigger
   * activation. The return value of the function is considered the reason why
   * the trigger cannot be activated. If the function returns anything else than
   * null, the activation is cancelled and the result of the function is send to
   * the activator entity.
   *
   */
  public String canActivate(TriggerEvent event);
}
