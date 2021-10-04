package de.gurkenlabs.litiengine.entities;

import java.util.EventObject;

/**
 * This implementation is used for events that contain information about a received message.
 *
 * @see IEntity#sendMessage(Object, String)
 */
public class EntityMessageEvent extends EventObject {
  private static final long serialVersionUID = 5131621546037429725L;
  private final transient IEntity entity;
  private final String message;

  EntityMessageEvent(Object sender, IEntity entity, String message) {
    super(sender);
    this.entity = entity;
    this.message = message;
  }

  /**
   * Gets the entity that received the message.
   *
   * @return The entity that received the message.
   */
  public IEntity getEntity() {
    return this.entity;
  }

  /**
   * Gets the message that was received.
   *
   * @return The message.
   */
  public String getMessage() {
    return this.message;
  }
}
