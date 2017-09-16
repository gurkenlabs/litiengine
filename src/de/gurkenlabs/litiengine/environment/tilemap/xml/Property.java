package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class Property.
 */
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class Property implements Serializable {
  private static final long serialVersionUID = 4684901360660207599L;

  /** The name. */
  @XmlAttribute
  private String name;

  @XmlAttribute(required = false)
  private String type;

  /** The value. */
  @XmlAttribute
  private String value;

  public Property() {
  }

  public Property(String name, String value) {
    this.name = name;
    this.value = value;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  public String getValue() {
    return this.value;
  }

  /**
   * Sets the name.
   *
   * @param name
   *          the new name
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Sets the value.
   *
   * @param value
   *          the new value
   */
  public void setValue(final String value) {
    this.value = value;
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }

}
