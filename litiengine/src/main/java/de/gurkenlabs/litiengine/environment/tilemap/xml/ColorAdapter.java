package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.util.ColorHelper;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.awt.Color;

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
