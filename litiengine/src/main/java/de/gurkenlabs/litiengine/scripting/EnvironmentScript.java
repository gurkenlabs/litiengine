package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;

/// Base class for scripts attached to one loaded environment.
public abstract class EnvironmentScript extends AbstractScript<Environment> {
  @Override protected final void attached() throws Exception { this.onLoaded(); }
  @Override protected final void detached() throws Exception { this.onUnloaded(); }

  /// Called after the environment has loaded and is the current world environment.
  protected void onLoaded() throws Exception {}

  /// Called when the environment binding is detached during unload.
  protected void onUnloaded() throws Exception {}

  /// Called when the attached environment is cleared while it remains active.
  protected void onCleared() throws Exception {}

  /// Called when an entity is added to the attached environment.
  protected void onEntityAdded(IEntity entity) throws Exception {}

  /// Called when an entity is removed from the attached environment.
  protected void onEntityRemoved(IEntity entity) throws Exception {}

  final void dispatchCleared() throws Exception {
    this.onCleared();
  }

  final void dispatchEntityAdded(IEntity entity) throws Exception {
    this.onEntityAdded(entity);
  }

  final void dispatchEntityRemoved(IEntity entity) throws Exception {
    this.onEntityRemoved(entity);
  }
}
