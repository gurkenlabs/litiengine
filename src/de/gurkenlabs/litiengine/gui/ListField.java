/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;
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
  private final CopyOnWriteArrayList<ImageComponent> listEntries;

  private int elementMargin = 0;
  private VerticalSlider slider;
  private Spritesheet buttonSprite, entrySprite;

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
  public ListField(final double x, final double y, final double width, final double height, final Object[] content, final Spritesheet entrySprite, final Spritesheet buttonSprite, final Sound hoverSound) {
    super(x, y, width, height * content.length);
    this.changeConsumer = new CopyOnWriteArrayList<Consumer<Integer>>();
    this.contents = content;
    this.listEntries = new CopyOnWriteArrayList<ImageComponent>();
    this.buttonSprite = buttonSprite;
    this.entrySprite = entrySprite;
    for (int i = 0; i < this.contents.length; i++) {
      ImageComponent entryComponent;
      if (this.contents[i] == null) {
        entryComponent = new ImageComponent(this.getX(), this.getY() + (this.getHeight() / this.contents.length + this.getElementMargin()) * i, this.getWidth(), this.getHeight() / this.contents.length, this.entrySprite, "", null, hoverSound);
      } else {
        entryComponent = new ImageComponent(this.getX(), this.getY() + (this.getHeight() / this.contents.length + this.getElementMargin()) * i, this.getWidth(), this.getHeight() / this.contents.length, this.entrySprite, this.contents[i].toString(), null, hoverSound);

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
  public CopyOnWriteArrayList<ImageComponent> getListEntries() {
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
    if (selection < 0 || selection >= this.contents.length) {
      return;
    }
    this.currentSelection = selection;
    for (final ImageComponent comp : this.getListEntries()) {
      if (comp != this.getListEntries().get(this.currentSelection)) {
        comp.setSelected(false);
      } else {
        if (!comp.isSelected()) {

        }
        comp.setSelected(true);
      }
    }
    this.lockSelection();
    this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getSelection()));
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
    for (final ImageComponent e : this.getListEntries()) {
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
    if (this.currentSelection < 0 || this.currentSelection > this.getListEntries().size() - 1) {
      return;
    }

    this.lockedSelection = this.currentSelection;
  }

  @Override
  public void prepare() {
    if (this.buttonSprite != null) {
      slider = new VerticalSlider(this.getX() + this.getWidth(), this.getY(), this.buttonSprite.getSpriteWidth()*3/4, this.getHeight(), 0, this.contents.length - 1, this.entrySprite, this.buttonSprite, null, false);
      this.getComponents().add(slider);
      slider.setCurrentValue(this.getSelection());
    }
    super.prepare();
    for (final ImageComponent comp : this.getListEntries()) {
      comp.onClicked(e -> {
        this.setSelection(this.getListEntries().indexOf(e.getSender()));
      });
    }

    Input.MOUSE.onWheelMoved(e -> {
      if (this.isHovered()) {
        if (e.getWheelRotation() < 0) {
          this.setSelection(this.getSelection() - 1);
        } else {
          this.setSelection(this.getSelection() + 1);
        }
        return;
      }
    });

    this.onChange(selection -> {
      slider.setCurrentValue(selection);
      slider.getSlider().setPosition(slider.getRelativeSliderPosition());
    });
    slider.onChange(sliderValue -> {
      this.setSelection(sliderValue.intValue());
      slider.getSlider().setPosition(slider.getRelativeSliderPosition());
    });

  }

  @Override
  public void initializeComponents() {

  }

  public int getElementMargin() {
    return elementMargin;
  }

  public void setElementMargin(int elementMargin) {
    int marginDiff = elementMargin - this.elementMargin;
    for (int i = 0; i < this.getListEntries().size(); i++) {
      if (i == 0) {
        continue;
      }

      this.getListEntries().get(i).setPosition(this.getListEntries().get(i).getPosition().getX(), this.getListEntries().get(i).getPosition().getY() + i * marginDiff);
    }

    this.elementMargin = elementMargin;
  }
}