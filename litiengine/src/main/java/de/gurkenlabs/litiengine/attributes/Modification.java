package de.gurkenlabs.litiengine.attributes;

/**
 * Represents the types of modifications that can be applied. Each modification type has an associated apply order.
 */
public enum Modification {
  ADD(1),
  DIVIDE(4),
  MULTIPLY(3),
  SET(5),
  SUBTRACT(2),
  UNKNOWN(-1);

  private final int applyOrder;

  Modification(final int order) {
    this.applyOrder = order;
  }

  /**
   * Gets the apply order of this modification type.
   *
   * @return the apply order
   */
  public int getApplyOrder() {
    return this.applyOrder;
  }
}
