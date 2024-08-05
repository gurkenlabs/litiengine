package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntConsumer;

/**
 * Represents a menu component that extends the ImageComponentList. This class provides functionality to create a menu with selectable items.
 */
public class Menu extends ImageComponentList {

  private int currentSelection;

  private final String[] items;

  private final List<IntConsumer> selectionChangeConsumers;

  private final Orientation orientation;

  /**
   * Constructs a new Menu with the specified position, size, and orientation.
   *
   * @param x           The x-coordinate of the menu.
   * @param y           The y-coordinate of the menu.
   * @param width       The width of the menu.
   * @param height      The height of the menu.
   * @param orientation The orientation of the menu (horizontal or vertical).
   * @param items       The items to be displayed in the menu.
   */
  public Menu(
    double x,
    double y,
    double width,
    double height,
    Orientation orientation,
    String... items) {
    this(x, y, width, height, null, orientation, items);
  }

  /**
   * Constructs a new Menu with the specified position, size, and items.
   *
   * @param x      The x-coordinate of the menu.
   * @param y      The y-coordinate of the menu.
   * @param width  The width of the menu.
   * @param height The height of the menu.
   * @param items  The items to be displayed in the menu.
   */
  public Menu(
    double x,
    double y,
    double width,
    double height,
    String... items) {
    this(x, y, width, height, null, Orientation.VERTICAL, items);
  }

  /**
   * Constructs a new Menu with the specified position, size, background, and orientation.
   *
   * @param x           The x-coordinate of the menu.
   * @param y           The y-coordinate of the menu.
   * @param width       The width of the menu.
   * @param height      The height of the menu.
   * @param background  The background of the menu.
   * @param orientation The orientation of the menu (horizontal or vertical).
   * @param items       The items to be displayed in the menu.
   */
  public Menu(
    double x,
    double y,
    double width,
    double height,
    Spritesheet background,
    Orientation orientation,
    String... items) {
    super(x, y, width, height,
      orientation == Orientation.VERTICAL ? items.length : 1,
      orientation == Orientation.VERTICAL ? 1 : items.length,
      null, background);
    this.items = items;
    this.selectionChangeConsumers = new CopyOnWriteArrayList<>();
    this.orientation = orientation;
  }

  /**
   * Gets the current selection index of the menu.
   *
   * @return The current selection index of the menu.
   */
  public int getCurrentSelection() {
    return this.currentSelection;
  }

  /**
   * Gets the orientation of the menu.
   *
   * @return The orientation of the menu.
   */
  public Orientation getOrientation() {
    return orientation;
  }

  /**
   * Adds a consumer that will be notified when the selection of the menu changes.
   *
   * @param cons The consumer that will be notified when the selection of the menu changes.
   */
  public void onChange(final IntConsumer cons) {
    this.selectionChangeConsumers.add(cons);
  }

  /**
   * Prepares the menu by setting the text of the menu buttons and adding the click listeners.
   */
  @Override
  public void prepare() {

    super.prepare();
    for (int i = 0; i < items.length; i++) {
      final ImageComponent menuButton = getCellComponents().get(i);
      menuButton.setText(items[i]);
      menuButton.onClicked(
        c -> setCurrentSelection(getCellComponents().indexOf(menuButton)));
    }
  }

  /**
   * Sets the current selection index of the menu and updates the selection state of the menu items.
   *
   * @param newSelection The index of the item to be selected.
   */
  public void setCurrentSelection(final int newSelection) {
    this.currentSelection = newSelection;

    for (int i = 0; i < getCellComponents().size(); i++) {
      getCellComponents().get(getCurrentSelection()).setSelected(i == getCurrentSelection());
    }

    this.selectionChangeConsumers.forEach(c -> c.accept(getCurrentSelection()));
  }
}
