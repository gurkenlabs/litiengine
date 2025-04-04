package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import de.gurkenlabs.litiengine.util.ColorHelper;

/**
 * Adapter class for converting between Color objects and their String representations
 * in XML using JAXB.
 */
public class ColorAdapter extends XmlAdapter<String, Color> {

  /**
   * Converts a String value to a Color object.
   *
   * @param v the String value to be converted
   * @return the corresponding Color object
   */
  @Override
  public Color unmarshal(String v) {
    return ColorHelper.decode(v);
  }

  /**
   * Converts a Color object to its String representation.
   *
   * @param v the Color object to be converted
   * @return the corresponding String representation
   */
  @Override
  public String marshal(Color v) {
    return ColorHelper.encode(v);
  }
}
