package com.litiengine.environment.tilemap;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum StaggerAxis {
  @XmlEnumValue("x")
  X,
  @XmlEnumValue("y")
  Y;
}
