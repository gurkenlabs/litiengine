package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import de.gurkenlabs.litiengine.util.ColorHelper;

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
