package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;

public abstract class CustomPropertyProvider implements ICustomPropertyProvider, Serializable {
  private static final long serialVersionUID = 7418225969292279565L;

  @XmlElementWrapper(name = "properties")
  @XmlElement(name = "property")
  private List<Property> properties = new CopyOnWriteArrayList<>();
  
  @Override
  public boolean hasCustomProperty(final String name) {
    return properties != null && properties.stream().anyMatch(p -> p.getName().equals(name));
  }

  @Override
  public String getCustomProperty(final String name) {
    return this.getCustomProperty(name, null);
  }

  @Override
  public String getCustomProperty(String name, String defaultValue) {
    if (this.properties != null && this.properties.stream().anyMatch(x -> x.getName().equals(name))) {
      Optional<Property> opt = this.properties.stream().filter(x -> x.getName().equals(name)).findFirst();
      if (opt.isPresent()) {
        return opt.get().getValue();
      }
    }

    return defaultValue;
  }

  @Override
  public void setCustomProperty(String name, String value) {
    if (this.properties == null || name == null) {
      return;
    }

    Optional<Property> opt = this.properties.stream().filter(x -> x.getName().equals(name)).findFirst();
    if (opt.isPresent()) {
      // clear property if the value is null because there is no need to keep it
      if (value == null || value.isEmpty()) {
        this.properties.removeIf(x -> x.getName().equals(name));
        return;
      }

      opt.get().setValue(value);
      return;
    }

    this.properties.add(new Property(name, value));
  }

  @Override
  @XmlTransient
  public List<Property> getCustomProperties() {
    if (this.properties == null) {
      return new CopyOnWriteArrayList<>();
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

  @Override
  public int getCustomPropertyInt(String name) {
    return this.getCustomPropertyInt(name, 0);
  }

  @Override
  public int getCustomPropertyInt(String name, int defaultValue) {
    String value = this.getCustomProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Integer.parseInt(value);
  }

  @Override
  public boolean getCustomPropertyBool(String name) {
    return this.getCustomPropertyBool(name, false);
  }

  @Override
  public boolean getCustomPropertyBool(String name, boolean defaultValue) {
    String value = this.getCustomProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Boolean.valueOf(value);
  }

  @Override
  public float getCustomPropertyFloat(String name) {
    return this.getCustomPropertyFloat(name, 0);
  }

  @Override
  public float getCustomPropertyFloat(String name, float defaultValue) {
    String value = this.getCustomProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Float.valueOf(value);
  }

  @Override
  public double getCustomPropertyDouble(String name) {
    return this.getCustomPropertyDouble(name, 0);
  }

  @Override
  public double getCustomPropertyDouble(String name, double defaultValue) {
    String value = this.getCustomProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Double.valueOf(value);
  }

  @Override
  public Color getCustomPropertyColor(String name) {
    return getCustomPropertyColor(name, null);
  }

  @Override
  public Color getCustomPropertyColor(String name, Color defaultValue) {
    String value = this.getCustomProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Color.decode(value);
  }

  @Override
  public <T extends Enum<T>> T getCustomPropertyEnum(String name, Class<T> enumType) {
    return getCustomPropertyEnum(name, enumType, null);
  }

  @Override
  public <T extends Enum<T>> T getCustomPropertyEnum(String name, Class<T> enumType, T defaultValue) {
    String value = this.getCustomProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    try {
      return Enum.valueOf(enumType, value);
    } catch (final IllegalArgumentException iae) {
      return defaultValue;
    }
  }

  private static int sortPropertiesByName(Property prop1, Property prop2) {
    if (prop1 == null) {
      return -1;
    }

    if (prop2 == null) {
      return 1;
    }

    if (prop1.getName() == null) {
      if (prop2.getName() == null) {
        return 0;
      }

      return -1;
    }

    return prop1.getName().compareTo(prop2.getName());
  }
  
  void beforeMarshal(Marshaller m) {
    if (this.properties != null && this.properties.isEmpty()) {
      this.properties = null;
      return;
    }

    if (this.properties != null) {
      this.properties.sort(CustomPropertyProvider::sortPropertiesByName);
    }
  }
}
