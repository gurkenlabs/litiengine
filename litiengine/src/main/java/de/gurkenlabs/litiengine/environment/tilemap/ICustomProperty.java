package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.net.URL;

/**
 * The {@code ICustomProperty} interface defines methods for managing custom properties with various data types. It provides functionality to set and
 * retrieve property values in different formats, as well as methods for type and equality handling.
 */
public interface ICustomProperty {

  /**
   * Sets the value of the property as a {@link URL}.
   *
   * @param value the {@link URL} value to set
   */
  void setValue(URL value);

  /**
   * Sets the value of the property as a {@link String}.
   *
   * @param value the {@link String} value to set
   */
  void setValue(String value);

  /**
   * Sets the value of the property as a {@code char}.
   *
   * @param value the {@code char} value to set
   */
  void setValue(char value);

  /**
   * Sets the value of the property as an {@link Enum}.
   *
   * @param value the {@link Enum} value to set
   */
  void setValue(Enum<?> value);

  /**
   * Sets the value of the property as a {@code long}.
   *
   * @param value the {@code long} value to set
   */
  void setValue(long value);

  /**
   * Sets the value of the property as a {@code double}.
   *
   * @param value the {@code double} value to set
   */
  void setValue(double value);

  /**
   * Sets the value of the property as a {@code boolean}.
   *
   * @param value the {@code boolean} value to set
   */
  void setValue(boolean value);

  /**
   * Sets the value of the property as a {@link Color}.
   *
   * @param value the {@link Color} value to set
   */
  void setValue(Color value);

  /**
   * Retrieves the value of the property as a {@link String}.
   *
   * @return the property value as a {@link String}
   */
  String getAsString();

  /**
   * Retrieves the value of the property as a {@code char}.
   *
   * @return the property value as a {@code char}
   */
  char getAsChar();

  /**
   * Retrieves the value of the property as a {@code boolean}.
   *
   * @return the property value as a {@code boolean}
   */
  boolean getAsBool();

  /**
   * Retrieves the value of the property as a {@link Color}.
   *
   * @return the property value as a {@link Color}
   */
  Color getAsColor();

  /**
   * Retrieves the value of the property as a {@code float}.
   *
   * @return the property value as a {@code float}
   */
  float getAsFloat();

  /**
   * Retrieves the value of the property as a {@code double}.
   *
   * @return the property value as a {@code double}
   */
  double getAsDouble();

  /**
   * Retrieves the value of the property as a {@code byte}.
   *
   * @return the property value as a {@code byte}
   */
  byte getAsByte();

  /**
   * Retrieves the value of the property as a {@code short}.
   *
   * @return the property value as a {@code short}
   */
  short getAsShort();

  /**
   * Retrieves the value of the property as an {@code int}.
   *
   * @return the property value as an {@code int}
   */
  int getAsInt();

  /**
   * Retrieves the value of the property as a {@code long}.
   *
   * @return the property value as a {@code long}
   */
  long getAsLong();

  /**
   * Retrieves the value of the property as an {@link Enum}.
   *
   * @param <T>      the type of the {@link Enum}
   * @param enumType the {@link Class} of the {@link Enum} type
   * @return the property value as an {@link Enum}
   */
  <T extends Enum<T>> T getAsEnum(Class<T> enumType);

  /**
   * Retrieves the value of the property as a {@link URL}.
   *
   * @return the property value as a {@link URL}
   */
  URL getAsFile();

  /**
   * Retrieves the map object ID associated with this property.
   *
   * @return the map object ID
   */
  int getMapObjectId();

  /**
   * Retrieves the type of the property.
   *
   * @return the property type as a {@link String}
   */
  String getType();

  /**
   * Sets the type of the property.
   *
   * @param type the property type as a {@link String}
   */
  void setType(String type);

  /**
   * Tests for equality between two custom properties. Two custom properties are <i>equal</i> if they both have the same type and string value.
   *
   * @param anObject the custom property to test equality for
   * @return {@code true} if the two custom properties are equal, or {@code false} otherwise
   */
  boolean equals(Object anObject);

  /**
   * Returns the hash code for this custom property. The hash code for a custom property is equal to its type's hash code times 31 plus its value's
   * hash code.
   *
   * @return the hash code for this custom property
   */
  int hashCode();
}
