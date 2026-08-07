package de.gurkenlabs.litiengine.scripting;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

/** A persisted string representation of a typed script parameter. */
@XmlAccessorType(XmlAccessType.FIELD)
public final class ScriptParameterValue {
  @XmlAttribute(required = true) private String name;
  @XmlAttribute private String value;

  public ScriptParameterValue() {}

  public ScriptParameterValue(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return this.name;
  }

  public String getValue() {
    return this.value;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
