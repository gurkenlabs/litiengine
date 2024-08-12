package de.gurkenlabs.litiengine.attributes;

/**
 * The different modification types that can be applied to an {@code Attribute}.
 *
 * @see AttributeModifier#getModification()
 */
public enum Modification {
  ADD(1),
  DIVIDE(4),
  MULTIPLY(3),
  SET(5),
  SUBTRACT(2),
  UNKNOWN(-1);

  private final int applyOrder;

  private Modification(final int value) {
    this.applyOrder = value;
  }

  public int getApplyOrder() {
    return this.applyOrder;
  }
}
