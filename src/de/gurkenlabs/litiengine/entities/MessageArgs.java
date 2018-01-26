package de.gurkenlabs.litiengine.entities;

public class MessageArgs {
  private final IEntity entity;
  private final Object sender;
  private final String message;

  public MessageArgs(IEntity entity, Object sender, String message) {
    this.entity = entity;
    this.sender = sender;
    this.message = message;
  }

  public IEntity getEntity() {
    return this.entity;
  }

  public Object getSender() {
    return sender;
  }

  public String getMessage() {
    return this.message;
  }

}
