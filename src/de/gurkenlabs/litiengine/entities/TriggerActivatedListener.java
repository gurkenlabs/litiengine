package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

/**
 * This listener provides a callback for when a <code>Trigger</code> was activated.
 */
public interface TriggerActivatedListener extends EventListener {
  
  /**
   * This method is called when a <code>Trigger</code> was activated.
   * 
   * @param event
   *          The event data that contains information about the trigger.
   *          
   * @see Trigger#isActivated()
   */
  public void activated(TriggerEvent event);
}
