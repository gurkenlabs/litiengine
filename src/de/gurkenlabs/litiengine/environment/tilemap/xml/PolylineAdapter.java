package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class PolylineAdapter extends XmlAdapter<String, List<Point2D>> {
  @Override
  public List<Point2D> unmarshal(String v) {
    String[] rawPoints = v.split(" ");
    List<Point2D> points = new ArrayList<>(rawPoints.length);
    for (int i = 0; i < rawPoints.length; i++) {
      String[] coord = rawPoints[i].split(",", 2);
      if (coord.length < 2) {
        throw new IllegalArgumentException("invalid coordinate pair: " + rawPoints[i]);
      }
      points.add(new Point2D.Float(Float.parseFloat(coord[0]), Float.parseFloat(coord[1])));
    }
    return points;
  }

  @Override
  public String marshal(List<Point2D> v) {
    Iterator<Point2D> iter = v.iterator();
    if (!iter.hasNext()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    Point2D p = iter.next();
    sb.append(save(p.getX()) + "," + save(p.getY()));
    while (iter.hasNext()) {
      p = iter.next();
      sb.append(" " + save(p.getX()) + ',' + save(p.getY()));
    }
    return sb.toString();
  }

  private static String save(double d) {
    if (d % 1.0 == 0.0 && d >= Long.MIN_VALUE && d <= Long.MAX_VALUE) {
      return Long.toString((long) d);
    } else {
      return Double.toString(d);
    }
  }
}
