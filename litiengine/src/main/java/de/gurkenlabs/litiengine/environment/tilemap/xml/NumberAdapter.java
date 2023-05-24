package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.text.NumberFormat;

public class NumberAdapter extends XmlAdapter<String, Number> {

  @Override
  public String marshal(Number value) throws Exception {
    return value == null ? "" : value.toString();
  }

  @Override
  public Number unmarshal(String value) throws Exception {
    if (value == null || value.isEmpty()) {
      return null;
    }
    return NumberFormat.getInstance().parse(value);
  }
}
