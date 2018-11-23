package de.gurkenlabs.litiengine.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * An abstract implementation for all classes that provide a certain type of resources.
 * Basically, it's an in-memory cache of the resources and provides access to manage the resources.
 *
 * @param <T>
 *          The type of the resource that is contained by this instance.
 */
public abstract class ResourcesContainer<T> {
  private static final Logger log = Logger.getLogger(ResourcesContainer.class.getName());
  private final Map<String, T> resources = new ConcurrentHashMap<>();

  public void add(String resourceName, T resource) {
    String identifier = resourceName.toLowerCase();
    if (this.resources.containsKey(identifier)) {
      this.resources.remove(identifier);
    }

    this.resources.put(identifier, resource);
  }

  /**
   * Clears this resources container. This method removes all previously loaded resources.
   */
  public void clear() {
    this.resources.clear();
  }

  public boolean contains(String resourceName) {
    return this.resources.containsKey(resourceName.toLowerCase());
  }

  public boolean contains(T resource) {
    return this.resources.values().contains(resource);
  }

  public int count() {
    return this.resources.size();
  }

  public Collection<T> get(Predicate<T> pred) {
    if (pred == null) {
      return new ArrayList<>();
    }

    return this.resources.values().stream().filter(pred).collect(Collectors.toList());
  }

  public T get(String resourceName) {
    return this.get(resourceName, false);
  }

  /**
   * Gets the game resource with the specified name.<br>
   * If not previously loaded, this method attempts to load the resource on the fly otherwise it will be retrieved from the cache.
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

    // the case is ignored when retrieving resources
    String identifier = resourceName.toLowerCase();

    if (!forceLoad && this.resources.containsKey(identifier)) {
      return this.resources.get(identifier);
    }

    T newResource = this.load(identifier);

    if (newResource == null) {

      log.log(Level.SEVERE, "Could not load the game resource {0} because it was not found.", new Object[] { resourceName });
      return null;
    }

    this.add(resourceName, newResource);

    return newResource;
  }

  public Collection<T> getAll() {
    return this.resources.values();
  }

  public T remove(String resourceName) {
    return this.resources.remove(resourceName.toLowerCase());
  }

  public Optional<T> tryGet(String resourceName) {
    return Optional.ofNullable(this.get(resourceName));
  }

  protected T load(String resourceName) {
    return null;
  }

  protected Map<String, T> getResources() {
    return this.resources;
  }
}
