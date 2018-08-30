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
  public String getCustomProperty(String name);

  public String getCustomProperty(String name, String defaultValue);

  public int getCustomPropertyInt(String name);

  public int getCustomPropertyInt(String name, int defaultValue);

  public long getCustomPropertyLong(String name);

  public long getCustomPropertyLong(String name, long defaultValue);

  public short getCustomPropertyShort(String name);

  public short getCustomPropertyShort(String name, short defaultValue);

  public byte getCustomPropertyByte(String name);

  public byte getCustomPropertyByte(String name, byte defaultValue);

  public boolean getCustomPropertyBool(String name);

  public boolean getCustomPropertyBool(String name, boolean defaultValue);

  public float getCustomPropertyFloat(String name);

  public float getCustomPropertyFloat(String name, float defaultValue);

  public double getCustomPropertyDouble(String name);

  public double getCustomPropertyDouble(String name, double defaultValue);

  public Color getCustomPropertyColor(String name);

  public Color getCustomPropertyColor(String name, Color defaultValue);

  public <T extends Enum<T>> T getCustomPropertyEnum(String name, Class<T> enumType);

  public <T extends Enum<T>> T getCustomPropertyEnum(String name, Class<T> enumType, T defaultValue);

  public void setCustomProperty(String name, String value);

  public List<Property> getCustomProperties();

  public void setCustomProperties(List<Property> props);
}
