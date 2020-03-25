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

/**
 * The <code>GameWorld</code> class is a global environment manager that contains all <code>Environments</code>
 * and provides the currently active <code>Environment</code> and <code>Camera</code>.<br>
 * <p>
 * The <code>GameWorld</code> returns the same instance for a particular map/mapName until the
 * <code>GameWorld.reset(String)</code> method is called.
 * </p>
 * 
 * Moreover, it provides the possibility to attach game logic via <code>EnvironmentListeners</code> to different events of the
 * <code>Envrionment's</code> life cycle (e.g. loaded, initialized, ...).<br>
 * <i>This is typically used to provide some per-level logic or to trigger
 * general loading behavior.</i>
 * 
 * @see Environment
 * @see Camera
 * @see GameWorld#environment()
 * @see GameWorld#camera()
 * @see GameWorld#reset(String)
 *
 */
public final class GameWorld implements IUpdateable {



  //<editor-fold desc="extra camera vars and consts">

  // Note that this code is placed on top for convenience of development.
  // The code should be moved to more fitting place once the extra camera feature development had ended.

  // The number of cameras to be loaded into the Game World.
  // This constant value is temporary, final implementation should
  // load this from a configuration object / file or set using game settings.
  // The number of cameras should remain constant for the Game World at all times.
  public static final int NUMBER_OF_CAMERAS = 3;

  // The index for the active camera.
  // This is set during render-time when the game world is rendered.
  private int activeCameraIndex;

  public int getActiveCameraIndex() {
    return activeCameraIndex;
  }
  public void setActiveCameraIndex(int activeCameraIndex) {
    this.activeCameraIndex = activeCameraIndex;
    if (activeCameraIndex < 0 || activeCameraIndex >= NUMBER_OF_CAMERAS)
      this.activeCameraIndex = 0;
  }

  public ICamera getCamera(int index){
    return cameras[index];
  }

  /**
   * Gets the camera currently / last used for rendering.
   * Use this in render code to get the camera.
   * @return
   */
  public ICamera getActiveCamera(){
    return getCamera(activeCameraIndex);
  }

  public void setCamera(final ICamera cam, int index) {
    if (cameras[index] != null) {
      Game.loop().detach(cameras[index]);
    }
    cameras[index] = cam;

    if (cam != null && !Game.isInNoGUIMode()) {
      Game.loop().attach(cam);
      cam.updateFocus();
    }
  }


  //</editor-fold>


  private final List<EnvironmentListener> listeners = new CopyOnWriteArrayList<>();
  private final List<EnvironmentLoadedListener> loadedListeners = new CopyOnWriteArrayList<>();
  private final List<EnvironmentUnloadedListener> unloadedListeners = new CopyOnWriteArrayList<>();

  private final Map<String, Collection<EnvironmentListener>> environmentListeners = new ConcurrentHashMap<>();
  private final Map<String, Collection<EnvironmentLoadedListener>> environmentLoadedListeners = new ConcurrentHashMap<>();
  private final Map<String, Collection<EnvironmentUnloadedListener>> environmentUnloadedListeners = new ConcurrentHashMap<>();
  private final Map<String, Collection<IUpdateable>> updatables = new ConcurrentHashMap<>();

  private final Map<String, Environment> environments = new ConcurrentHashMap<>();

  private Environment environment;
  private ICamera[] cameras;
  private int gravity;

  /**
   * <p>
   * <b>You should never call this manually! Instead use the <code>Game.world()</code> instance.</b>
   * </p>
   * 
   * @see Game#world()
   */
  public GameWorld() {
    if (Game.world() != null) {
      throw new UnsupportedOperationException("Never initialize a GameWorld manually. Use Game.world() instead.");
    }
    cameras = new ICamera[NUMBER_OF_CAMERAS];
  }

  /**
   * Don't call this manually!
   */
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

  /**
   * Adds the specified environment listener to receive events about the basic life-cycle of environments. This is a global event that gets called for
   * any map.
   * 
   * @param listener
   *          The listener to add.
   */
  public void addListener(EnvironmentListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Removes the specified environment listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeListener(EnvironmentListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Adds the specified environment loaded listener to receive events for when an environment gets loaded. This is a global event that gets called for
   * any map.
   * 
   * @param listener
   *          The listener to add.
   */
  public void addLoadedListener(EnvironmentLoadedListener listener) {
    this.loadedListeners.add(listener);
  }

  /**
   * Removes the specified environment loaded listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeLoadedListener(EnvironmentLoadedListener listener) {
    this.loadedListeners.remove(listener);
  }

  /**
   * Adds the specified environment unloaded listener to receive events for when an environment gets unloaded. This is a global event that gets called
   * for any map.
   * 
   * @param listener
   *          The listener to add.
   */
  public void addUnloadedListener(EnvironmentUnloadedListener listener) {
    this.unloadedListeners.add(listener);
  }

  /**
   * Removes the specified environment unloaded listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeUnloadedListener(EnvironmentUnloadedListener listener) {
    this.unloadedListeners.remove(listener);
  }

  /**
   * Adds the specified environment loaded listener to receive events for when an environment with the specified map name gets loaded.
   * 
   * @param mapName
   *          The name of the map for which to add the listener.
   * @param listener
   *          The listener to add.
   */
  public void addLoadedListener(String mapName, EnvironmentLoadedListener listener) {
    add(this.environmentLoadedListeners, mapName, listener);
  }

  /**
   * Removes the specified environment loaded listener for the specified map name.
   * 
   * @param mapName
   *          The name of the map for which to remove the listener.
   * @param listener
   *          The listener to remove.
   */
  public void removeLoadedListener(String mapName, EnvironmentLoadedListener listener) {
    remove(this.environmentLoadedListeners, mapName, listener);
  }

  /**
   * Adds the specified environment unloaded listener to receive events for when an environment with the specified map name gets unloaded.
   * 
   * @param mapName
   *          The name of the map for which to add the listener.
   * @param listener
   *          The listener to add.
   */
  public void addUnloadedListener(String mapName, EnvironmentUnloadedListener listener) {
    add(this.environmentUnloadedListeners, mapName, listener);
  }

  /**
   * Removes the specified environment unloaded listener for the specified map name.
   *
   * @param mapName
   *          The name of the map for which to remove the listener.
   * @param listener
   *          The listener to remove.
   */
  public void removeUnloadedListener(String mapName, EnvironmentUnloadedListener listener) {
    add(this.environmentUnloadedListeners, mapName, listener);
  }

  /**
   * Adds the specified environment listener to receive events about the basic life-cycle of environments with the specified map name.
   * 
   * @param mapName
   *          The name of the map for which to add the listener.
   * @param listener
   *          The listener to add.
   */
  public void addListener(String mapName, EnvironmentListener listener) {
    add(this.environmentListeners, mapName, listener);
  }

  /**
   * Removes the specified environment listener.
   * 
   * @param mapName
   *          The name of the map for which to remove the listener.
   * @param listener
   *          The listener to remove.
   */
  public void removeListener(String mapName, EnvironmentListener listener) {
    remove(this.environmentListeners, mapName, listener);
  }

  /**
   * Attaches the specified updatable instance that only gets updated when an environment with the specified map name is currently loaded.
   * 
   * @param mapName
   *          The name of the map for which to attach the updatable instance.
   * @param updateable
   *          The updatable instance to attach.
   */
  public void attach(String mapName, IUpdateable updateable) {
    add(this.updatables, mapName, updateable);
  }

  /**
   * Detaches the specified updatable instance from the updating of environments with the specified map name.
   * 
   * @param mapName
   *          The name of the map for which to detach the updatable instance.
   * @param updateable
   *          The updatable instance to detach.
   */
  public void detach(String mapName, IUpdateable updateable) {
    remove(this.updatables, mapName, updateable);
  }

  /**
   * Gets the game's current <code>Camera</code>.
   *
   * @deprecated There may now be more than one camera in the world.
   *             use {@link #getActiveCamera()} or {@link #getCamera(int)} instead.
   *             Currently redirects to {@link #getActiveCamera()}
   *
   *
   * @return The currently active camera.
   * 
   * @see ICamera
   */
  @Deprecated
  public ICamera camera() {
    return getActiveCamera();
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

  /**
   * Gets the game worlds gravity that is applied to any environment. This can e.g. be useful for platformers.
   * 
   * @return The gravity of the game world that gets applied to any environment.
   */
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

    this.listeners.clear();
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
    Lock lock = Game.loop().getLock();
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

    return this.reset(Resources.maps().get(mapName));
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

        for (EnvironmentListener listener : this.listeners) {
          env.removeListener(listener);
        }
      }
    }

    return env;
  }

  /**
   * Sets the active camera of the game.
   *
   * @deprecated  There now may be more than one camera in the world, so you need to specify.<br>
   *              Please use {@link #setCamera(ICamera, int)} instead.
   *              Currently redirects to {@link #setCamera(ICamera, int)} with 0 as camera index
   *
   * @param cam
   *          The new camera to be set.
   */
  @Deprecated
  public void setCamera(final ICamera cam) {
    setCamera(cam, 0);
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

    for (EnvironmentListener listener : this.listeners) {
      env.addListener(listener);
    }
  }
}
