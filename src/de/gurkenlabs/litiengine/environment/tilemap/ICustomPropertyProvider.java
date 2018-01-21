package de.gurkenlabs.litiengine.environment.tilemap;

import java.util.List;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Property;

public interface ICustomPropertyProvider {

  /**
   * Gets the custom property.
   *
   * @param name
   *          the name
   * @return the custom property
   */
  public String getCustomProperty(String name);

  public int getCustomPropertyInt(String name);

  public int getCustomPropertyInt(String name, int defaultValue);

  public boolean getCustomPropertyBool(String name);

  public boolean getCustomPropertyBool(String name, boolean defaultValue);

  public float getCustomPropertyFloat(String name);

  public float getCustomPropertyFloat(String name, float defaultValue);

  public double getCustomPropertyDouble(String name);

  public double getCustomPropertyDouble(String name, double defaultValue);

  public void setCustomProperty(String name, String value);

  public List<Property> getAllCustomProperties();

  public void setCustomProperties(List<Property> props);
}
