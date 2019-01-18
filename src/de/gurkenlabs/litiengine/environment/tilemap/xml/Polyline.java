package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.io.Serializable;

import de.gurkenlabs.litiengine.environment.tilemap.IPolyline;

public class Polyline extends Polyshape implements IPolyline, Serializable {
  private static final long serialVersionUID = 7468139198894325522L;

  public Polyline() {
    super();
  }

  public Polyline(IPolyline polyLineToBeCopied) {
    super(polyLineToBeCopied);
  }
}
