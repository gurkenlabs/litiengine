package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;

@XmlAccessorType(XmlAccessType.FIELD)
public class CustomPropertyProvider implements ICustomPropertyProvider, Serializable {
  private static final long serialVersionUID = 5564165255132042594L;

  @XmlElement
  @XmlJavaTypeAdapter(CustomPropertyAdapter.class)
  private Map<String, ICustomProperty> properties;

  public CustomPropertyProvider() {
    this.properties = new Hashtable<>(); // use Hashtable because it rejects null keys and null values
  }

  /**
   * Copy Constructor for copying instances of CustomPropertyProviders.
   *
   * @param propertyProviderToBeCopied
   *          the PropertyProvider we want to copy
   */
  public CustomPropertyProvider(CustomPropertyProvider propertyProviderToBeCopied) {
    this.properties = new Hashtable<>(propertyProviderToBeCopied.getProperties());
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
  public String getTypeOfProperty(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }
    return property.getType();
  }

  @Override
  public void setTypeOfProperty(String propertyName, String type) {
    this.getProperty(propertyName).setType(type);
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
    return property.getAsColor();
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
    return property.getAsEnum(enumType);
  }

  private ICustomProperty createPropertyIfAbsent(String propertyName) {
    return this.getProperties().computeIfAbsent(propertyName, n -> new CustomProperty());
  }

  @Override
  public void setValue(String propertyName, String value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType("string");
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, boolean value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType("bool");
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, byte value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType("int");
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, short value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType("int");
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, int value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType("int");
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, long value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType("int");
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, float value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType("float");
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, double value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType("float");
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, Color value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType("color");
    property.setValue(value);
  }

  @Override
  public void setValue(String propertyName, Enum<?> value) {
    ICustomProperty property = createPropertyIfAbsent(propertyName);
    property.setType("string");
    property.setValue(value);
  }

  @Override
  public void setProperties(Map<String, ICustomProperty> props) {
    this.getProperties().clear();
    if (props != null) {
      this.getProperties().putAll(props);
    }
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    if (this.properties == null) {
      this.properties = new Hashtable<>();
    }
  }
}
