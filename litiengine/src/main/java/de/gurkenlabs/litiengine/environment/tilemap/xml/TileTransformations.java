package de.gurkenlabs.litiengine.environment.tilemap.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "transformations")
public class TileTransformations {
  public TileTransformations() {
    //  default no-args constructor
  }

  public TileTransformations(TileTransformations other) {
    this.hflip = other.hflip;
    this.vflip = other.vflip;
    this.rotate = other.rotate;
    this.preferuntransformed = other.preferuntransformed;
  }

  @XmlAttribute
  private boolean hflip;

  @XmlAttribute
  private boolean vflip;

  @XmlAttribute
  private boolean rotate;

  @XmlAttribute
  private boolean preferuntransformed;

  public boolean isHflip() {
    return this.hflip;
  }

  public boolean isVflip() {
    return this.vflip;
  }

  public boolean isRotate() {
    return this.rotate;
  }

  public boolean isPreferuntransformed() {
    return this.preferuntransformed;
  }
}
