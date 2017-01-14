package de.gurkenlabs.litiengine.entities;

import java.util.Map;

public class TriggerEvent {
  private final String message;
  private final IEntity entity;
  private final int target;
  private final Map<String, String> arguments;

  public TriggerEvent(String message, IEntity entity, int target, Map<String, String> arguments) {
    super();
    this.message = message;
    this.target = target;
    this.entity = entity;
    this.arguments = arguments;
  }

  public String getMessage() {
    return this.message;
  }

  public int getTarget() {
    return this.target;
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
