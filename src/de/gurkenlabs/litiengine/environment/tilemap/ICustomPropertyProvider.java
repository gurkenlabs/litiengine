package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.util.List;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Property;

public interface ICustomPropertyProvider {
  boolean hasCustomProperty(String name);

  /**
   * Gets the custom property.
   *
   * @param name
   *          the name
   * @return the custom property
   */
  public String getString(String name);

  public String getString(String name, String defaultValue);

  public int getInt(String name);

  public int getInt(String name, int defaultValue);

  public long getLong(String name);

  public long getLong(String name, long defaultValue);

  public short getShort(String name);

  public short getShort(String name, short defaultValue);

  public byte getByte(String name);

  public byte getByte(String name, byte defaultValue);

  public boolean getBool(String name);

  public boolean getBool(String name, boolean defaultValue);

  public float getFloat(String name);

  public float getFloat(String name, float defaultValue);

  public double getDouble(String name);

  public double getDouble(String name, double defaultValue);

  public Color getColor(String name);

  public Color getColor(String name, Color defaultValue);

  public <T extends Enum<T>> T getEnum(String name, Class<T> enumType);

  public <T extends Enum<T>> T getEnum(String name, Class<T> enumType, T defaultValue);

  public void setCustomProperty(String name, String value);

  public List<Property> getCustomProperties();

  public void setCustomProperties(List<Property> props);
}
