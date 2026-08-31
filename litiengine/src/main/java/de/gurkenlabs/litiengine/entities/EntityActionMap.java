package de.gurkenlabs.litiengine.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/// Stores the named [EntityAction] instances registered on an [Entity]. Provides registration, lookup and removal by name.
public final class EntityActionMap {
  private final Map<String, EntityAction> actions;

  EntityActionMap() {
    this.actions = new ConcurrentHashMap<>();
  }

  /// Registers a new [EntityAction] backed by the supplied runnable.
  ///
  /// @param name   the action name
  /// @param action the runnable executed when the action is triggered
  /// @return the registered action, or `null` if the parameters are invalid
  public EntityAction register(String name, Runnable action) {
    if (name == null || name.isEmpty() || action == null) {
      return null;
    }

    EntityAction entityAction = new EntityAction(name, action);
    this.actions.put(name, entityAction);
    return entityAction;
  }

  /// Registers the supplied [EntityAction].
  ///
  /// @param action the action to register; ignored if `null`
  public void register(EntityAction action) {
    if (action == null) {
      return;
    }

    this.actions.put(action.getName(), action);
  }

  /// Unregisters the supplied [EntityAction].
  ///
  /// @param action the action to unregister; ignored if `null`
  public void unregister(EntityAction action) {
    if (action == null) {
      return;
    }

    this.unregister(action.getName());
  }

  /// Unregisters the [EntityAction] with the supplied name.
  ///
  /// @param actionName the action name; ignored if blank
  public void unregister(String actionName) {
    if (actionName == null || actionName.isEmpty()) {
      return;
    }

    this.actions.remove(actionName);
  }

  /// Returns an unmodifiable view of all registered actions.
  ///
  /// @return the registered actions
  public Collection<EntityAction> getActions() {
    return Collections.unmodifiableCollection(this.actions.values());
  }

  /// Returns the action registered under the supplied name.
  ///
  /// @param actionName the action name
  /// @return the action, or `null` if not found
  public EntityAction get(String actionName) {
    return this.actions.getOrDefault(actionName, null);
  }

  /// Returns whether an action with the supplied name is registered.
  ///
  /// @param actionName the action name
  /// @return `true` if registered
  public boolean exists(String actionName) {
    return this.actions.containsKey(actionName);
  }
}
