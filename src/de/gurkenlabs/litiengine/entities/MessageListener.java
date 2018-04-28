package de.gurkenlabs.litiengine.entities;

import java.util.EventListener;

/**
 * This listener provides callbacks for when an <code>Entity</code> received a message.
 */
public interface MessageListener extends EventListener {

  /**
   * This method is called whenever a message is received by {@link IEntity#sendMessage(Object, String)}.
   * 
   * @param event
   *          The event data that contains information about the received message and sender.
   */
  public void messageReceived(MessageEvent event);
}
