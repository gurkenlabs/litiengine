package de.gurkenlabs.litiengine.environment.tilemap.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;

@XmlAccessorType(XmlAccessType.FIELD)
public class Terrain extends CustomPropertyProvider implements ITerrain {
  private static final long serialVersionUID = 2890727446376641648L;

  @XmlAttribute
  private String name;

  @XmlAttribute
  private int tile;

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public int getTile() {
    return this.tile;
  }
}