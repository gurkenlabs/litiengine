package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.util.AlphanumComparator;

public interface Resource extends Comparable<Resource> {

  /**
   * Gets the name.
   *
   * @return the name
   */
  String getName();

  void setName(String name);

  @Override
  default int compareTo(Resource obj) {
    return AlphanumComparator.compareTo(this.getName(), obj.getName());
  }
}
