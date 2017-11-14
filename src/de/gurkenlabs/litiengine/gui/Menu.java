package de.gurkenlabs.litiengine.gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.Spritesheet;

/**
 * The Class Menu.
 */
public class Menu extends ImageComponentList {

  private int currentSelection;
  /** The menu buttons. */
  private final String[] items;
  private final List<Consumer<Integer>> selectionChangeConsumers;

  public Menu(final double x, final double y, final double width, final double height, final String[] items) {
    this(x, y, width, height, items, null);
  }

  public Menu(final double x, final double y, final double width, final double height, final String[] items, final Spritesheet background) {
    super(x, y, width, height, items.length, 1, null, background);
    this.items = items;
    this.selectionChangeConsumers = new CopyOnWriteArrayList<>();
  }

  public int getCurrentSelection() {
    return this.currentSelection;
  }

  public void onChange(final Consumer<Integer> cons) {
    this.selectionChangeConsumers.add(cons);
  }

  @Override
  public void prepare() {

    super.prepare();
    for (int i = 0; i < this.items.length; i++) {
      final ImageComponent menuButton = this.getCellComponents().get(i);
      menuButton.setText(this.items[i]);
      menuButton.onClicked(c -> this.setCurrentSelection(this.getCellComponents().indexOf(menuButton)));
    }
  }

  public void setCurrentSelection(final int currentSelection) {
    this.currentSelection = currentSelection;

    for (int i = 0; i < this.getCellComponents().size(); i++) {
      this.getCellComponents().get(this.currentSelection).setSelected(i == this.currentSelection);
    }

    this.selectionChangeConsumers.forEach(c -> c.accept(this.getCurrentSelection()));
  }
}
