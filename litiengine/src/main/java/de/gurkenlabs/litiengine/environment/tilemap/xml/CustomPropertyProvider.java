package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;


@XmlAccessorType(XmlAccessType.FIELD)
public class CustomPropertyProvider implements ICustomPropertyProvider {
  @XmlElement
  @XmlJavaTypeAdapter(CustomPropertyAdapter.class)
  private Map<String, ICustomProperty> properties;

  public CustomPropertyProvider() {
    this.properties = new Hashtable<>(); // use Hashtable because it rejects null keys and null values
  }

  /**
   * Copy Constructor for copying instances of CustomPropertyProviders.
   *
   * @param propertyProviderToBeCopied the PropertyProvider we want to copy
   */
  public CustomPropertyProvider(ICustomPropertyProvider propertyProviderToBeCopied) {
    this.properties = propertyProviderToBeCopied.getProperties().entrySet().stream()
      .collect(Collectors.toMap(Entry::getKey, e -> new CustomProperty((e.getValue()))));
  }

  @Override
  public Map<String, ICustomProperty> getProperties() {
    return this.properties;
  }

  @Override
  public boolean hasCustomProperty(String propertyName) {
    return this.getProperties().containsKey(propertyName);
  }

  @Override
  public ICustomProperty getProperty(String propertyName) {
    return this.getProperties().get(propertyName);
  }

  @Override
  public void setValue(String propertyName, ICustomProperty value) {
    if (value != null) {
      this.getProperties().put(propertyName, value);
    }
  }

  @Override
  public String getStringValue(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }
    return property.getAsString();
  }

  @Override
  public String getStringValue(String propertyName, String defaultValue) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      return defaultValue;
    }
    return property.getAsString();
  }


  @Override
  public char getCharValue(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }
    return property.getAsChar();
  }

  @Override
  public char getCharValue(String propertyName, char defaultValue) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      return defaultValue;
    }
    return property.getAsChar();
  }

  @Override
  public int getIntValue(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }
    return property.getAsInt();
  }

  @Override
  public int getIntValue(String propertyName, int defaultValue) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      return defaultValue;
    }
    return property.getAsInt();
  }

  @Override
  public long getLongValue(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }
    return property.getAsLong();
  }

  @Override
  public long getLongValue(String propertyName, long defaultValue) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      return defaultValue;
    }
    return property.getAsLong();
  }

  @Override
  public short getShortValue(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }
    return property.getAsShort();
  }

  @Override
  public short getShortValue(String propertyName, short defaultValue) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      return defaultValue;
    }
    return property.getAsShort();
  }

  @Override
  public byte getByteValue(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }
    return property.getAsByte();
  }

  @Override
  public byte getByteValue(String propertyName, byte defaultValue) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      return defaultValue;
    }
    return property.getAsByte();
  }

  @Override
  public boolean getBoolValue(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }
    return property.getAsBool();
  }

  @Override
  public boolean getBoolValue(String propertyName, boolean defaultValue) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      return defaultValue;
    }
    return property.getAsBool();
  }

  @Override
  public float getFloatValue(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }
    return property.getAsFloat();
  }

  @Override
  public float getFloatValue(String propertyName, float defaultValue) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      return defaultValue;
    }
    return property.getAsFloat();
  }

  @Override
  public double getDoubleValue(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }

    return property.getAsDouble();
  }

  @Override
  public double getDoubleValue(String propertyName, double defaultValue) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      return defaultValue;
    }

    return property.getAsDouble();
  }

  @Override
  public Color getColorValue(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }

    return property.getAsColor();
  }

  @Override
  public Color getColorValue(String propertyName, Color defaultValue) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      return defaultValue;
    }

    Color value = property.getAsColor();
    if (value == null) {
      return defaultValue;
    }

    return value;
  }

  @Override
  public <T extends Enum<T>> T getEnumValue(String propertyName, Class<T> enumType) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }

    return property.getAsEnum(enumType);
  }

  @Override
  public <T extends Enum<T>> T getEnumValue(String propertyName, Class<T> enumType, T defaultValue) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      return defaultValue;
    }

    T value = property.getAsEnum(enumType);
    if (value == null) {
      return defaultValue;
    }

    return value;
  }

  @Override
  public URL getFileValue(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }

    return property.getAsFile();
  }

  @Override
  public URL getFileValue(String propertyName, URL defaultValue) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      return defaultValue;
    }

    URL value = property.getAsFile();
    if (value == null) {
      return defaultValue;
    }

    return value;
  }

  @Override
  public int getMapObjectId(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }

    return property.getMapObjectId();
  }

  private ICustomProperty createPropertyIfAbsent(String propertyName) {
    return this.getProperties().computeIfAbsent(propertyName, n -> new CustomProperty());
  }

  @Override
  public void setValue(String propertyName, URL value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType(CustomPropertyType.FILE);
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, String value) {
    if (value != null) {
      ICustomProperty property = createPropertyIfAbsent(propertyName);
      property.setType(CustomPropertyType.STRING);
      property.setValue(value);
    } else {
      this.getProperties().remove(propertyName);
    }
  }

  @Override
  public void setValue(String propertyName, boolean value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType(CustomPropertyType.BOOL);
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, byte value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType(CustomPropertyType.INT);
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, short value) {
    setValue(propertyName, (int) value);
  }

  @Override
  public void setValue(String propertyName, char value) {
    setValue(propertyName, String.valueOf(value));
  }

  @Override
  public void setValue(String propertyName, int value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType(CustomPropertyType.INT);
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, long value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType(CustomPropertyType.INT);
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, float value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType(CustomPropertyType.FLOAT);
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, double value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType(CustomPropertyType.FLOAT);
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, Color value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType(CustomPropertyType.COLOR);
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, Enum<?> value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType(CustomPropertyType.STRING);
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, IMapObject value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType(CustomPropertyType.OBJECT);
    property.setValue(value.getId());
  }

  @Override
  public void setProperties(Map<String, ICustomProperty> props) {
    this.getProperties().clear();
    if (props != null) {
      this.getProperties().putAll(props);
    }
  }

  @Override
  public void removeProperty(String propertyName) {
    this.getProperties().remove(propertyName);
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    if (this.properties == null) {
      this.properties = new Hashtable<>();
    }
  }

  void finish(URL location) throws TmxException {
    // blank base case
  }

  @Override
  public List<String> getCommaSeparatedStringValues(String propertyName, String defaultValue) {
    List<String> values = new ArrayList<>();
    String valuesStr = this.getStringValue(propertyName, defaultValue);
    if (valuesStr != null && !valuesStr.isEmpty()) {
      for (String value : valuesStr.split(",")) {
        if (value != null) {
          values.add(value);
        }
      }
    }
    return values;
  }
}
