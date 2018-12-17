package de.gurkenlabs.litiengine.environment;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.ICamera;

public final class GameWorld {
  private final List<EnvironmentLoadedListener> loadedListeners = new CopyOnWriteArrayList<>();
  private final List<EnvironmentUnloadedListener> unloadedListeners = new CopyOnWriteArrayList<>();

  private IEnvironment environment;
  private ICamera camera;

  public void addLoadedListener(EnvironmentLoadedListener listener) {
    this.loadedListeners.add(listener);
  }

  public void removeLoadedListener(EnvironmentLoadedListener listener) {
    this.loadedListeners.remove(listener);
  }

  public void addUnloadedListener(EnvironmentUnloadedListener listener) {
    this.unloadedListeners.add(listener);
  }

  public void removeUnloadedListener(EnvironmentUnloadedListener listener) {
    this.unloadedListeners.remove(listener);
  }

  public ICamera camera() {
    return this.camera;
  }

  public IEnvironment environment() {
    return this.environment;
  }

  public void loadEnvironment(final IEnvironment env) {
    unloadEnvironment();

    environment = env;
    if (this.environment() != null) {
      this.environment().load();
    }

    for (final EnvironmentLoadedListener listener : loadedListeners) {
      listener.environmentLoaded(environment());
    }
  }

  public void unloadEnvironment() {
    if (this.environment() != null) {
      this.environment().unload();

      for (final EnvironmentUnloadedListener listener : unloadedListeners) {
        listener.environmentUnloaded(this.environment());
      }
    }
  }

  public void setCamera(final ICamera cam) {
    if (this.camera() != null) {
      Game.loop().detach(camera);
    }

    camera = cam;

    if (!Game.isInNoGUIMode()) {
      Game.loop().attach(cam);
      this.camera().updateFocus();
    }
  }
}
