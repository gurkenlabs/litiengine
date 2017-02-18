package de.gurkenlabs.litiengine.inventory;

// TODO: Auto-generated Javadoc
/**
 * The Class StorageSlot.
 */
public abstract class StorageSlot {

  /** The capacity. */
  private final int capacity;

  /** The current amount. */
  private int currentAmount;

  /**
   * Instantiates a new storage slot.
   *
   * @param capacity
   *          the capacity
   */
  public StorageSlot(final int capacity) {
    this.capacity = capacity;
  }

  /**
   * Adds the.
   *
   * @param amount
   *          the amount
   */
  public void add(int amount) {
    while (!this.canAdd(amount)) {
      amount--;
    }
    this.currentAmount += amount;
  }

  /**
   * Can add.
   *
   * @param count
   *          the count
   * @return true, if successful
   */
  public boolean canAdd(final int count) {
    return this.getCurrentAmount() + count <= this.getCapacity();
  }

  /**
   * Can remove.
   *
   * @param count
   *          the count
   * @return true, if successful
   */
  public boolean canRemove(final int count) {
    return this.getCurrentAmount() >= count;
  }

  /**
   * Clear.
   */
  public void clear() {
    this.currentAmount = 0;
  }

  /**
   * Gets the capacity.
   *
   * @return the capacity
   */
  public int getCapacity() {
    return this.capacity;
  }

  /**
   * Gets the current amount.
   *
   * @return the current amount
   */
  public int getCurrentAmount() {
    return this.currentAmount;
  }

  /**
   * Removes the.
   *
   * @param amount
   *          the amount
   */
  public void remove(final int amount) {
    if (this.getCurrentAmount() < amount) {
      this.currentAmount = 0;
      return;
    }
    this.currentAmount -= amount;

  }
}
