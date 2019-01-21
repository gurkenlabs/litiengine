package de.gurkenlabs.litiengine.environment.tilemap.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import de.gurkenlabs.litiengine.environment.tilemap.ITileOffset;

@XmlAccessorType(XmlAccessType.FIELD)
public class TileOffset extends CustomPropertyProvider implements ITileOffset {

  @XmlAttribute
  private int x;

  @XmlAttribute
  private int y;

  @Override
  public int getX() {
    return this.x;
  }

  @Override
  public int getY() {
    return this.y;
  }
}