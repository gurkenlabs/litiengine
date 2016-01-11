/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.attributes;

// TODO: Auto-generated Javadoc
/**
 * The Enum Modification.
 */
public enum Modification {

  /** The Add. */
  Add(1),

  /** The Add percent. */
  AddPercent(5),

  /** The Divide. */
  Divide(4),

  /** The Multiply. */
  Multiply(3),

  /** The Set. */
  Set(7),

  /** The Substract. */
  Substract(2),

  /** The Substract percent. */
  SubstractPercent(6),

  /** The Unknown. */
  Unknown(-1);

  /** The apply order. */
  private final int applyOrder;

  /**
   * Instantiates a new modification.
   *
   * @param value
   *          the value
   */
  private Modification(final int value) {
    this.applyOrder = value;
  }

  /**
   * Gets the apply order.
   *
   * @return the apply order
   */
  public int getApplyOrder() {
    return this.applyOrder;
  }
}
