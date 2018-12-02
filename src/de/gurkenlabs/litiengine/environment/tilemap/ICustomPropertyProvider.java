package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * The Interface ICustomPropertyProvider is providing methods to get and set custom properties.
 */
public interface ICustomPropertyProvider {
  /**
   * Checks if a custom property with the given name is present.
   *
   * @param propertyName
   *          the name of the custom property
   * @return true if a custom property with the given name is present. False otherwise.
   */
  public boolean hasCustomProperty(String propertyName);

  public String getTypeOfProperty(String propertyName);

  public void setTypeOfProperty(String propertyName, String type);

  public ICustomProperty getProperty(String propertyName);

  public void setValue(String propertyName, ICustomProperty value);

  /**
   * Gets the string value of the custom property with the provided name.
   *
   * @param propertyName
   *          the name of the custom property
   * @return the string value of the custom property
   * @throws NoSuchElementException if the custom property does not exist
   */
  public String getStringValue(String propertyName);

  /**
   * Gets the string value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param propertyName
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the string value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public String getStringValue(String propertyName, String defaultValue);

  /**
   * Gets the int value of the custom property with the provided name.
   *
   * @param propertyName
   *          the name of the custom property
   * @return the int value of the custom property
   * @throws NoSuchElementException if the custom property does not exist
   * @throws NumberFormatException if the custom property is not an integer or is not in range for an {@code int}
   */
  public int getIntValue(String propertyName);

  /**
   * Gets the int value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param propertyName
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the int value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public int getIntValue(String propertyName, int defaultValue);

  /**
   * Gets the long value of the custom property with the provided name.
   *
   * @param propertyName
   *          the name of the custom property
   * @return the long value of the custom property
   * @throws NoSuchElementException if the custom property does not exist
   * @throws NumberFormatException if the custom property is not an integer or is not in range for a {@code long}
   */
  public default long getLongValue(String propertyName) {
    ICustomProperty property = this.getProperty(propertyName);
    if (property == null) {
      throw new NoSuchElementException(propertyName);
    }
    return property.getAsLong();
  }

  /**
   * Gets the long value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param propertyName
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the long value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public long getLongValue(String propertyName, long defaultValue);

  /**
   * Gets the short value of the custom property with the provided name.
   *
   * @param propertyName
   *          the name of the custom property
   * @return the short value of the custom property
   * @throws NoSuchElementException if the custom property does not exist
   * @throws NumberFormatException if the custom property is not an integer or is out of range for a {@code short}
   */
  public short getShortValue(String propertyName);

  /**
   * Gets the short value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param propertyName
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the short value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public short getShortValue(String propertyName, short defaultValue);

  /**
   * Gets the byte value of the custom property with the provided name.
   *
   * @param propertyName
   *          the name of the custom property
   * @return the byte value of the custom property
   * @throws NoSuchElementException if the custom property does not exist
   * @throws NumberFormatException if the custom property is not an integer or is out of range for a {@code byte}
   */
  public byte getByteValue(String propertyName);

  /**
   * Gets the byte value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param propertyName
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the byte value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public byte getByteValue(String propertyName, byte defaultValue);

  /**
   * Gets the boolean value of the custom property with the provided name.
   *
   * @param propertyName
   *          the name of the custom property
   * @return the boolean value of the custom property
   * @throws NoSuchElementException if the custom property does not exist
   * @throws NumberFormatException if the custom property is not a {@code boolean} value
   */
  public boolean getBoolValue(String propertyName);

  /**
   * Gets the boolean value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param propertyName
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the boolean value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public boolean getBoolValue(String propertyName, boolean defaultValue);

  /**
   * Gets the float value of the custom property with the provided name.
   *
   * @param propertyName
   *          the name of the custom property
   * @return the float value of the custom property
   * @throws NoSuchElementException if the custom property does not exist
   * @throws NumberFormatException if the custom property is not a number
   */
  public float getFloatValue(String propertyName);

  /**
   * Gets the float value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param propertyName
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the float value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public float getFloatValue(String propertyName, float defaultValue);

  /**
   * Gets the double value of the custom property with the provided name.
   *
   * @param propertyName
   *          the name of the custom property
   * @return the double value of the custom property
   * @throws NoSuchElementException if the custom property does not exist
   * @throws NumberFormatException if the custom property is not a number
   */
  public double getDoubleValue(String propertyName);

  /**
   * Gets the double value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param propertyName
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the double value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public double getDoubleValue(String propertyName, double defaultValue);

  /**
   * Gets the color value of the custom property with the provided name.
   *
   * @param propertyName
   *          the name of the custom property
   * @return the color value of the custom property
   * @throws NoSuchElementException if the custom property does not exist
   */
  public Color getColorValue(String propertyName);

  /**
   * Gets the color value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param propertyName
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the color value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public Color getColorValue(String propertyName, Color defaultValue);

  /**
   * Gets the enum value of the custom property with the provided name.
   *
   * @param propertyName
   *          the name of the custom property
   * @param enumType
   *          a {@code Class} object for {@code <T>}
   * @param <T>
   *          the enum type to use
   * @return the enum value of the custom property
   * @throws NoSuchElementException if the custom property does not exist
   */
  public <T extends Enum<T>> T getEnumValue(String propertyName, Class<T> enumType);

  /**
   * Gets the enum value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param propertyName
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @param enumType
   *          a {@code Class} object for {@code <T>}
   * @param <T>
   *          the enum type to use
   * @return the enum value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public <T extends Enum<T>> T getEnumValue(String propertyName, Class<T> enumType, T defaultValue);

  /**
   * Sets the value for the custom property with the given name to the given string.
   *
   * @param propertyName
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setValue(String propertyName, String value);

  /**
   * Sets the value for the custom property with the given name to the given boolean.
   *
   * @param propertyName
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setValue(String propertyName, boolean value);

  /**
   * Sets the value for the custom property with the given name to the given byte.
   *
   * @param propertyName
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setValue(String propertyName, byte value);

  /**
   * Sets the value for the custom property with the given name to the given short.
   *
   * @param propertyName
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setValue(String propertyName, short value);

  /**
   * Sets the value for the custom property with the given name to the given int.
   *
   * @param propertyName
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setValue(String propertyName, int value);

  /**
   * Sets the value for the custom property with the given name to the given long.
   *
   * @param propertyName
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setValue(String propertyName, long value);

  /**
   * Sets the value for the custom property with the given name to the given float.
   *
   * @param propertyName
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setValue(String propertyName, float value);

  /**
   * Sets the value for the custom property with the given name to the given double.
   *
   * @param propertyName
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setValue(String propertyName, double value);

  /**
   * Sets the value for the custom property with the given name to the given color.
   *
   * @param propertyName
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setValue(String propertyName, Color value);

  /**
   * Sets the value for the custom property with the given name to the given enum.
   *
   * @param propertyName
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setValue(String propertyName, Enum<?> value);

  /**
   * Returns a {@code Map} view of the custom properties for this {@code ICustomPropertyProvider}.
   *
   * @return a {@code Map} view of the custom properties for this {@code ICustomPropertyProvider}
   */
  public Map<String, ICustomProperty> getProperties();

  /**
   * Sets all of the custom properties on this object to the provided values. Properties are added
   * when they only exist in the provided properties, and deleted when they only exist in the current
   * properties.
   *
   * @param props
   *          the new list of properties
   */
  public void setProperties(Map<String, ICustomProperty> props);
}
