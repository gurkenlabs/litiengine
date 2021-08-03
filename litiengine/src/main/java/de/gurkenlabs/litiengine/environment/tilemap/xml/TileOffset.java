package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import de.gurkenlabs.litiengine.environment.tilemap.ITileOffset;

@XmlAccessorType(XmlAccessType.FIELD)
public class TileOffset implements ITileOffset {

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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ITileOffset)) {
      return false;
    }
    ITileOffset other = (ITileOffset) obj;
    return this.getX() == other.getX() && this.getY() == other.getY();
  }

  @Override
  public int hashCode() {
    return this.getX() + this.getY() << 16 + this.getY() >>> 16;
  }

  @Override
  public String toString() {
    return "tile offset: (" + this.getX() + ',' + this.getY() + ')';
  }
}
