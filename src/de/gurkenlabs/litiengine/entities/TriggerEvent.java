package de.gurkenlabs.litiengine.entities;

public class TriggerEvent {
  private String message;
  private IEntity entity;
  private int target;

  public String getMessage() {
    return this.message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getTarget() {
    return this.target;
  }

  public void setTarget(int target) {
    this.target = target;
  }

  public TriggerEvent(String message, IEntity entity,  int target) {
    super();
    this.message = message;
    this.target = target;
    this.entity = entity;
  }

  public IEntity getEntity() {
    return entity;
  }

  public void setEntity(IEntity entity) {
    this.entity = entity;
  }

}
