package de.gurkenlabs.util.geom;

import java.awt.geom.Point2D;

import java.lang.Math;

public class Vector2D {

  protected double dX;
  protected double dY;

  public Vector2D() {
    dX = dY = 0.0;
  }

  public Vector2D(double dX, double dY) {
    this.dX = dX;
    this.dY = dY;
  }

  public Vector2D(Point2D p1, Point2D p2) {
    this.dX = p2.getX() - p1.getX();
    this.dY = p2.getY() - p1.getY();
  }

  public double getX() {
    return this.dX;
  }

  public double getY() {
    return this.dY;
  }

  public String toString() {
    return "Vector2D(" + dX + ", " + dY + ")";
  }

  public double length() {
    return Math.sqrt(dX * dX + dY * dY);
  }

  public Vector2D add(Vector2D v1) {
    Vector2D v2 = new Vector2D(this.dX + v1.dX, this.dY + v1.dY);
    return v2;
  }

  public Vector2D sub(Vector2D v1) {
    Vector2D v2 = new Vector2D(this.dX - v1.dX, this.dY - v1.dY);
    return v2;
  }

  public Vector2D scale(double scaleFactor) {
    Vector2D v2 = new Vector2D(this.dX * scaleFactor, this.dY * scaleFactor);
    return v2;
  }

  public Vector2D normalVector() {
    Vector2D norm = new Vector2D(this.getY(), -this.getX());
    return norm;
  }

  public Vector2D unitVector() {
    Vector2D unit = new Vector2D();
    double length = this.length();
    if (length != 0) {
      unit.dX = this.dX / length;
      unit.dY = this.dY / length;
    }

    return unit;
  }

  public double dotProduct(Vector2D v1) {
    return this.dX * v1.dX + this.dY * v1.dY;
  }
}
