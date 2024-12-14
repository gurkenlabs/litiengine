package de.gurkenlabs.litiengine.resources;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * An abstract class representing a named resource. This class implements the Resource interface and provides methods to get and set the resource
 * name.
 */
public abstract class NamedResource implements Resource {
  @XmlAttribute
  private String name;

  /**
   * Gets the name of the resource.
   *
   * @return The name of the resource.
   */
  @XmlTransient
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of the resource.
   *
   * @param n The new name of the resource.
   */
  @Override
  public void setName(final String n) {
    this.name = n;
  }
}
