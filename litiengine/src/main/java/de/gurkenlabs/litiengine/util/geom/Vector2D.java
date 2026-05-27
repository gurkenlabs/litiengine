package de.gurkenlabs.litiengine.util.geom;

import java.awt.geom.Point2D;

/**
 * A simple mutable 2D vector with basic linear algebra operations (addition, subtraction, scaling, dot product, length and unit/normal vector).
 */
public class Vector2D {

  /**
   * The x component of the vector.
   */
  protected double dX;
  /**
   * The y component of the vector.
   */
  protected double dY;

  /**
   * Creates a new zero vector.
   */
  public Vector2D() {
    this.dX = this.dY = 0.0;
  }

  /**
   * Creates a new vector with the supplied components.
   *
   * @param dX the x component
   * @param dY the y component
   */
  public Vector2D(final double dX, final double dY) {
    this.dX = dX;
    this.dY = dY;
  }

  /**
   * Creates a new vector pointing from {@code p1} to {@code p2}.
   *
   * @param p1 the start point
   * @param p2 the end point
   */
  public Vector2D(final Point2D p1, final Point2D p2) {
    this.dX = p2.getX() - p1.getX();
    this.dY = p2.getY() - p1.getY();
  }

  /**
   * Adds another vector to this one and returns the resulting new vector.
   *
   * @param v1 the vector to add
   * @return the sum vector
   */
  public Vector2D add(final Vector2D v1) {
    return new Vector2D(this.dX + v1.dX, this.dY + v1.dY);
  }

  /**
   * Computes the dot product of this vector with another.
   *
   * @param v1 the other vector
   * @return the dot product
   */
  public double dotProduct(final Vector2D v1) {
    return this.dX * v1.dX + this.dY * v1.dY;
  }

  /**
   * Returns the x component of the vector.
   *
   * @return the x component
   */
  public double getX() {
    return this.dX;
  }

  /**
   * Returns the y component of the vector.
   *
   * @return the y component
   */
  public double getY() {
    return this.dY;
  }

  /**
   * Returns the Euclidean length of the vector.
   *
   * @return the length
   */
  public double length() {
    return Math.sqrt(this.dX * this.dX + this.dY * this.dY);
  }

  /**
   * Returns a vector perpendicular to this one, obtained by rotating it 90 degrees clockwise.
   *
   * @return the perpendicular vector
   */
  public Vector2D normalVector() {
    return new Vector2D(this.getY(), -this.getX());
  }

  /**
   * Returns this vector scaled by the supplied factor.
   *
   * @param scaleFactor the scale factor
   * @return the scaled vector
   */
  public Vector2D scale(final double scaleFactor) {
    return new Vector2D(this.dX * scaleFactor, this.dY * scaleFactor);
  }

  /**
   * Subtracts another vector from this one and returns the resulting new vector.
   *
   * @param v1 the vector to subtract
   * @return the difference vector
   */
  public Vector2D sub(final Vector2D v1) {
    return new Vector2D(this.dX - v1.dX, this.dY - v1.dY);
  }

  @Override
  public String toString() {
    return "Vector2D(" + this.dX + ", " + this.dY + ")";
  }

  /**
   * Returns the unit vector pointing in the same direction as this vector, or the zero vector if this vector has length zero.
   *
   * @return the unit vector
   */
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
