package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class WangTile {

  @XmlAttribute
  private int tileid;

  @XmlAttribute
  @XmlJavaTypeAdapter(IntegerArrayAdapter.class)
  private int[] wangid;

  public int getTileId() {
    return tileid;
  }

  public int[] getWangId() {
    return wangid;
  }
}
