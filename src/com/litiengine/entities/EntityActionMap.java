package com.litiengine.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EntityActionMap {
  private final Map<String, EntityAction> actions;

  EntityActionMap() {
    this.actions = new ConcurrentHashMap<>();
  }

  public EntityAction register(String name, Runnable action) {
    if (name == null || name.isEmpty() || action == null) {
      return null;
    }
    
    EntityAction entityAction = new EntityAction(name, action);
    this.actions.put(name, entityAction);
    return entityAction;
  }

  public void register(EntityAction action) {
    if (action == null) {
      return;
    }

    this.actions.put(action.getName(), action);
  }

  public void unregister(EntityAction action) {
    if (action == null) {
      return;
    }

    this.unregister(action.getName());
  }

  public void unregister(String actionName) {
    if (actionName == null || actionName.isEmpty()) {
      return;
    }

    this.actions.remove(actionName);
  }

  public Collection<EntityAction> getActions() {
    return Collections.unmodifiableCollection(this.actions.values());
  }

  public EntityAction get(String actionName) {
    return this.actions.getOrDefault(actionName, null);
  }

  public boolean exists(String actionName) {
    return this.actions.containsKey(actionName);
  }
}
