package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.util.List;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Property;

/**
 * The Interface ICustomPropertyProvider is providing methods to get and set custom properties.
 *
 */
public interface ICustomPropertyProvider {
  /**
   * Checks if a custom property with the given name is present.
   *
   * @param name
   *          the name of the custom property
   * @return true if a custom property with the given name is present. False otherwise.
   */
  boolean hasCustomProperty(String name);

  /**
   * Gets the string value of the custom property with the provided name.
   *
   * @param name
   *          the name of the custom property
   * @return the string value of the custom property
   */
  public String getStringProperty(String name);

  /**
   * Gets the string value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param name
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the string value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public String getStringProperty(String name, String defaultValue);

  /**
   * Gets the int value of the custom property with the provided name.
   *
   * @param name
   *          the name of the custom property
   * @return the int value of the custom property
   */
  public int getIntProperty(String name);

  /**
   * Gets the int value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param name
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the int value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public int getIntProperty(String name, int defaultValue);

  /**
   * Gets the long value of the custom property with the provided name.
   *
   * @param name
   *          the name of the custom property
   * @return the long value of the custom property
   */
  public long getLongProperty(String name);

  /**
   * Gets the long value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param name
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the long value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public long getLongProperty(String name, long defaultValue);

  /**
   * Gets the short value of the custom property with the provided name.
   *
   * @param name
   *          the name of the custom property
   * @return the short value of the custom property
   */
  public short getShortProperty(String name);

  /**
   * Gets the short value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param name
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the short value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public short getShortProperty(String name, short defaultValue);

  /**
   * Gets the byte value of the custom property with the provided name.
   *
   * @param name
   *          the name of the custom property
   * @return the byte value of the custom property
   */
  public byte getByteProperty(String name);

  /**
   * Gets the byte value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param name
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the byte value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public byte getByteProperty(String name, byte defaultValue);

  /**
   * Gets the boolean value of the custom property with the provided name.
   *
   * @param name
   *          the name of the custom property
   * @return the boolean value of the custom property
   */
  public boolean getBoolProperty(String name);

  /**
   * Gets the boolean value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param name
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the boolean value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public boolean getBoolProperty(String name, boolean defaultValue);

  /**
   * Gets the float value of the custom property with the provided name.
   *
   * @param name
   *          the name of the custom property
   * @return the float value of the custom property
   */
  public float getFloatProperty(String name);

  /**
   * Gets the float value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param name
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the float value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public float getFloatProperty(String name, float defaultValue);

  /**
   * Gets the double value of the custom property with the provided name.
   *
   * @param name
   *          the name of the custom property
   * @return the double value of the custom property
   */
  public double getDoubleProperty(String name);

  /**
   * Gets the double value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param name
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the double value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public double getDoubleProperty(String name, double defaultValue);

  /**
   * Gets the color value of the custom property with the provided name.
   *
   * @param name
   *          the name of the custom property
   * @return the color value of the custom property
   */
  public Color getColorProperty(String name);

  /**
   * Gets the color value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param name
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the color value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public Color getColorProperty(String name, Color defaultValue);

  /**
   * Gets the enum value of the custom property with the provided name.
   *
   * @param name
   *          the name of the custom property
   * @return the enum value of the custom property
   */
  public <T extends Enum<T>> T getEnumProperty(String name, Class<T> enumType);

  /**
   * Gets the enum value of the custom property with the provided name. If the value is null, the provided default value is used as a fallback.
   *
   * @param name
   *          the name of the custom property
   * @param defaultValue
   *          the fallback value in case the property value is null.
   * @return the enum value of the custom property, if present. Otherwise, the provided default value is returned.
   */
  public <T extends Enum<T>> T getEnumProperty(String name, Class<T> enumType, T defaultValue);

  /**
   * Sets the value for the custom property with the given name to the given string.
   *
   * @param name
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setProperty(String name, String value);

  /**
   * Sets the value for the custom property with the given name to the given boolean.
   *
   * @param name
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setProperty(String name, boolean value);

  /**
   * Sets the value for the custom property with the given name to the given byte.
   *
   * @param name
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setProperty(String name, byte value);

  /**
   * Sets the value for the custom property with the given name to the given short.
   *
   * @param name
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setProperty(String name, short value);

  /**
   * Sets the value for the custom property with the given name to the given int.
   *
   * @param name
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setProperty(String name, int value);

  /**
   * Sets the value for the custom property with the given name to the given long.
   *
   * @param name
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setProperty(String name, long value);

  /**
   * Sets the value for the custom property with the given name to the given float.
   *
   * @param name
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setProperty(String name, float value);

  /**
   * Sets the value for the custom property with the given name to the given double.
   *
   * @param name
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setProperty(String name, double value);

  /**
   * Sets the value for the custom property with the given name to the given color.
   *
   * @param name
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setProperty(String name, Color value);

  /**
   * Sets the value for the custom property with the given name to the given enum.
   *
   * @param name
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public <T extends Enum<T>> void setProperty(String name, T value);

  /**
   * Sets the value for the custom property with the given name to the given string value.
   *
   * @param name
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public List<Property> getCustomProperties();

  /**
   * Sets the value for the custom property with the given name to the given string value.
   *
   * @param name
   *          the name of the custom property
   * @param value
   *          the new value
   */
  public void setCustomProperties(List<Property> props);
}
