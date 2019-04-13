package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.IPolygon;

public class Polygon extends PolyShape implements IPolygon {
  public Polygon() {
    super();
  }

  public Polygon(IPolygon polygonToBeCopied) {
    super(polygonToBeCopied);
  }
}
