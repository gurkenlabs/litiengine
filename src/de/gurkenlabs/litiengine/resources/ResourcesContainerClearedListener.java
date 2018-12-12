package de.gurkenlabs.litiengine.resources;

/**
 * This listener provides a callback for when a <code>ResourcesContainer</code> was cleared.
 *
 * @see ResourcesContainer
 * 
 */
public interface ResourcesContainerClearedListener {
  /**
   * This method gets called after the <code>ResourcesContainer.clear</code> method was executed.
   * 
   * @see ResourcesContainer#clear()
   */
  public void cleared();
}
