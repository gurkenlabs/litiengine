package de.gurkenlabs.util.geom;

import java.awt.geom.Point2D;

public class Vector2D {

  protected double dX;
  protected double dY;

  public Vector2D() {
    this.dX = this.dY = 0.0;
  }

  public Vector2D(final double dX, final double dY) {
    this.dX = dX;
    this.dY = dY;
  }

  public Vector2D(final Point2D p1, final Point2D p2) {
    this.dX = p2.getX() - p1.getX();
    this.dY = p2.getY() - p1.getY();
  }

  public Vector2D add(final Vector2D v1) {
    final Vector2D v2 = new Vector2D(this.dX + v1.dX, this.dY + v1.dY);
    return v2;
  }

  public double dotProduct(final Vector2D v1) {
    return this.dX * v1.dX + this.dY * v1.dY;
  }

  public double getX() {
    return this.dX;
  }

  public double getY() {
    return this.dY;
  }

  public double length() {
    return Math.sqrt(this.dX * this.dX + this.dY * this.dY);
  }

  public Vector2D normalVector() {
    final Vector2D norm = new Vector2D(this.getY(), -this.getX());
    return norm;
  }

  public Vector2D scale(final double scaleFactor) {
    final Vector2D v2 = new Vector2D(this.dX * scaleFactor, this.dY * scaleFactor);
    return v2;
  }

  public Vector2D sub(final Vector2D v1) {
    final Vector2D v2 = new Vector2D(this.dX - v1.dX, this.dY - v1.dY);
    return v2;
  }

  @Override
  public String toString() {
    return "Vector2D(" + this.dX + ", " + this.dY + ")";
  }

  public Vector2D unitVector() {
    final Vector2D unit = new Vector2D();
    final double length = this.length();
    if (length != 0) {
      unit.dX = this.dX / length;
      unit.dY = this.dY / length;
    }

    return unit;
  }
}
