package de.gurkenlabs.litiengine.entities;

import java.util.List;
import java.util.Map;

public class TriggerEvent {
  private final Map<String, String> arguments;
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
    this.arguments = trigger.getArguments();
  }

  public String getArgument(final String name) {
    if (this.arguments.containsKey(name)) {
      return this.arguments.get(name);
    }

    return null;
  }

  public Map<String, String> getArguments() {
    return this.arguments;
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
