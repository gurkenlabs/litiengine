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

// TODO: Auto-generated Javadoc
/**
 * The Class ListField.
 */
public class ListField extends GuiComponent {
  private final List<Consumer<Integer>> changeConsumer;
  private ImageComponent selectedComponent;
  private final Object[] contents;

  /** The locked selection. */
  private int selection, shownElements;;

  /** The list items. */
  private final CopyOnWriteArrayList<ImageComponent> listEntries;

  private int elementMargin = 0, lowerBound = 0;
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
  public ListField(final double x, final double y, final double width, final double height, final Object[] content, final int shownElements, final Spritesheet entrySprite, final Spritesheet buttonSprite) {
    super(x, y, width, height);
    this.changeConsumer = new CopyOnWriteArrayList<Consumer<Integer>>();
    this.contents = content;
    this.listEntries = new CopyOnWriteArrayList<ImageComponent>();
    this.buttonSprite = buttonSprite;
    this.entrySprite = entrySprite;
    this.shownElements = shownElements;

  }

  /**
   * Gets the all list items.
   *
   * @return the all list items
   */
  public CopyOnWriteArrayList<ImageComponent> getListEntries() {
    return this.listEntries;
  }

  public int getNumberOfShownElements() {
    return this.shownElements;
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
    return this.selection;
  }

  public void setSelection(final int selection) {
    if (selection < 0 || selection >= this.contents.length) {
      return;
    }
    if (selection >= this.lowerBound + (this.getNumberOfShownElements() - 1)) {
      this.lowerBound++;
    } else if (selection <= this.lowerBound && lowerBound > 0) {
      this.lowerBound--;
    }
    this.selection = selection;
    this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getSelection()));
    System.out.println(" Selection: " + this.getSelection() + " lowerBound: " + this.lowerBound + " upperBound: " + (this.lowerBound + this.getNumberOfShownElements() - 1) + " selectedComponent: " + this.getListEntries().indexOf(selectedComponent));

  }

  public void onChange(final Consumer<Integer> c) {
    this.getChangeConsumer().add(c);
  }

  private void refresh() {
    for (int i = 0; i < this.getNumberOfShownElements(); i++) {
      this.getListEntry(i).setText(this.contents[i + this.lowerBound].toString());
    }
    if (this.getSelection() >= this.lowerBound && this.getSelection() < this.lowerBound + this.getNumberOfShownElements()) {
      this.selectedComponent = this.getListEntry(this.getSelection() - this.lowerBound);
      this.selectedComponent.setSelected(true);
    } else {
      this.selectedComponent = null;
    }
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
    if (this.selectedComponent != null) {
      final Rectangle2D border = new Rectangle2D.Double(this.selectedComponent.getX(), this.selectedComponent.getY(), this.selectedComponent.getWidth() - 1, this.selectedComponent.getHeight() - 1);
      g.setColor(Color.WHITE);
      g.draw(border);
    }
  }

  @Override
  public void prepare() {
    boolean showButtons = false;
    if (this.buttonSprite != null) {
      showButtons = true;
    }
    slider = new VerticalSlider(this.getX() + this.getWidth(), this.getY(), this.buttonSprite.getSpriteWidth() * 3 / 4, this.getHeight(), 0, this.contents.length - 1, this.entrySprite, this.buttonSprite, null, showButtons);
    this.getComponents().add(slider);
    slider.setCurrentValue(this.getSelection());

    for (int i = 0; i < getNumberOfShownElements(); i++) {
      ImageComponent entryComponent;
      if (this.contents[i] == null) {
        entryComponent = new ImageComponent(this.getX(), this.getY() + (this.getHeight() / this.getNumberOfShownElements() + this.getElementMargin()) * i, this.getWidth(), this.getHeight() / this.getNumberOfShownElements(), this.entrySprite, "", null);
      } else {
        entryComponent = new ImageComponent(this.getX(), this.getY() + (this.getHeight() / this.getNumberOfShownElements() + this.getElementMargin()) * i, this.getWidth(), this.getHeight() / this.getNumberOfShownElements(), this.entrySprite, this.contents[i].toString(), null);
      }
      this.getListEntries().add(entryComponent);
      this.getComponents().add(entryComponent);
    }
    super.prepare();
    for (final ImageComponent comp : this.getListEntries()) {
      comp.onClicked(e -> {
        this.setSelection(this.lowerBound + this.getListEntries().indexOf(comp) % this.getNumberOfShownElements());
        this.refresh();
      });
    }

    Input.MOUSE.onWheelMoved(e -> {
      if (this.isHovered()) {
        if (e.getWheelRotation() < 0) {
          this.setSelection(this.getSelection() - 1);
        } else {
          this.setSelection(this.getSelection() + 1);
        }
        this.refresh();
        return;
      }
    });

    this.onChange(selection -> {
      if (slider != null) {
        slider.setCurrentValue(selection);
        slider.getSlider().setPosition(slider.getRelativeSliderPosition());
        this.refresh();
      }
    });
    if (slider != null) {
      slider.onChange(sliderValue -> {
        if (sliderValue <= this.contents.length - (this.getNumberOfShownElements() - 1)) {
          this.lowerBound = (sliderValue.intValue());
        }
        slider.getSlider().setPosition(slider.getRelativeSliderPosition());
        this.refresh();
      });

    }
    // this.selectedComponent = this.getListEntry(0);
    // this.setSelection(0);
  }

  @Override
  public void initializeComponents() {

  }

  public int getElementMargin() {
    return elementMargin;
  }

  public void setElementMargin(int elementMargin) {
    this.elementMargin = elementMargin;
  }
}