package de.gurkenlabs.litiengine.resources;

/**
 * This listener provides callbacks to observe {@code ResourcesContainer} instances.
 *
 * @param <T> The type of the resource that is managed by the container.
 * @see ResourcesContainer
 * @see Images
 * @see Fonts
 * @see Maps
 * @see Sounds
 * @see Spritesheets
 */
public interface ResourcesContainerListener<T> extends ResourcesContainerClearedListener {

  /**
   * This method gets called after the {@code ResourcesContainer.add} method was executed.
   *
   * @param resourceName The name by which the added resource is identified.
   * @param resource     The added resource.
   * @see ResourcesContainer#add(String, Object)
   */
  default void added(String resourceName, T resource) {
  }

  /**
   * This method gets called after the {@code ResourcesContainer.remove} method was executed.
   *
   * @param resourceName The name by which the removed resource was identified.
   * @param resource     The removed resource.
   * @see ResourcesContainer#remove(String)
   */
  default void removed(String resourceName, T resource) {
  }

  /**
   * This method gets called after the {@code ResourcesContainer.clear} method was executed. It notifies that all resources have been removed from the
   * container.
   *
   * @see ResourcesContainer#clear()
   */
  default void cleared() {
  }
}
