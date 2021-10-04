package de.gurkenlabs.litiengine.resources;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;

public abstract class NamedResource implements Resource {
  @XmlAttribute
  private String name;

  @XmlTransient
  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void setName(final String n) {
    this.name = n;
  }
}
