package de.gurkenlabs.litiengine.environment.tilemap;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum StaggerIndex {
  @XmlEnumValue("odd")
  ODD,
  @XmlEnumValue("even")
  EVEN;
}
