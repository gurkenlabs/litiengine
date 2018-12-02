package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.util.ColorHelper;

public class CustomProperty implements ICustomProperty {

  private String type;
  private String value;

  public CustomProperty() {
    this("string", "");
  }

  public CustomProperty(String value) {
    this("string", value);
  }

  public CustomProperty(String type, String value) {
    this.type = type;
    this.value = value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public void setValue(char value) {
    this.setValue(Character.toString(value));
  }

  @Override
  public void setValue(Enum<?> value) {
    this.setValue(value.name());
  }

  @Override
  public void setValue(long value) {
    this.setValue(Long.toString(value));
  }

  @Override
  public void setValue(double value) {
    this.setValue(Double.toString(value));
  }

  @Override
  public void setValue(boolean value) {
    this.setValue(Boolean.toString(value));
  }

  @Override
  public void setValue(Color value) {
    this.setValue(ColorHelper.encode(value));
  }

  @Override
  public String getAsString() {
    return this.value;
  }

  @Override
  public char getAsChar() {
    return this.value.charAt(0); // Is this enough? Should it check if it's the right length and throw an exception if it's not?
  }

  @Override
  public boolean getAsBool() {
    return Boolean.parseBoolean(this.value);
  }

  @Override
  public Color getAsColor() {
    return ColorHelper.decode(this.value);
  }

  @Override
  public float getAsFloat() {
    return Float.parseFloat(this.value);
  }

  @Override
  public double getAsDouble() {
    return Double.parseDouble(this.value);
  }

  @Override
  public byte getAsByte() {
    return Byte.parseByte(this.value);
  }

  @Override
  public short getAsShort() {
    return Short.parseShort(this.value);
  }

  @Override
  public int getAsInt() {
    return Integer.parseInt(this.value);
  }

  @Override
  public long getAsLong() {
    return Long.parseLong(this.value);
  }

  @Override
  public <T extends Enum<T>> T getAsEnum(Class<T> enumType) {
    try {
      return Enum.valueOf(enumType, this.value);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Override
  public String getType() {
    return this.type;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }
}
