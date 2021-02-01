package com.litiengine.environment.tilemap.xml;

import java.awt.Color;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.litiengine.util.ColorHelper;

public class ColorAdapter extends XmlAdapter<String, Color> {

  @Override
  public Color unmarshal(String v) {
    return ColorHelper.decode(v);
  }

  @Override
  public String marshal(Color v) {
    return ColorHelper.encode(v);
  }
}
