package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * An adapter for converting between `String` and `Float` values during XML serialization and deserialization.
 *
 * <p>This implementation ensures that float values are serialized as integers if they have no digits
 * after the decimal point. For example:
 * <ul>
 *   <li>A float value of `5.0` will be serialized as `"5"`.</li>
 *   <li>A float value of `5.5` will remain serialized as `"5.5"`.</li>
 * </ul>
 */
public class DecimalFloatAdapter extends XmlAdapter<String, Float> {

  /**
   * Converts a `String` value from the XML into a `Float` object.
   *
   * @param v The string value to be converted.
   * @return The parsed `Float` value.
   * @throws Exception If the string cannot be parsed as a float.
   */
  @Override
  public Float unmarshal(String v) throws Exception {
    return Float.parseFloat(v);
  }

  /**
   * Converts a `Float` value into a `String` for XML serialization.
   *
   * <p>If the float value is `null`, this method returns `null`. If the float value has no digits
   * after the decimal point, it is serialized as an integer string. Otherwise, it is serialized as a standard float string.
   *
   * @param v The float value to be converted.
   * @return The string representation of the float value.
   * @throws Exception If an error occurs during conversion.
   */
  @Override
  public String marshal(Float v) throws Exception {
    if (v == null) {
      return null;
    }

    if (v % 1 == 0) {
      return Integer.toString(v.intValue());
    }

    return v.toString();
  }
}
