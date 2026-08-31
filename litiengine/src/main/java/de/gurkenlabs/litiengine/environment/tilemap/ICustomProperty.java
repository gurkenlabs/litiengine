package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.net.URL;

/// The `ICustomProperty` interface defines methods for managing custom properties with various data types. It provides functionality to set and
/// retrieve property values in different formats, as well as methods for type and equality handling.
public interface ICustomProperty {

  /// Sets the value of the property as a [URL].
  ///
  /// @param value the [URL] value to set
  void setValue(URL value);

  /// Sets the value of the property as a [String].
  ///
  /// @param value the [String] value to set
  void setValue(String value);

  /// Sets the value of the property as a `char`.
  ///
  /// @param value the `char` value to set
  void setValue(char value);

  /// Sets the value of the property as an [Enum].
  ///
  /// @param value the [Enum] value to set
  void setValue(Enum<?> value);

  /// Sets the value of the property as a `long`.
  ///
  /// @param value the `long` value to set
  void setValue(long value);

  /// Sets the value of the property as a `double`.
  ///
  /// @param value the `double` value to set
  void setValue(double value);

  /// Sets the value of the property as a `boolean`.
  ///
  /// @param value the `boolean` value to set
  void setValue(boolean value);

  /// Sets the value of the property as a [Color].
  ///
  /// @param value the [Color] value to set
  void setValue(Color value);

  /// Retrieves the value of the property as a [String].
  ///
  /// @return the property value as a [String]
  String getAsString();

  /// Retrieves the value of the property as a `char`.
  ///
  /// @return the property value as a `char`
  char getAsChar();

  /// Retrieves the value of the property as a `boolean`.
  ///
  /// @return the property value as a `boolean`
  boolean getAsBool();

  /// Retrieves the value of the property as a [Color].
  ///
  /// @return the property value as a [Color]
  Color getAsColor();

  /// Retrieves the value of the property as a `float`.
  ///
  /// @return the property value as a `float`
  float getAsFloat();

  /// Retrieves the value of the property as a `double`.
  ///
  /// @return the property value as a `double`
  double getAsDouble();

  /// Retrieves the value of the property as a `byte`.
  ///
  /// @return the property value as a `byte`
  byte getAsByte();

  /// Retrieves the value of the property as a `short`.
  ///
  /// @return the property value as a `short`
  short getAsShort();

  /// Retrieves the value of the property as an `int`.
  ///
  /// @return the property value as an `int`
  int getAsInt();

  /// Retrieves the value of the property as a `long`.
  ///
  /// @return the property value as a `long`
  long getAsLong();

  /// Retrieves the value of the property as an [Enum].
  ///
  /// @param <T>      the type of the [Enum]
  /// @param enumType the [Class] of the [Enum] type
  /// @return the property value as an [Enum]
  <T extends Enum<T>> T getAsEnum(Class<T> enumType);

  /// Retrieves the value of the property as a [URL].
  ///
  /// @return the property value as a [URL]
  URL getAsFile();

  /// Retrieves the map object ID associated with this property.
  ///
  /// @return the map object ID
  int getMapObjectId();

  /// Retrieves the type of the property.
  ///
  /// @return the property type as a [String]
  String getType();

  /// Sets the type of the property.
  ///
  /// @param type the property type as a [String]
  void setType(String type);

  /// Tests for equality between two custom properties. Two custom properties are *equal* if they both have the same type and string value.
  ///
  /// @param anObject the custom property to test equality for
  /// @return `true` if the two custom properties are equal, or `false` otherwise
  boolean equals(Object anObject);

  /// Returns the hash code for this custom property. The hash code for a custom property is equal to its type's hash code times 31 plus its value's
  /// hash code.
  ///
  /// @return the hash code for this custom property
  int hashCode();
}
