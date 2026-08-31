package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/// XML adapter that converts between a [String] representation and a generic [Number].
///
/// Used to serialize `Number`-typed fields of generic classes (e.g.
/// [de.gurkenlabs.litiengine.attributes.Attribute]) where the concrete numeric type cannot be
/// recovered at unmarshalling time. The adapter always returns a [Double] when the value
/// contains a decimal point (or scientific notation) and a [Long] otherwise. Consumers are
/// expected to convert the returned `Number` to the desired concrete type via
/// [Number#floatValue()], [Number#longValue()], etc.
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
