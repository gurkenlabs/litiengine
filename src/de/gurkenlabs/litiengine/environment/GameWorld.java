package de.gurkenlabs.litiengine.environment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.resources.Resources;

public final class GameWorld {
  private final List<EnvironmentLoadedListener> loadedListeners = new CopyOnWriteArrayList<>();
  private final List<EnvironmentUnloadedListener> unloadedListeners = new CopyOnWriteArrayList<>();
  private final Map<String, Collection<EnvironmentListener>> environmentListeners = new ConcurrentHashMap<>();
  
  private final Map<String, IEnvironment> environments = new ConcurrentHashMap<>();

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

  public void addListener(String mapName, EnvironmentListener listener) {
    if (mapName == null || mapName.isEmpty()) {
      return;
    }

    String mapIdentifier = mapName.toLowerCase();
    if (!this.environmentListeners.containsKey(mapIdentifier)) {
      this.environmentListeners.put(mapIdentifier, Collections.synchronizedCollection(ConcurrentHashMap.newKeySet()));
    }

    this.environmentListeners.get(mapIdentifier).add(listener);
  }

  public void removeListener(String mapName, EnvironmentListener listener) {
    if (mapName == null || mapName.isEmpty()) {
      return;
    }

    String mapIdentifier = mapName.toLowerCase();
    if (!this.environmentListeners.containsKey(mapIdentifier)) {
      return;
    }

    this.environmentListeners.get(mapIdentifier).remove(listener);
  }

  public ICamera camera() {
    return this.camera;
  }

  public IEnvironment environment() {
    return this.environment;
  }

  public Collection<IEnvironment> getEnvironments() {
    return this.environments.values();
  }

  public IEnvironment getEnvironment(String environmentIdentifier) {
    if (environmentIdentifier == null || environmentIdentifier.isEmpty()) {
      return null;
    }

    return this.getEnvironments().stream().filter(e -> e.identifier().equals(environmentIdentifier)).findFirst().orElse(null);
  }

  public IEnvironment getEnvironmentByMapName(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      return null;
    }

    IMap map = Resources.maps().get(mapName);
    return this.getEnvironment(map);
  }

  public IEnvironment getEnvironment(IMap map) {
    if (map == null || map.getName() == null || map.getName().isEmpty()) {
      return null;
    }

    IEnvironment env = this.getEnvironments().stream().filter(e -> e.getMap().equals(map)).findFirst().orElse(null);
    if (env != null) {
      return env;
    }

    env = new Environment(map);
    this.addEnvironment(env);

    return env;
  }

  public boolean containsEnvironment(String identifier) {
    return this.getEnvironment(identifier) != null;
  }

  public void loadEnvironment(final IEnvironment env) {
    unloadEnvironment();

    if (env != null) {
      this.addEnvironment(env);

      env.load();
      for (final EnvironmentLoadedListener listener : this.loadedListeners) {
        listener.loaded(env);
      }
    }

    this.environment = env;
  }

  public IEnvironment loadEnvironmentByMapName(String mapName) {
    IEnvironment env = this.getEnvironment(mapName);
    this.loadEnvironment(env);
    return env;
  }

  public IEnvironment loadEnvironment(IMap map) {
    IEnvironment env = this.getEnvironment(map);
    this.loadEnvironment(env);
    return env;
  }

  private void addEnvironment(IEnvironment env) {
    if (this.containsEnvironment(env.identifier())) {
      return;
    }

    this.environments.put(env.identifier(), env);

    // wire up all previously registered listeners
    if (env.getMap() != null && env.getMap().getName() != null) {
      String mapName = env.getMap().getName().toLowerCase();
      if (this.environmentListeners.containsKey(mapName)) {
        for (EnvironmentListener listener : this.environmentListeners.get(mapName)) {
          env.addListener(listener);
        }
      }
    }
  }

  public void unloadEnvironment() {
    if (this.environment() != null) {
      this.environment().unload();

      for (final EnvironmentUnloadedListener listener : this.unloadedListeners) {
        listener.unloaded(this.environment());
      }
    }

    this.environment = null;
  }

  public IEnvironment reset(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      return null;
    }

    return this.getEnvironment(Resources.maps().get(mapName));
  }

  public IEnvironment reset(IMap map) {
    if (map == null) {
      return null;
    }

    IEnvironment env = this.getEnvironment(map);
    if (env != null) {
      this.environments.remove(env.identifier());

      if (env.getMap() != null && env.getMap().getName() != null) {
        
        // unwire all registered listeners for this particular map
        String mapName = map.getName().toLowerCase();
        if (this.environmentListeners.containsKey(mapName)) {
          for (EnvironmentListener listener : this.environmentListeners.get(mapName)) {
            env.removeListener(listener);
          }
        }
      }
    }

    return this.getEnvironment(map);
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
