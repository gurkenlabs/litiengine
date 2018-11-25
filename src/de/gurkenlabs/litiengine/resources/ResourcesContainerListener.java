package de.gurkenlabs.litiengine.resources;

/**
 * This listener provides callbacks to observe <code>ResourcesContainer</code> instances.
 *
 * @param <T>
 *          The type of the resource that is managed by the container.
 * 
 * @see ResourcesContainer
 * @see Images
 * @see Fonts
 * @see Maps
 * @see Sounds
 * @see Spritesheets
 * 
 */
public interface ResourcesContainerListener<T> extends ResourcesContainerClearedListener {

  /**
   * This method gets called after the <code>ResourcesContainer.add</code> method was executed.
   * 
   * @param resourceName
   *          The name by which the added resource is identified.
   * @param resource
   *          The added resource.
   * @see ResourcesContainer#add(String, Object)
   */
  public void added(String resourceName, T resource);

  /**
   * This method gets called after the <code>ResourcesContainer.remove</code> method was executed.
   * 
   * @param resourceName
   *          The name by which the removed resource was identified.
   * @param resource
   *          The removed resource.
   * @see ResourcesContainer#remove(String)
   */
  public void removed(String resourceName, T resource);
}
