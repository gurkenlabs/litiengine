package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.util.AlphanumComparator;

public interface Resource extends Comparable<Resource> {

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName();

  public void setName(String name);

  @Override
  public default int compareTo(Resource obj) {
    return AlphanumComparator.compareTo(this.getName(), obj.getName());
  }
}
