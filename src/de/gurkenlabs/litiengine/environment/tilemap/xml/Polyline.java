package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.IPolyline;

public class Polyline extends PolyShape implements IPolyline {
  public Polyline() {
    super();
  }

  public Polyline(IPolyline polyLineToBeCopied) {
    super(polyLineToBeCopied);
  }
}
