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

  @Override
  public IEntity getEntity() {
    return this.entity;
  }

  public String getMessage() {
    return this.message;
  }

  public List<Integer> getTargets() {
    return this.targets;
  }

  public Trigger getTrigger() {
    return trigger;
  }
}
