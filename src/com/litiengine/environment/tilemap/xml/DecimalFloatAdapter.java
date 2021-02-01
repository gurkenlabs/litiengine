package com.litiengine.environment.tilemap.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * This adapter implementation ensures that the float value is serialized
 * like an integer when it has no digits behind the decimal point.
 */
public class DecimalFloatAdapter extends XmlAdapter<String, Float> {

  @Override
  public Float unmarshal(String v) throws Exception {
    return Float.parseFloat(v);
  }

  @Override
  public String marshal(Float v) throws Exception {
    if (v == null) {
      return null;
    }

    if (v.floatValue() % 1 == 0) {
      return Integer.toString(v.intValue());
    }

    return v.toString();
  }
}
