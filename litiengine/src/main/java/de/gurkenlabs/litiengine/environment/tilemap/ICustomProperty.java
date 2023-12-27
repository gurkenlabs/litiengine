package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.net.URL;

/**
 * Represents a custom property on a map element.
 */
public interface ICustomProperty {

  void setValue(URL value);

  void setValue(String value);

  void setValue(char value);

  void setValue(Enum<?> value);

  void setValue(long value);

  void setValue(double value);

  // no methods for setting to byte, short, int, or float because they will already be accepted as a
  // long or double

  void setValue(boolean value);

  void setValue(Color value);

  String getAsString();

  char getAsChar();

  boolean getAsBool();

  Color getAsColor();

  float getAsFloat();

  double getAsDouble();

  byte getAsByte();

  short getAsShort();

  int getAsInt();

  long getAsLong();

  <T extends Enum<T>> T getAsEnum(Class<T> enumType);

  URL getAsFile();

  int getMapObjectId();

  String getType();

  void setType(String type);

  /**
   * Tests for equality between two custom properties. Two custom properties are <i>equal</i> if they both have the same
   * type and string value.
   *
   * @param anObject
   *          The custom property to test equality for
   * @return Whether the two custom properties are equal, or false if {@code anObject} is not a custom property
   */
  boolean equals(Object anObject);

  /**
   * Returns the hash code for this custom property. The hash code for a custom property is equal to its type's hash code
   * times 31 plus its value's hash code.
   *
   * @return The hash code for this custom property
   */
  int hashCode();
}
