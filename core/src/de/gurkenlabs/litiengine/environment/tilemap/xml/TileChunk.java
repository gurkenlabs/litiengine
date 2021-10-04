package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlValue;

/**
 * This class represents a chunk of tiles in an infinite map.
 */
@XmlRootElement(name = "chunk")
public class TileChunk implements Comparable<TileChunk> {
  @XmlAttribute
  private int x;

  @XmlAttribute
  private int y;

  @XmlAttribute
  private int width;

  @XmlAttribute
  private int height;

  @XmlValue
  private String value;

  @XmlTransient
  public String getValue() {
    return this.value;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }

  @Override
  public int compareTo(TileChunk o) {
    if (this.getY() != o.getY()) {
      return Integer.compare(this.getY(), o.getY());
    }

    if (this.getX() != o.getX()) {
      return Integer.compare(this.getX(), o.getX());
    }

    return 0;
  }
}
