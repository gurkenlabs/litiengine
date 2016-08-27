/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.sound.Sound;

// TODO: Auto-generated Javadoc
/**
 * The Class ListField.
 */
public class ListField extends GuiComponent {
  private final List<Consumer<Integer>> changeConsumer;
  private final Object[] contents;

  /** The locked selection. */
  private int currentSelection, lockedSelection;

  /** The list items. */
  private final ArrayList<ImageComponent> listEntries;

  /**
   * Instantiates a new list field.
   *
   * @param x
   *          the x
   * @param y
   *          the y
   * @param width
   *          the width
   * @param height
   *          the height
   * @param content
   *          the content
   */
  public ListField(final double x, final double y, final double width, final double height, final Object[] content, final Spritesheet entrySprite, final Sound hoverSound) {
    super(x, y, width, height * content.length);

    this.changeConsumer = new ArrayList<Consumer<Integer>>();
    this.contents = content;
    this.listEntries = new ArrayList<ImageComponent>();
    for (int i = 0; i < this.contents.length; i++) {
      ImageComponent entryComponent;
      if (this.contents[i] == null) {
        entryComponent = new ImageComponent(this.getX(), this.getY() + this.getHeight() / this.contents.length * i, this.getWidth(), this.getHeight() / this.contents.length, entrySprite, "", null, hoverSound);
      } else {
        entryComponent = new ImageComponent(this.getX(), this.getY() + this.getHeight() / this.contents.length * i, this.getWidth(), this.getHeight() / this.contents.length, entrySprite, this.contents[i].toString(), null, hoverSound);

      }
      this.listEntries.add(entryComponent);
      this.getComponents().add(entryComponent);
    }
    this.setSelection(0);

  }

  /**
   * Gets the all list items.
   *
   * @return the all list items
   */
  public ArrayList<ImageComponent> getAllListEntries() {
    return this.listEntries;
  }

  public List<Consumer<Integer>> getChangeConsumer() {
    return this.changeConsumer;
  }

  /**
   * Gets the list item.
   *
   * @param listIndex
   *          the list index
   * @return the list item
   */
  public ImageComponent getListEntry(final int listIndex) {
    return this.listEntries.get(listIndex);
  }

  /**
   * Gets the selection.
   *
   * @return the selection
   */
  public int getSelection() {
    return this.lockedSelection;
  }

  public void setSelection(final int selection) {
    this.currentSelection = selection;
    for (final ImageComponent comp : this.getAllListEntries()) {
      if (comp != this.getAllListEntries().get(this.currentSelection)) {
        comp.setSelected(false);
      } else {
        if (!comp.isSelected()) {

        }
        comp.setSelected(true);
      }
    }
    this.lockSelection();

  }

  public void onChange(final Consumer<Integer> c) {
    this.getChangeConsumer().add(c);
  }

  /**
   * Removes the list entry.
   *
   * @param listIndex
   *          the list index
   */
  public void removeListEntry(final int listIndex) {
    this.listEntries.remove(listIndex);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.gui.GuiComponent#render(java.awt.Graphics)
   */
  @Override
  public void render(final Graphics2D g) {
    super.render(g);
    for (final ImageComponent e : this.getAllListEntries()) {
      e.render(g);
      if (e.isSelected()) {
        final Rectangle2D border = new Rectangle2D.Double(e.getX(), e.getY(), e.getWidth() - 1, e.getHeight() - 1);
        g.setColor(Color.WHITE);
        g.draw(border);
      }
    }
  }

  /**
   * Lock selection.
   */
  protected void lockSelection() {
    if (this.currentSelection < 0 || this.currentSelection > this.getAllListEntries().size() - 1) {
      return;
    }

    this.lockedSelection = this.currentSelection;
    this.getChangeConsumer().forEach(consumer -> consumer.accept(this.lockedSelection));

  }

  @Override
  public void prepare() {
    super.prepare();
    for (final ImageComponent comp : this.getAllListEntries()) {
      comp.onClicked(e -> {
        this.setSelection(this.getAllListEntries().indexOf(e.getSender()));
        this.lockSelection();
      });
      comp.prepare();
    }
  }

  @Override
  public void initializeComponents() {

  }
}