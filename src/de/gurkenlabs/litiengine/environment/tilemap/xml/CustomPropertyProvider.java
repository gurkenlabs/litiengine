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
import de.gurkenlabs.litiengine.util.ColorHelper;

public class CustomPropertyProvider implements ICustomPropertyProvider, Serializable {
  private static final long serialVersionUID = 7418225969292279565L;

  public CustomPropertyProvider() {
    super();
  }

  /**
   * Copy Constructor for copying instances of CustomPropertyProviders.
   *
   * @param propertyProviderToBeCopied
   *          the PropertyProvider we want to copy
   */
  public CustomPropertyProvider(CustomPropertyProvider propertyProviderToBeCopied) {
    super();
    for (Property p : propertyProviderToBeCopied.getCustomProperties()) {
      this.getCustomProperties().add(new Property(p));
    }
  }

  @XmlElementWrapper(name = "properties")
  @XmlElement(name = "property")
  private List<Property> properties = new CopyOnWriteArrayList<>();

  @Override
  public boolean hasCustomProperty(final String name) {
    return properties != null && properties.stream().anyMatch(p -> p.getName().equals(name));
  }

  @Override
  public String getStringProperty(final String name) {
    return this.getStringProperty(name, null);
  }

  @Override
  public String getStringProperty(String name, String defaultValue) {
    if (this.properties != null && this.properties.stream().anyMatch(x -> x.getName().equals(name))) {
      Optional<Property> opt = this.properties.stream().filter(x -> x.getName().equals(name)).findFirst();
      if (opt.isPresent()) {
        return opt.get().getValue();
      }
    }

    return defaultValue;
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
      this.setProperty(prop.getName(), prop.getValue());
    }
  }

  @Override
  public int getIntProperty(String name) {
    return this.getIntProperty(name, 0);
  }

  @Override
  public int getIntProperty(String name, int defaultValue) {
    String value = this.getStringProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Integer.parseInt(value);
  }

  @Override
  public short getShortProperty(String name) {
    return this.getShortProperty(name, (short) 0);
  }

  @Override
  public short getShortProperty(String name, short defaultValue) {
    String value = this.getStringProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Short.parseShort(value);
  }

  @Override
  public byte getByteProperty(String name) {
    return this.getByteProperty(name, (byte) 0);
  }

  @Override
  public byte getByteProperty(String name, byte defaultValue) {
    String value = this.getStringProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Byte.parseByte(value);
  }

  @Override
  public long getLongProperty(String name) {
    return this.getLongProperty(name, 0L);
  }

  @Override
  public long getLongProperty(String name, long defaultValue) {
    String value = this.getStringProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Long.parseLong(value);
  }

  @Override
  public boolean getBoolProperty(String name) {
    return this.getBoolProperty(name, false);
  }

  @Override
  public boolean getBoolProperty(String name, boolean defaultValue) {
    String value = this.getStringProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Boolean.valueOf(value);
  }

  @Override
  public float getFloatProperty(String name) {
    return this.getFloatProperty(name, 0);
  }

  @Override
  public float getFloatProperty(String name, float defaultValue) {
    String value = this.getStringProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Float.valueOf(value);
  }

  @Override
  public double getDoubleProperty(String name) {
    return this.getDoubleProperty(name, 0);
  }

  @Override
  public double getDoubleProperty(String name, double defaultValue) {
    String value = this.getStringProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Double.valueOf(value);
  }

  @Override
  public Color getColorProperty(String name) {
    return getColorProperty(name, null);
  }

  @Override
  public Color getColorProperty(String name, Color defaultValue) {
    String value = this.getStringProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return ColorHelper.decode(value);
  }

  @Override
  public <T extends Enum<T>> T getEnumProperty(String name, Class<T> enumType) {
    return getEnumProperty(name, enumType, null);
  }

  @Override
  public <T extends Enum<T>> T getEnumProperty(String name, Class<T> enumType, T defaultValue) {
    String value = this.getStringProperty(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    try {
      return Enum.valueOf(enumType, value);
    } catch (final IllegalArgumentException iae) {
      return defaultValue;
    }
  }

  @Override
  public void setProperty(String name, String value) {
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
  public void setProperty(String name, boolean value) {
    this.setProperty(name, Boolean.toString(value));
  }

  @Override
  public void setProperty(String name, byte value) {
    this.setProperty(name, Byte.toString(value));
  }

  @Override
  public void setProperty(String name, short value) {
    this.setProperty(name, Short.toString(value));
  }

  @Override
  public void setProperty(String name, int value) {
    this.setProperty(name, Integer.toString(value));
  }

  @Override
  public void setProperty(String name, long value) {
    this.setProperty(name, Long.toString(value));
  }

  @Override
  public void setProperty(String name, float value) {
    this.setProperty(name, Float.toString(value));
  }

  @Override
  public void setProperty(String name, double value) {
    this.setProperty(name, Double.toString(value));

  }

  @Override
  public void setProperty(String name, Color value) {
    this.setProperty(name, ColorHelper.encode(value));
  }

  @Override
  public void setProperty(String name, Enum<?> value) {
    this.setProperty(name, value.name());
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
