package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Color;

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

  public String getType();

  public void setType(String type);
}
