package de.gurkenlabs.litiengine.entities;

import java.util.EventObject;
import java.util.List;

public class TriggerEvent extends EventObject implements IEntityProvider {
  private static final long serialVersionUID = 3624707673365488289L;

  private final transient IEntity entity;
  private final transient String message;
  private final transient List<Integer> targets;
  private final transient Trigger trigger;

  public TriggerEvent(final Trigger trigger, final IEntity entity, final List<Integer> targets) {
    super(trigger);
    this.trigger = trigger;
    this.message = trigger.getMessage();
    this.targets = targets;
    this.entity = entity;
  }
  
  /**
   * Get the entity that activated the Trigger.
   */
  @Override
  public IEntity getEntity() {
    return this.entity;
  }
  /**
   * Get this Trigger's message.
   */
  public String getMessage() {
    return this.message;
  }
  /**
   * Get the entities that are affected by the Trigger.
   */
  public List<Integer> getTargets() {
    return this.targets;
  }
  /**
   * Get the Trigger affected by this event.
   */
  public Trigger getTrigger() {
    return trigger;
  }
}
