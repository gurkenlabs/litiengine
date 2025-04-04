package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter class for converting between Boolean objects and their Integer representations
 * in XML using JAXB.
 */
public class BooleanIntegerAdapter extends XmlAdapter<Integer, Boolean> {

  /**
   * Converts an Integer value to a Boolean object.
   *
   * @param s the Integer value to be converted
   * @return the corresponding Boolean object, or null if the input is null
   */
  @Override
  public Boolean unmarshal(Integer s) {
    return s == null ? null : s == 1;
  }

  /**
   * Converts a Boolean object to its Integer representation.
   *
   * @param c the Boolean object to be converted
   * @return the corresponding Integer representation, or null if the input is null
   */
  @Override
  public Integer marshal(Boolean c) {
    if (c == null) {
      return null;
    }

    return c ? 1 : 0;
  }
}
