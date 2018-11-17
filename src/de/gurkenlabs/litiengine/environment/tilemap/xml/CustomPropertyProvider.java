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
    for (Property p : propertyProviderToBeCopied.getProperties()) {
      this.getProperties().add(new Property(p));
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
  public String getStringValue(final String name) {
    return this.getStringValue(name, null);
  }

  @Override
  public String getStringValue(String name, String defaultValue) {
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
  public List<Property> getProperties() {
    if (this.properties == null) {
      return new CopyOnWriteArrayList<>();
    }

    return this.properties;
  }

  @Override
  public void setProperties(List<Property> props) {
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
      this.setValue(prop.getName(), prop.getValue());
    }
  }

  @Override
  public int getIntValue(String name) {
    return this.getIntValue(name, 0);
  }

  @Override
  public int getIntValue(String name, int defaultValue) {
    String value = this.getStringValue(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Integer.parseInt(value);
  }

  @Override
  public short getShortValue(String name) {
    return this.getShortValue(name, (short) 0);
  }

  @Override
  public short getShortValue(String name, short defaultValue) {
    String value = this.getStringValue(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Short.parseShort(value);
  }

  @Override
  public byte getByteValue(String name) {
    return this.getByteValue(name, (byte) 0);
  }

  @Override
  public byte getByteValue(String name, byte defaultValue) {
    String value = this.getStringValue(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Byte.parseByte(value);
  }

  @Override
  public long getLongValue(String name) {
    return this.getLongValue(name, 0L);
  }

  @Override
  public long getLongValue(String name, long defaultValue) {
    String value = this.getStringValue(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Long.parseLong(value);
  }

  @Override
  public boolean getBoolValue(String name) {
    return this.getBoolValue(name, false);
  }

  @Override
  public boolean getBoolValue(String name, boolean defaultValue) {
    String value = this.getStringValue(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Boolean.valueOf(value);
  }

  @Override
  public float getFloatValue(String name) {
    return this.getFloatValue(name, 0);
  }

  @Override
  public float getFloatValue(String name, float defaultValue) {
    String value = this.getStringValue(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Float.valueOf(value);
  }

  @Override
  public double getDoubleValue(String name) {
    return this.getDoubleValue(name, 0);
  }

  @Override
  public double getDoubleValue(String name, double defaultValue) {
    String value = this.getStringValue(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return Double.valueOf(value);
  }

  @Override
  public Color getColorValue(String name) {
    return getColorValue(name, null);
  }

  @Override
  public Color getColorValue(String name, Color defaultValue) {
    String value = this.getStringValue(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return ColorHelper.decode(value);
  }

  @Override
  public <T extends Enum<T>> T getEnumValue(String name, Class<T> enumType) {
    return getEnumValue(name, enumType, null);
  }

  @Override
  public <T extends Enum<T>> T getEnumValue(String name, Class<T> enumType, T defaultValue) {
    String value = this.getStringValue(name);
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
  public void setValue(String name, String value) {
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
  public void setValue(String name, boolean value) {
    this.setValue(name, Boolean.toString(value));
  }

  @Override
  public void setValue(String name, byte value) {
    this.setValue(name, Byte.toString(value));
  }

  @Override
  public void setValue(String name, short value) {
    this.setValue(name, Short.toString(value));
  }

  @Override
  public void setValue(String name, int value) {
    this.setValue(name, Integer.toString(value));
  }

  @Override
  public void setValue(String name, long value) {
    this.setValue(name, Long.toString(value));
  }

  @Override
  public void setValue(String name, float value) {
    this.setValue(name, Float.toString(value));
  }

  @Override
  public void setValue(String name, double value) {
    this.setValue(name, Double.toString(value));

  }

  @Override
  public void setValue(String name, Color value) {
    this.setValue(name, ColorHelper.encode(value));
  }

  @Override
  public void setValue(String name, Enum<?> value) {
    this.setValue(name, value.name());
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
