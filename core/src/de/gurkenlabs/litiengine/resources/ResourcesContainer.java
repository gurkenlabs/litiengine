package de.gurkenlabs.litiengine.resources;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;

/**
 * An abstract implementation for all classes that provide a certain type of resources.
 * Basically, it's an in-memory cache of the resources and provides access to manage the resources.
 *
 * @param <T>
 *          The type of the resource that is contained by this instance.
 * 
 * @see ResourcesContainerListener
 */
public abstract class ResourcesContainer<T> {
  // use a work-stealing pool to maximize resource load speed while minimizing the number of resources in use
  private static final ExecutorService ASYNC_POOL = Executors.newWorkStealingPool();

  private final Map<String, T> resources = new ConcurrentHashMap<>();
  private final Map<String, String> aliases = new ConcurrentHashMap<>();
  private final List<ResourcesContainerListener<? super T>> listeners = new CopyOnWriteArrayList<>();
  private final List<ResourcesContainerClearedListener> clearedListeners = new CopyOnWriteArrayList<>();

  static {
    Game.addGameListener(new GameListener() {
      @Override
      public void terminated() {
        ASYNC_POOL.shutdownNow();
      }
    });
  }

  /**
   * Add a new container listener to this instance in order to observe resource life cycles.
   * The listener will get notified whenever a resource was added to or removed from this container.
   * 
   * @param listener
   *          The container listener instance that will receive call backs from this container.
   * 
   * @see #removeContainerListener(ResourcesContainerListener)
   */
  public void addContainerListener(ResourcesContainerListener<? super T> listener) {
    this.listeners.add(listener);
  }

  /**
   * Remove the specified listener from this container.
   * 
   * @param listener
   *          The listener instance that was previously added to this container.
   * 
   * @see #addContainerListener(ResourcesContainerListener)
   */
  public void removeContainerListener(ResourcesContainerListener<T> listener) {
    this.listeners.remove(listener);
  }

  /**
   * Add a new container listener to this instance that observes whenever this instance is cleared.
   * 
   * @param listener
   *          The container listener instance.
   * 
   * @see #removeClearedListener(ResourcesContainerClearedListener)
   */
  public void addClearedListener(ResourcesContainerClearedListener listener) {
    this.clearedListeners.add(listener);
  }

  /**
   * Remove the specified listener from this container.
   * 
   * @param listener
   *          The listener instance that was previously added to this container.
   * 
   * @see #addClearedListener(ResourcesContainerClearedListener)
   */
  public void removeClearedListener(ResourcesContainerClearedListener listener) {
    this.clearedListeners.remove(listener);
  }

  /**
   * Add the specified resource to this container.<br>
   * The added element can later be retrieved from this container by calling {@code get(resourceName)}.
   * <p>
   * Use this method to make a resource accessible over this container during runtime.
   * </p>
   *
   * @param resourceName
   *          The name that the resource is managed by.
   * @param resource
   *          The resource instance.
   * 
   * @see #get(Predicate)
   * @see #get(String)
   * @see #get(String, boolean)
   * @see #remove(String)
   * @see #tryGet(String)
   */
  public void add(String resourceName, T resource) {
    this.resources.put(resourceName, resource);

    for (ResourcesContainerListener<? super T> listener : this.listeners) {
      listener.added(resourceName, resource);
    }
  }

  public void add(URL resourceName, T resource) {
    this.add(resourceName.toString(), resource);
  }

  /**
   * Clears the resource container by removing all previously loaded resources.
   */
  public void clear() {
    this.resources.clear();

    for (ResourcesContainerListener<? super T> listener : this.listeners) {
      listener.cleared();
    }
  }

  /**
   * Checks if this instance contains a resource with the specified name.
   * <p>
   * Note that the name is <b>not case-sensitive</b>.
   * </p>
   * 
   * @param resourceName
   *          The resource's name.
   * @return True if this container contains a resource with the specified name; otherwise false.
   * 
   * @see ResourcesContainer#contains(Object)
   */
  public boolean contains(String resourceName) {
    return this.resources.containsKey(this.getIdentifier(resourceName));
  }

  public boolean contains(URL resourceName) {
    return this.contains(resourceName.toString());
  }

  /**
   * Checks if the specified resource is contained by this instance.
   * 
   * @param resource
   *          The resource.
   * @return True if this instance contains the specified resource instance; otherwise false.
   */
  public boolean contains(T resource) {
    return this.resources.containsValue(resource);
  }

  /**
   * Gets the amount of resources that this container holds.
   * 
   * @return The amount of resources in this container.
   */
  public int count() {
    return this.resources.size();
  }

  /**
   * Gets all resources that match the specified condition.
   * 
   * @param pred
   *          The condition that a resource must fulfill in order to be returned.
   * @return All resources that match the specified condition.
   */
  public Collection<T> get(Predicate<? super T> pred) {
    if (pred == null) {
      return new ArrayList<>();
    }

    return this.resources.values().stream().filter(pred).collect(Collectors.toList());
  }

  /**
   * Gets the resource with the specified name.<br>
   * <p>
   * This is the most common (and preferred) way to fetch resources from a container.
   * </p>
   * <p>
   * If not previously loaded, this method attempts to load the resource on the fly otherwise it will be retrieved from the cache.
   * </p>
   * 
   * @param resourceName
   *          The resource's name.
   * @return The resource with the specified name or null if not found.
   */
  public T get(String resourceName) {
    return this.get(this.getIdentifier(resourceName), false);
  }

  public T get(URL resourceName) {
    return this.get(resourceName, false);
  }

  /**
   * Gets the resource with the specified name.<br>
   * <p>
   * If no such resource is currently present on the container, it will be loaded with the specified {@code loadCallback} and added to this
   * container.
   * </p>
   * 
   * @param resourceName
   *          The resource's name.
   * @param loadCallback
   *          The callback that is used to load the resource on-demand if it's not present on this container.
   * @return T The resource with the specified name.
   */
  public T get(String resourceName, Supplier<? extends T> loadCallback) {
    Optional<T> opt = this.tryGet(resourceName);
    if (opt.isPresent()) {
      return opt.get();
    }

    T resource = loadCallback.get();
    if (resource != null) {
      this.add(resourceName, resource);
    }

    return resource;

  }

  public T get(URL resourceName, Supplier<? extends T> loadCallback) {
    return this.get(resourceName.toString(), loadCallback);
  }

  /**
   * Gets the resource with the specified name.
   * <p>
   * If not previously loaded, this method attempts to load the resource on the fly otherwise it will be retrieved from the cache.
   * </p>
   * 
   * @param resourceName
   *          The name of the game resource.
   * @param forceLoad
   *          If set to true, cached resource (if existing) will be discarded and the resource will be freshly loaded.
   * @return The game resource or null if not found.
   */
  public T get(String resourceName, boolean forceLoad) {
    if (resourceName == null) {
      return null;
    }

    if (forceLoad) {
      T resource = this.loadResource(resourceName);
      if (resource == null) {
        return null;
      }

      this.resources.put(resourceName, resource);

      return resource;
    } else {
      return this.resources.computeIfAbsent(resourceName, this::loadResource);
    }
  }

  public T get(URL resourceName, boolean forceLoad) {
    return this.get(resourceName.toString(), forceLoad);
  }

  /**
   * Eventually gets the resource with the specified location. The resource is loaded asynchronously and can be retrieved from the returned
   * {@code Future} object returned by this method once loaded.
   * 
   * @param location
   *          The location of the resource
   * @return A {@code Future} object that can be used to retrieve the resource once it is finished loading
   */
  public Future<T> getAsync(URL location) {
    return ASYNC_POOL.submit(() -> this.get(location));
  }

  /**
   * Eventually gets the resource with the specified location. The resource is loaded asynchronously and can be retrieved from the returned
   * {@code Future} object returned by this method once loaded.
   * 
   * @param name
   *          The name or location of the resource
   * @return A {@code Future} object that can be used to retrieve the resource once it is finished loading
   */
  public Future<T> getAsync(String name) {
    return this.getAsync(Resources.getLocation(this.getIdentifier(name)));
  }

  /**
   * Gets all loaded resources from this container.
   * 
   * @return All loaded resources.
   */
  public Collection<T> getAll() {
    return this.resources.values();
  }

  /**
   * Removes the resource with the specified name from this container.
   * 
   * @param resourceName
   *          The name of the resource that should be removed.
   * @return The removed resource.
   */
  public T remove(String resourceName) {
    T removedResource = this.resources.remove(resourceName);

    if (removedResource != null) {
      for (ResourcesContainerListener<? super T> listener : this.listeners) {
        listener.removed(resourceName, removedResource);
      }
    }

    return removedResource;

  }

  public T remove(URL resourceName) {
    return this.remove(resourceName.toString());
  }

  /**
   * Tries to get a resource with the specified name from this container.
   * <p>
   * This method should be used, if it's not clear whether the resource is present on this container.<br>
   * It is basically a combination of {@code get(String)} and {@code contains(String)} and allows
   * to check whether a resource is present while also fetching it from the container.
   * </p>
   * 
   * @param resourceName
   *          The name of the resource.
   * 
   * @return An Optional instance that holds the resource instance, if present on this container.
   * 
   * @see Optional
   * @see #contains(String)
   * @see #get(String)
   */
  public Optional<T> tryGet(String resourceName) {
    if (this.contains(resourceName)) {
      return Optional.of(this.get(resourceName));
    }

    return Optional.empty();
  }

  public Optional<T> tryGet(URL resourceName) {
    return this.tryGet(resourceName);
  }

  protected abstract T load(URL resourceName) throws Exception;

  /**
   * Gets an alias for the specified resourceName. Note that the process of providing an alias is up to the ResourceContainer implementation.
   * 
   * @param resourceName
   *          The original name of the resource.
   * @param resource
   *          The resource.
   * @return An alias for the specified resource.
   */
  protected String getAlias(String resourceName, T resource) {
    return null;
  }

  protected Map<String, T> getResources() {
    return this.resources;
  }

  private T loadResource(String identifier) {
    T newResource;
    try {
      newResource = this.load(Resources.getLocation(identifier));
    } catch (Exception e) {
      throw new ResourceLoadException(e);
    }

    for (ResourcesContainerListener<? super T> listener : this.listeners) {
      listener.added(identifier, newResource);
    }

    String alias = this.getAlias(identifier, newResource);
    if (alias != null) {
      this.aliases.put(alias, identifier);
    }

    return newResource;
  }

  private String getIdentifier(String resourceName) {
    return this.aliases.getOrDefault(resourceName, resourceName);
  }
}
