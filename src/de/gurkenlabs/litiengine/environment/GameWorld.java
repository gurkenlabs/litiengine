package de.gurkenlabs.litiengine.environment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.graphics.Camera;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.resources.Resources;

public final class GameWorld implements IUpdateable {
  private final List<EnvironmentLoadedListener> loadedListeners = new CopyOnWriteArrayList<>();
  private final List<EnvironmentUnloadedListener> unloadedListeners = new CopyOnWriteArrayList<>();
  private final Map<String, Collection<EnvironmentListener>> environmentListeners = new ConcurrentHashMap<>();
  private final Map<String, Collection<EnvironmentLoadedListener>> environmentLoadedListeners = new ConcurrentHashMap<>();
  private final Map<String, Collection<EnvironmentUnloadedListener>> environmentUnloadedListeners = new ConcurrentHashMap<>();
  private final Map<String, Collection<IUpdateable>> updatables = new ConcurrentHashMap<>();

  private final Map<String, Environment> environments = new ConcurrentHashMap<>();

  private Environment environment;
  private ICamera camera;
  private int gravity;

  @Override
  public void update() {
    if (this.environment() == null) {
      return;
    }

    String mapName = getMapName(this.environment());
    if (mapName != null && this.updatables.containsKey(mapName)) {
      for (IUpdateable updatable : this.updatables.get(mapName)) {
        updatable.update();
      }
    }
  }

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

  public void addLoadedListener(String mapName, EnvironmentLoadedListener listener) {
    add(this.environmentLoadedListeners, mapName, listener);
  }

  public void removeLoadedListener(String mapName, EnvironmentLoadedListener listener) {
    remove(this.environmentLoadedListeners, mapName, listener);
  }

  public void addUnloadedListener(String mapName, EnvironmentUnloadedListener listener) {
    add(this.environmentUnloadedListeners, mapName, listener);
  }

  public void removeUnloadedListener(String mapName, EnvironmentUnloadedListener listener) {
    add(this.environmentUnloadedListeners, mapName, listener);
  }

  public void addListener(String mapName, EnvironmentListener listener) {
    add(this.environmentListeners, mapName, listener);
  }

  public void removeListener(String mapName, EnvironmentListener listener) {
    remove(this.environmentListeners, mapName, listener);
  }

  public void attach(String mapName, IUpdateable updateable) {
    add(this.updatables, mapName, updateable);
  }

  public void detach(String mapName, IUpdateable updateable) {
    remove(this.updatables, mapName, updateable);
  }

  /**
   * Gets the game's current <code>Camera</code>.
   * 
   * @return The currently active camera.
   * 
   * @see ICamera
   */
  public ICamera camera() {
    return this.camera;
  }

  /**
   * Gets the game's current <code>Environment</code>.
   * 
   * @return The currently active environment.
   * 
   * @see Environment
   */
  public Environment environment() {
    return this.environment;
  }

  public int gravity() {
    return this.gravity;
  }

  /**
   * Clears the currently active camera and environment, removes all previously loaded environments
   * and clears all listener lists.
   */
  public void clear() {
    this.unloadEnvironment();
    this.environments.clear();
    this.setCamera(null);

    this.environmentListeners.clear();
    this.environmentLoadedListeners.clear();
    this.environmentUnloadedListeners.clear();

    this.loadedListeners.clear();
    this.unloadedListeners.clear();
  }

  /**
   * Gets all environments that are known to the game world.
   * 
   * @return All known environments.
   */
  public Collection<Environment> getEnvironments() {
    return this.environments.values();
  }

  /**
   * Gets the environment that's related to the specified mapName.<br>
   * This method implicitly creates a new <code>Environment</code> if necessary.
   * 
   * @param mapName
   *          The map name by which the environment is identified.
   * @return The environment for the map name or null if no such map can be found.
   */
  public Environment getEnvironment(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      return null;
    }

    IMap map = Resources.maps().get(mapName);
    return this.getEnvironment(map);
  }

  /**
   * Gets the environment that's related to the specified map.<br>
   * This method implicitly creates a new <code>Environment</code> if necessary.
   * 
   * @param map
   *          The map by which the environment is identified.
   * @return The environment for the map or null if no such map can be found.
   */
  public Environment getEnvironment(IMap map) {
    if (map == null || map.getName() == null || map.getName().isEmpty()) {
      return null;
    }

    Environment env = this.getEnvironments().stream().filter(e -> e.getMap().equals(map)).findFirst().orElse(null);
    if (env != null) {
      return env;
    }

    env = new Environment(map);
    this.addEnvironment(env);

    return env;
  }

  /**
   * Indicates whether this instance already contains an <code>Environment</code> for the specified map name.
   * 
   * @param mapName
   *          The map name by which the environment is identified.
   * @return True if the game world already has an environment for the specified map name; otherwise false.
   */
  public boolean containsEnvironment(String mapName) {
    return this.environments.containsKey(mapName.toLowerCase());
  }

  /**
   * Loads the specified <code>Environment</code> and sets it as current environment of the game.
   * This implicitly unloads the previously loaded environment (if present).
   * 
   * <p>
   * <i>The loaded environment can then be accessed via <code>GameWorld#environment()</code>.</i>
   * </p>
   * 
   * @param env
   *          The environment to be loaded.
   * 
   * @see GameWorld#environment()
   */
  public void loadEnvironment(final Environment env) {
    Lock lock = Game.renderLoop().getLock();
    lock.lock();
    try {
      unloadEnvironment();
      this.environment = env;
      if (env != null) {
        this.addEnvironment(env);
  
        if (env.getGravity() == 0 && this.gravity() != 0) {
          env.setGravity(this.gravity());
        }
  
        env.load();
        for (final EnvironmentLoadedListener listener : this.loadedListeners) {
          listener.loaded(env);
        }
  
        // call map specific listeners
        String mapName = getMapName(env);
        if (mapName != null && this.environmentLoadedListeners.containsKey(mapName)) {
          
          // for the default camera we center the camera on the environment
          if (this.camera().getClass().equals(Camera.class)) {
            camera().setFocus(env.getCenter());
          }
          
          for (EnvironmentLoadedListener listener : this.environmentLoadedListeners.get(mapName)) {
            listener.loaded(env);
          }
        }
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Loads the <code>Environment</code> that is identified by the specified map name and sets it as current environment of the game.
   * This implicitly unloads the previously loaded environment (if present).
   * 
   * <p>
   * <i>The loaded environment can then be accessed via <code>GameWorld#environment()</code>.</i>
   * </p>
   * 
   * @param mapName
   *          The map name by which the environment is identified.
   * @return The loaded environment.
   * 
   * @see GameWorld#environment()
   * @see GameWorld#loadEnvironment(Environment)
   */
  public Environment loadEnvironment(String mapName) {
    Environment env = this.getEnvironment(mapName);
    this.loadEnvironment(env);
    return env;
  }

  /**
   * Loads the <code>Environment</code> that is identified by the specified map and sets it as current environment of the game.
   * This implicitly unloads the previously loaded environment (if present).
   * 
   * <p>
   * <i>The loaded environment can then be accessed via <code>GameWorld#environment()</code>.</i>
   * </p>
   * 
   * @param map
   *          The map by which the environment is identified.
   * @return The loaded environment.
   * 
   * @see GameWorld#environment()
   * @see GameWorld#loadEnvironment(Environment)
   */
  public Environment loadEnvironment(IMap map) {
    Environment env = this.getEnvironment(map);
    this.loadEnvironment(env);
    return env;
  }

  /**
   * Unloads the current <code>Environment</code> and sets it to null.
   */
  public void unloadEnvironment() {
    if (this.environment() != null) {
      this.environment().unload();

      for (final EnvironmentUnloadedListener listener : this.unloadedListeners) {
        listener.unloaded(this.environment());
      }

      // call map specific listeners
      String mapName = getMapName(this.environment());
      if (mapName != null && this.environmentUnloadedListeners.containsKey(mapName)) {
        for (EnvironmentUnloadedListener listener : this.environmentUnloadedListeners.get(mapName)) {
          listener.unloaded(this.environment());
        }
      }
    }

    this.environment = null;
  }

  /**
   * Resets the previously loaded <code>Environment</code> for the specified map name so that it can be re-initiated upon the next access.
   * 
   * <p>
   * <i>This can be used if one wants to completely reset the state of a level to its initial state. It'll just throw away the current environment
   * instance and reload a new one upon the next access.</i>
   * </p>
   * 
   * @param mapName
   *          The map name by which the environment is identified.
   * @return The environment instance that was reset or null if none was previously loaded.
   * 
   * @see GameWorld#getEnvironment(String)
   * @see GameWorld#reset(IMap)
   */
  public Environment reset(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      return null;
    }

    return this.getEnvironment(Resources.maps().get(mapName));
  }

  /**
   * Resets the previously loaded <code>Environment</code> for the specified map so that it can be re-initiated upon the next access.
   * 
   * <p>
   * <i>This can be used if one wants to completely reset the state of a level to its initial state. It'll just throw away the current environment
   * instance and reload a new one upon the next access.</i>
   * </p>
   * 
   * @param map
   *          The map by which the environment is identified.
   * @return The environment instance that was reset or null if none was previously loaded.
   * 
   * @see GameWorld#getEnvironment(String)
   * @see GameWorld#reset(IMap)
   */
  public Environment reset(IMap map) {
    if (map == null) {
      return null;
    }

    Environment env = this.getEnvironment(map);
    if (env != null) {
      String mapName = getMapName(env);
      if (mapName != null) {
        this.environments.remove(mapName);

        // unwire all registered listeners for this particular map
        if (this.environmentListeners.containsKey(mapName)) {
          for (EnvironmentListener listener : this.environmentListeners.get(mapName)) {
            env.removeListener(listener);
          }
        }
      }
    }

    return env;
  }

  /**
   * Sets the active camera of the game.
   * 
   * @param cam
   *          The new camera to be set.
   */
  public void setCamera(final ICamera cam) {
    if (this.camera() != null) {
      Game.renderLoop().detach(camera);
    }

    camera = cam;

    if (cam != null && !Game.isInNoGUIMode()) {
      Game.renderLoop().attach(cam);
      cam.updateFocus();
    }
  }

  /**
   * Specify the general gravity that will be used as default value for all environments that are loaded.
   * The value's unit of measure is pixel/second (similar to the velocity of a <code>IMobileEntity</code>.
   * 
   * @param gravity
   *          The default gravity for all environments.
   * 
   * @see IMobileEntity#getVelocity()
   */
  public void setGravity(int gravity) {
    this.gravity = gravity;
  }

  private static <T> void add(Map<String, Collection<T>> listeners, String mapName, T listener) {
    if (mapName == null || mapName.isEmpty()) {
      return;
    }

    String mapIdentifier = mapName.toLowerCase();
    if (!listeners.containsKey(mapIdentifier)) {
      listeners.put(mapIdentifier, Collections.synchronizedCollection(ConcurrentHashMap.newKeySet()));
    }

    listeners.get(mapIdentifier).add(listener);
  }

  private static <T> void remove(Map<String, Collection<T>> listeners, String mapName, T listener) {
    if (mapName == null || mapName.isEmpty()) {
      return;
    }

    String mapIdentifier = mapName.toLowerCase();
    if (!listeners.containsKey(mapIdentifier)) {
      return;
    }

    listeners.get(mapIdentifier).remove(listener);
  }

  private static String getMapName(Environment env) {
    if (env.getMap() != null && env.getMap().getName() != null) {
      return env.getMap().getName().toLowerCase();
    }

    return null;
  }

  private void addEnvironment(Environment env) {
    String mapName = getMapName(env);
    if (mapName == null) {
      return;
    }

    if (this.containsEnvironment(mapName)) {
      return;
    }

    this.environments.put(mapName, env);

    // wire up all previously registered listeners
    if (this.environmentListeners.containsKey(mapName)) {
      for (EnvironmentListener listener : this.environmentListeners.get(mapName)) {
        env.addListener(listener);
      }
    }
  }
}
