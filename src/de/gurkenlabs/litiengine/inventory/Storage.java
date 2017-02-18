package de.gurkenlabs.litiengine.inventory;

// TODO: Auto-generated Javadoc
/**
 * Storage is an abstract class that represents a container of StorageSlots.
 */
public abstract class Storage {

  /**
   * Instantiates a new storage.
   *
   * @param size
   *          the number of storage slots. Since changing the size of a storage
   *          is not intended, the StorageSlots are held in an Array. Use
   *          setupStorageSlots(int numberOfSlots) to reinitialize the array.
   */
  public Storage(final int size) {
    this.setupStorageSlots(size);
  }

  /**
   * Clear.
   */
  public abstract void clear();

  /**
   * Gets the number of slots.
   *
   * @return the number of slots
   */
  public abstract int getNumberOfSlots();

  /**
   * Gets a StorageSlot by index.
   *
   * @param index
   *          the index
   * @return the slot
   */
  public abstract StorageSlot getSlot(int index);

  /**
   * Gets the storage slots.
   *
   * @return the storage slots
   */
  public abstract StorageSlot[] getStorageSlots();

  /**
   * Sets up the array of StorageSlots with the given size.
   *
   * @param numberOfSlots
   *          the new amount of storage slots.
   */
  public abstract void setupStorageSlots(int numberOfSlots);
}
