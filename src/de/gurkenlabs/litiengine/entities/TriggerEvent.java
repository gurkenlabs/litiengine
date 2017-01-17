package de.gurkenlabs.litiengine.entities;

import java.util.List;
import java.util.Map;

public class TriggerEvent {
  private final String message;
  private final IEntity entity;
  private final List<Integer> targets;
  private final Map<String, String> arguments;

  public TriggerEvent(String message, IEntity entity, List<Integer> targets, Map<String, String> arguments) {
    super();
    this.message = message;
    this.targets = targets;
    this.entity = entity;
    this.arguments = arguments;
  }

  public String getMessage() {
    return this.message;
  }

  public List<Integer> getTargets() {
    return this.targets;
  }

  public IEntity getEntity() {
    return entity;
  }

  public Map<String, String> getArguments() {
    return arguments;
  }

  public String getArgument(String name) {
    if (this.arguments.containsKey(name)) {
      return this.arguments.get(name);
    }
    
    return null;
  }
}
