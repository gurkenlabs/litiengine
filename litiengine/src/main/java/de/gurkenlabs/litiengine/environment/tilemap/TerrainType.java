package de.gurkenlabs.litiengine.environment.tilemap;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum TerrainType {
  @XmlEnumValue("corner")
  CORNER,

  @XmlEnumValue("edge")
  EDGE,

  @XmlEnumValue("mixed")
  MIXED
}
