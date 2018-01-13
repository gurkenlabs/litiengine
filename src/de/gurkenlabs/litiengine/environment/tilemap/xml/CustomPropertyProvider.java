package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;

public class CustomPropertyProvider implements ICustomPropertyProvider, Serializable {
  private static final long serialVersionUID = 7418225969292279565L;

  /** The properties. */
  @XmlElementWrapper(name = "properties")
  @XmlElement(name = "property")
  private List<Property> properties = new ArrayList<>();

  @Override
  public String getCustomProperty(final String name) {
    if (this.properties != null && this.properties.stream().anyMatch(x -> x.getName().equals(name))) {
      Optional<Property> opt = this.properties.stream().filter(x -> x.getName().equals(name)).findFirst();
      if (opt.isPresent()) {
        return opt.get().getValue();
      }
    }

    return null;
  }

  @Override
  public void setCustomProperty(String name, String value) {
    if (this.properties == null || name == null) {
      return;
    }

    Optional<Property> opt = this.properties.stream().filter(x -> x.getName().equals(name)).findFirst();
    if (opt.isPresent()) {
      opt.get().setValue(value);
      return;
    }

    this.properties.add(new Property(name, value));
  }

  @Override
  @XmlTransient
  public List<Property> getAllCustomProperties() {
    if (this.properties == null) {
      return new ArrayList<>();
    }

    return this.properties;
  }

  @Override
  public void setCustomProperties(List<Property> props) {
    if (props == null) {
      this.properties = null;
      return;
    }
    if (this.properties != null) {
      this.properties.clear();
    } else {
      this.properties = new ArrayList<>();
    }

    for (Property prop : props) {
      this.setCustomProperty(prop.getName(), prop.getValue());
    }
  }

  void beforeMarshal(Marshaller m) {
    if (this.properties != null && this.properties.isEmpty()) {
      this.properties = null;
    }
  }

  @Override
  public int getCustomPropertyInt(String name) {
    String value = this.getCustomProperty(name);
    if (value == null || value.isEmpty()) {
      return 0;
    }

    return Integer.parseInt(value);
  }

  @Override
  public boolean getCustomPropertyBool(String name) {
    String value = this.getCustomProperty(name);
    if (value == null || value.isEmpty()) {
      return false;
    }

    return Boolean.valueOf(value);
  }

  @Override
  public float getCustomPropertyFloat(String name) {
    String value = this.getCustomProperty(name);
    if (value == null || value.isEmpty()) {
      return 0;
    }

    return Float.valueOf(value);
  }

  @Override
  public double getCustomPropertyDouble(String name) {
    String value = this.getCustomProperty(name);
    if (value == null || value.isEmpty()) {
      return 0;
    }

    return Double.valueOf(value);
  }
}
