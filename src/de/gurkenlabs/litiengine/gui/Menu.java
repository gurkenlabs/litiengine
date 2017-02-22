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

  private int currentSelection;
  /** The menu buttons. */
  private final String[] items;
  private final List<Consumer<Integer>> selectionChangeConsumers;

  public Menu(final double x, final double y, final double width, final double height, final int rows, final int columns, final String[] items, final Spritesheet background) {
    super(x, y, width, height, rows, columns, null, background);
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
      this.getCellComponents().add(menuButton);
      menuButton.onClicked(c -> this.setCurrentSelection(this.getCellComponents().indexOf(menuButton)));

    }
    for (final ImageComponent comp : this.getCellComponents()) {
      if (this.getCellComponents().indexOf(comp) >= this.items.length) {
        this.getCellComponents().remove(comp);
        this.getComponents().remove(comp);
      }
    }

  }

  @Override
  public void render(final Graphics2D g) {
    super.render(g);
  }

  public void setCurrentSelection(final int currentSelection) {
    this.currentSelection = currentSelection;
    this.selectionChangeConsumers.forEach(c -> c.accept(this.getCurrentSelection()));
  }

  @Override
  protected void initializeComponents() {

  }
}
