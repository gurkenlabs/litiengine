/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui;

import java.awt.Graphics2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.Spritesheet;

// TODO: Auto-generated Javadoc
/**
 * The Class Menu.
 */
public class Menu extends ImageComponentList {

  /** The menu buttons. */
  private final String[] items;
  private final List<Consumer<Integer>> selectionChangeConsumers;
  private int currentSelection;

  public Menu(final double x, final double y, final double width, final double height, int rows, int columns, final String[] items, final Spritesheet background) {
    super(x, y, width, height, rows, columns, null, background);
    this.items = items;
    this.selectionChangeConsumers = new CopyOnWriteArrayList<>();

  }

  @Override
  public void render(final Graphics2D g) {
    super.render(g);
  }

  @Override
  public void prepare() {

    super.prepare();
    for (int i = 0; i < this.items.length; i++) {
      final ImageComponent menuButton = this.getCellComponents().get(i);
      menuButton.setText(items[i]);
      this.getCellComponents().add(menuButton);
      menuButton.onClicked(c -> this.setCurrentSelection(this.getCellComponents().indexOf(menuButton)));

    }
    for (ImageComponent comp : this.getCellComponents()) {
      if (this.getCellComponents().indexOf(comp) >= this.items.length) {
        this.getCellComponents().remove(comp);
        this.getComponents().remove(comp);
      }
    }

  }

  @Override
  protected void initializeComponents() {

  }

  public int getCurrentSelection() {
    return this.currentSelection;
  }

  public void setCurrentSelection(int currentSelection) {
    this.currentSelection = currentSelection;
    this.selectionChangeConsumers.forEach(c -> c.accept(this.getCurrentSelection()));
  }

  public void onChange(Consumer<Integer> cons) {
    this.selectionChangeConsumers.add(cons);
  }
}
