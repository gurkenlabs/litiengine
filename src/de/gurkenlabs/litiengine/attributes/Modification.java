/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.attributes;

/**
 * The Enum Modification.
 */
public enum Modification {

  /** The Add. */
  ADD(1),

  /** The Add percent. */
  ADDPERCENT(5),

  /** The Divide. */
  DIVIDE(4),

  /** The Multiply. */
  MULTIPLY(3),

  /** The Set. */
  SET(7),

  /** The Substract. */
  SUBSTRACT(2),

  /** The Substract percent. */
  SUBSTRACTPERCENT(6),

  /** The Unknown. */
  UNKNOWN(-1);

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
