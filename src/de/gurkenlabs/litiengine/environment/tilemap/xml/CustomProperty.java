package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.util.Objects;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.util.ColorHelper;

public class CustomProperty implements ICustomProperty {

  private String type;
  private String value;

  public CustomProperty() {
    this.type = "string";
    this.value = "";
  }
  
  /**
   * Copy Constructor for copying instances of CustomProperties.
   *
   * @param propertyToBeCopied
   *          the Property we want to copy
   */
  public CustomProperty(CustomProperty propertyToBeCopied) {
    this.type = propertyToBeCopied.getType();
    this.value = propertyToBeCopied.getAsString();
  }

  public CustomProperty(String value) {
    this.type = "string";
    this.value = Objects.requireNonNull(value);
  }

  public CustomProperty(String type, String value) {
    this.type = Objects.requireNonNull(type);
    this.value = Objects.requireNonNull(value);
  }

  @Override
  public void setValue(String value) {
    this.value = Objects.requireNonNull(value);
  }

  @Override
  public void setValue(char value) {
    this.value = Character.toString(value);
  }

  @Override
  public void setValue(Enum<?> value) {
    this.value = value.name();
  }

  @Override
  public void setValue(long value) {
    this.value = Long.toString(value);
  }

  @Override
  public void setValue(double value) {
    this.value = Double.toString(value);
  }

  @Override
  public void setValue(boolean value) {
    this.value = Boolean.toString(value);
  }

  @Override
  public void setValue(Color value) {
    this.value = ColorHelper.encode(value);
  }

  @Override
  public String getAsString() {
    return this.value;
  }

  @Override
  public char getAsChar() {
    return this.value.charAt(0); //TODO Is this enough? Should it check if it's the right length and throw an exception if it's not?
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
    this.type = Objects.requireNonNull(type);
  }

  @Override
  public boolean equals(Object anObject) {
    if (this == anObject) {
      return true;
    }
    if (!(anObject instanceof ICustomProperty)) {
      return false;
    }
    ICustomProperty other = (ICustomProperty) anObject;
    return this.getType().equals(other.getType()) && this.getAsString().equals(other.getAsString());
  }

  @Override
  public int hashCode() {
    return this.getType().hashCode() * 31 + this.getAsString().hashCode();
  }

  @Override
  public String toString() {
    return this.getAsString() + " (" + this.getType() + ')';
  }
}
