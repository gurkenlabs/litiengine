package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "objecttype")
public class ObjectType implements Serializable {
  private static final long serialVersionUID = 2678464886060196465L;

  @XmlAttribute
  private String name;

  @XmlAttribute
  private String color;

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getColor() {
    return this.color;
  }

  public void setColor(final String color) {
    this.color = color;
  }
}
