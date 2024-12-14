package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.util.AlphanumComparator;

/**
 * Represents a resource that can be compared to other resources. This interface extends the Comparable interface for Resource objects.
 */
public interface Resource extends Comparable<Resource> {

  /**
   * Gets the name of the resource.
   *
   * @return The name of the resource.
   */
  String getName();

  /**
   * Sets the name of the resource.
   *
   * @param name The new name of the resource.
   */
  void setName(String name);

  /**
   * Compares this resource with the specified resource for order.
   *
   * @param obj The resource to be compared.
   * @return A negative integer, zero, or a positive integer as this resource is less than, equal to, or greater than the specified resource.
   */
  @Override
  default int compareTo(Resource obj) {
    return AlphanumComparator.compareTo(this.getName(), obj.getName());
  }
}
