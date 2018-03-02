package de.gurkenlabs.litiengine.entities;

import java.util.List;

public class TriggerEvent {
  private final IEntity entity;
  private final String message;
  private final List<Integer> targets;
  private final Trigger trigger;

  public TriggerEvent(final Trigger trigger, final IEntity entity, final List<Integer> targets) {
    super();
    this.trigger = trigger;
    this.message = trigger.getMessage();
    this.targets = targets;
    this.entity = entity;
  }

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
