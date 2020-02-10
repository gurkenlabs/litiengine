package de.gurkenlabs.litiengine.entities;

public class EntityAction {
  private final String name;
  private final Runnable action;

  private String description;

  EntityAction(String name, Runnable action) {
    this.name = name;
    this.action = action;
  }

  public String getDescription() {
    return this.description;
  }

  public String getName() {
    return this.name;
  }

  public void perform() {
    this.action.run();
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
