package de.gurkenlabs.litiengine.resources;

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
    if (obj == null) {
      return 1;
    }

    if (this.getName() == null) {
      if (obj.getName() == null) {
        return 0;
      }

      return -1;
    }

    if (obj.getName() == null) {
      return 1;
    }

    return this.getName().compareTo(obj.getName());
  }
}
