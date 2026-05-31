package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * XML adapter that converts between a {@link String} representation and a generic {@link Number}.
 * <p>
 * Used to serialize {@code Number}-typed fields of generic classes (e.g.
 * {@link de.gurkenlabs.litiengine.attributes.Attribute}) where the concrete numeric type cannot be
 * recovered at unmarshalling time. The adapter always returns a {@link Double} when the value
 * contains a decimal point (or scientific notation) and a {@link Long} otherwise. Consumers are
 * expected to convert the returned {@code Number} to the desired concrete type via
 * {@link Number#floatValue()}, {@link Number#longValue()}, etc.
 * </p>
 */
public class NumberAdapter extends XmlAdapter<String, Number> {

  @Override
  public String marshal(Number value) {
    return value == null ? null : value.toString();
  }

  @Override
  public Number unmarshal(String value) {
    if (value == null || value.isEmpty()) {
      return null;
    }
    if (value.indexOf('.') >= 0 || value.indexOf('e') >= 0 || value.indexOf('E') >= 0) {
      return Double.parseDouble(value);
    }
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException nfe) {
      return Double.parseDouble(value);
    }
  }
}
