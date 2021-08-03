package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.util.ColorHelper;
import java.awt.Color;
import javax.xml.bind.annotation.adapters.XmlAdapter;

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
