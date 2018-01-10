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

  public boolean getCustomPropertyBool(String name);

  public float getCustomPropertyFloat(String name);

  public double getCustomPropertyDouble(String name);

  public void setCustomProperty(String name, String value);

  public List<Property> getAllCustomProperties();

  public void setCustomProperties(List<Property> props);
}
