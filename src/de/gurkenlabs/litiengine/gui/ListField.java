/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
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

  private int lowerBound = 0;

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

  public ImageComponent getSelectedComponent() {
    return this.selectedComponent;
  }

  public int getLowerBound() {
    return this.lowerBound;
  }

  public void setLowerBound(int lowerBound) {
    this.lowerBound = lowerBound;
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

  public Spritesheet getButtonSprite() {
    return this.buttonSprite;
  }

  public void setButtonSprite(Spritesheet buttonSprite) {
    this.buttonSprite = buttonSprite;
  }

  public Spritesheet getEntrySprite() {
    return this.entrySprite;
  }

  public void setEntrySprite(Spritesheet entrySprite) {
    this.entrySprite = entrySprite;
  }

  public void setSelection(final int selection) {
    if (selection < 0 || selection >= this.contents.length) {
      return;
    }
    this.selection = selection;

    if (this.getSelection() >= this.getLowerBound() + (this.getNumberOfShownElements())) {
      this.setLowerBound(this.getLowerBound() + 1);
    } else if (this.getSelection() < this.getLowerBound() && this.getLowerBound() > 0) {
      this.setLowerBound(this.getLowerBound() - 1);
    }
    this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getSelection()));
    this.refresh();

  }

  public void onChange(final Consumer<Integer> c) {
    this.getChangeConsumer().add(c);
  }

  private void refresh() {
    for (int i = 0; i < this.getNumberOfShownElements(); i++) {
      if (this.contents.length <= i) {
        continue;
      }
      this.getListEntry(i).setText(this.contents[i + this.getLowerBound()].toString());
    }
    if (this.getSelection() >= this.getLowerBound() && this.getSelection() < this.getLowerBound() + this.getNumberOfShownElements()) {
      this.selectedComponent = this.getListEntry(this.getSelection() - this.getLowerBound());
      this.selectedComponent.setSelected(true);
    } else {
      this.selectedComponent = null;
    }

//    System.out.println(" Selection: " + this.getSelection() + " lowerBound: " + this.getLowerBound() + " upperBound: " + (this.getLowerBound() + this.getNumberOfShownElements() - 1) + " selectedComponent: " + this.getListEntries().indexOf(selectedComponent));

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
      Stroke oldStroke = g.getStroke();
      g.setStroke(new BasicStroke(2));
      final Rectangle2D border = new Rectangle2D.Double(this.selectedComponent.getX() - 1, this.selectedComponent.getY() - 1, this.selectedComponent.getWidth() + 2, this.selectedComponent.getHeight() + 2);
      g.setColor(Color.WHITE);
      g.draw(border);
      g.setStroke(oldStroke);
    }
  }

  @Override
  public void prepare() {
    boolean showButtons = false;
    if (this.buttonSprite != null) {
      showButtons = true;
    }
    slider = new VerticalSlider(this.getX() + this.getWidth(), this.getY(), this.buttonSprite.getSpriteWidth() * 3 / 4, this.getHeight(), 0, this.contents.length - this.getNumberOfShownElements(), 1, this.entrySprite, this.buttonSprite, null, showButtons);
    this.getComponents().add(slider);
    slider.setCurrentValue(this.getLowerBound());

    for (int i = 0; i < getNumberOfShownElements(); i++) {
      ImageComponent entryComponent;
      if (this.contents.length <= i) {
        continue;
      }
      if (this.contents[i] == null) {
        entryComponent = new ImageComponent(this.getX(), this.getY() + (this.getHeight() / this.getNumberOfShownElements()) * i, this.getWidth(), this.getHeight() / this.getNumberOfShownElements(), this.entrySprite, "", null);
      } else {
        entryComponent = new ImageComponent(this.getX(), this.getY() + (this.getHeight() / this.getNumberOfShownElements()) * i, this.getWidth(), this.getHeight() / this.getNumberOfShownElements(), this.entrySprite, this.contents[i].toString(), null);
      }
      this.getListEntries().add(entryComponent);
    }
    this.getComponents().addAll(this.getListEntries());
    super.prepare();
    for (final ImageComponent comp : this.getListEntries()) {
      comp.onClicked(e -> {
        this.setSelection(this.getLowerBound() + this.getListEntries().indexOf(comp) % this.getNumberOfShownElements());
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
        return;
      }
    });

    this.onChange(selection -> {
      if (slider != null) {
        slider.setCurrentValue(this.getLowerBound());
        slider.getSlider().setPosition(slider.getRelativeSliderPosition());
      }
    });
    if (slider != null) {
      slider.onChange(sliderValue -> {
        this.setLowerBound(sliderValue.intValue());
        slider.getSlider().setPosition(slider.getRelativeSliderPosition());
        this.refresh();
      });

    }
  }

  @Override
  public void initializeComponents() {

  }

}