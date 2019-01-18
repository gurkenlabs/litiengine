package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.IPolygon;

public class Polygon extends Polyshape implements IPolygon {
  private static final long serialVersionUID = -1461074704865254436L;

  public Polygon() {
    super();
  }

  public Polygon(IPolygon polygonToBeCopied) {
    super(polygonToBeCopied);
  }
}
