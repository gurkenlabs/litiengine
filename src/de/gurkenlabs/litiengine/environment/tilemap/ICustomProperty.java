package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;
import java.net.URL;

/**
 * Represents a custom property on a map element.
 */
public interface ICustomProperty {

  public void setValue(String value);

  public void setValue(char value);

  public void setValue(Enum<?> value);

  public void setValue(long value);

  public void setValue(double value);

  // no methods for setting to byte, short, int, or float because they will already be accepted as a long or double

  public void setValue(boolean value);

  public void setValue(Color value);

  public String getAsString();

  public char getAsChar();

  public boolean getAsBool();

  public Color getAsColor();

  public float getAsFloat();

  public double getAsDouble();

  public byte getAsByte();

  public short getAsShort();

  public int getAsInt();

  public long getAsLong();

  public <T extends Enum<T>> T getAsEnum(Class<T> enumType);

  public URL getAsFile();

  public String getType();

  public void setType(String type);

  /**
   * Tests for equality between two custom properties. Two custom
   * properties are <i>equal</i> if they both have the same type
   * and string value.
   * 
   * @param anObject
   *          The custom property to test equality for
   * @return Whether the two custom properties are equal, or false
   *         if {@code anObject} is not a custom property
   */
  public boolean equals(Object anObject);

  /**
   * Returns the hash code for this custom property. The hash code
   * for a custom property is equal to its type's hash code times 31
   * plus its value's hash code.
   * 
   * @return The hash code for this custom property
   */
  public int hashCode();
}
