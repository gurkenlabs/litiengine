package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Terrain extends CustomPropertyProvider implements ITerrain {
  public static final int NONE = -1;

  @XmlAttribute private String name;

  @XmlAttribute private int tile;

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public int getTile() {
    return this.tile;
  }
}
