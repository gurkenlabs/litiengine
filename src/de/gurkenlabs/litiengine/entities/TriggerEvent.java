package de.gurkenlabs.litiengine.entities;

import java.util.List;
import java.util.Map;

public class TriggerEvent {
  private final Map<String, String> arguments;
  private final IEntity entity;
  private final String message;
  private final List<Integer> targets;

  public TriggerEvent(final String message, final IEntity entity, final List<Integer> targets, final Map<String, String> arguments) {
    super();
    this.message = message;
    this.targets = targets;
    this.entity = entity;
    this.arguments = arguments;
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
}
