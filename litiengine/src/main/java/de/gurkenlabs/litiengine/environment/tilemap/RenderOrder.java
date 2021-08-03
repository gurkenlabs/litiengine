package de.gurkenlabs.litiengine.environment.tilemap;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum RenderOrder {
  @XmlEnumValue("right-down")
  RIGHT_DOWN(false, false),
  @XmlEnumValue("right-up")
  RIGHT_UP(false, true),
  @XmlEnumValue("left-down")
  LEFT_DOWN(true, false),
  @XmlEnumValue("left-up")
  LEFT_UP(true, true);

  public final boolean rtl;
  public final boolean btt;

  private RenderOrder(boolean rtl, boolean btt) {
    this.rtl = rtl;
    this.btt = btt;
  }
}
