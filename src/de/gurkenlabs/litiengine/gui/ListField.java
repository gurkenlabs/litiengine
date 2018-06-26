package de.gurkenlabs.litiengine.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;

/**
 * The Class ListField.
 */
public class ListField extends GuiComponent {
  private boolean arrowKeyNavigation;
  private Spritesheet buttonSprite;
  private Spritesheet entrySprite;
  private final List<Consumer<Integer>> changeConsumer;

  private final Object[] contents;

  private final CopyOnWriteArrayList<ImageComponent> listEntries;

  private int lowerBound = 0;

  private ImageComponent selectedComponent;

  private int selection;

  private final int shownElements;
  private VerticalSlider slider;

  public ListField(final double x, final double y, final double width, final double height, final Object[] content, final int shownElements, final Spritesheet entrySprite, final Spritesheet buttonSprite) {
    super(x, y, width, height);
    this.changeConsumer = new CopyOnWriteArrayList<>();
    this.contents = content;
    this.listEntries = new CopyOnWriteArrayList<>();
    this.buttonSprite = buttonSprite;
    this.entrySprite = entrySprite;
    this.shownElements = shownElements;
    this.initContentList();
    this.prepareInput();
  }

  public Spritesheet getButtonSprite() {
    return this.buttonSprite;
  }

  public List<Consumer<Integer>> getChangeConsumer() {
    return this.changeConsumer;
  }

  public Object[] getContentArray() {
    return this.contents;
  }

  public Spritesheet getEntrySprite() {
    return this.entrySprite;
  }

  /**
   * Gets the all list items.
   *
   * @return the all list items
   */
  public List<ImageComponent> getListEntries() {
    return this.listEntries;
  }

  /**
   * Gets the list item.
   *
   * @param listIndex
   *          the list index
   * @return the list item
   */
  public ImageComponent getListEntry(final int listIndex) {
    if (listIndex <= 0 || listIndex > this.listEntries.size()) {
      return null;
    }

    return this.listEntries.get(listIndex);
  }

  public int getLowerBound() {
    return this.lowerBound;
  }

  public int getNumberOfShownElements() {
    return this.shownElements;
  }

  public ImageComponent getSelectedComponent() {
    return this.selectedComponent;
  }

  public Object getSelectedObject() {
    return this.getContentArray()[this.getSelection()];
  }

  /**
   * Gets the selection.
   *
   * @return the selection
   */
  public int getSelection() {
    return this.selection;
  }

  public VerticalSlider getSlider() {
    return this.slider;
  }

  public boolean isArrowKeyNavigation() {
    return this.arrowKeyNavigation;
  }

  public void onChange(final Consumer<Integer> c) {
    this.getChangeConsumer().add(c);
  }

  @Override
  public void suspend() {
    super.suspend();
  }

  @Override
  public void prepare() {
    super.prepare();
  }

  public void refresh() {
    for (int i = 0; i < this.getNumberOfShownElements(); i++) {
      if (this.getContentArray().length <= i) {
        continue;
      }

      if (this.getListEntry(i) != null) {
        this.getListEntry(i).setText(this.getContentArray()[i + this.getLowerBound()].toString());
      }
    }
    if (this.getSelection() >= this.getLowerBound() && this.getSelection() < this.getLowerBound() + this.getNumberOfShownElements()) {
      this.selectedComponent = this.getListEntry(this.getSelection() - this.getLowerBound());
      if (this.selectedComponent != null) {
        this.selectedComponent.setSelected(true);
      }
    } else {
      this.selectedComponent = null;
    }
  }

  @Override
  public void render(final Graphics2D g) {
    super.render(g);
    if (this.selectedComponent != null) {
      final Stroke oldStroke = g.getStroke();
      g.setStroke(new BasicStroke(2));
      final Rectangle2D border = new Rectangle2D.Double(this.selectedComponent.getX() - 1, this.selectedComponent.getY() - 1, this.selectedComponent.getWidth() + 2, this.selectedComponent.getHeight() + 2);
      g.setColor(Color.WHITE);
      g.draw(border);
      g.setStroke(oldStroke);
    }
  }

  public void setArrowKeyNavigation(final boolean arrowKeyNavigation) {
    this.arrowKeyNavigation = arrowKeyNavigation;
  }

  public void setButtonSprite(final Spritesheet buttonSprite) {
    this.buttonSprite = buttonSprite;
  }

  public void setEntrySprite(final Spritesheet entrySprite) {
    this.entrySprite = entrySprite;
  }

  public void setLowerBound(final int lowerBound) {
    this.lowerBound = lowerBound;
  }

  public void setSelection(final int selection) {
    if (selection < 0 || selection >= this.contents.length) {
      return;
    }
    this.selection = selection;

    if (this.getSelection() >= this.getLowerBound() + this.getNumberOfShownElements()) {
      this.setLowerBound(this.getLowerBound() + 1);
    } else if (this.getSelection() < this.getLowerBound() && this.getLowerBound() > 0) {
      this.setLowerBound(this.getLowerBound() - 1);
    }
    this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getSelection()));
    this.refresh();

  }

  private void initContentList() {
    boolean showButtons = false;
    if (this.buttonSprite != null) {
      showButtons = true;
    }
    final int sliderMax = this.getContentArray().length - this.getNumberOfShownElements();
    if (sliderMax > 0) {
      this.slider = new VerticalSlider(this.getX() + this.getWidth(), this.getY(), this.getHeight() / this.getNumberOfShownElements(), this.getHeight(), 0, sliderMax, 1, this.buttonSprite, this.buttonSprite, showButtons);
      this.getSlider().setCurrentValue(this.getLowerBound());
      this.getComponents().add(this.getSlider());
    }

    for (int i = 0; i < this.getNumberOfShownElements(); i++) {
      ImageComponent entryComponent;
      if (this.getContentArray().length <= i) {
        continue;
      }
      if (this.getContentArray()[i] == null) {
        entryComponent = new ImageComponent(this.getX(), this.getY() + this.getHeight() / this.getNumberOfShownElements() * i, this.getWidth(), this.getHeight() / this.getNumberOfShownElements(), this.entrySprite, "", null);
      } else {
        entryComponent = new ImageComponent(this.getX(), this.getY() + this.getHeight() / this.getNumberOfShownElements() * i, this.getWidth(), this.getHeight() / this.getNumberOfShownElements(), this.entrySprite, this.contents[i].toString(), null);
      }
      entryComponent.setTextAlignment(Align.LEFT);
      this.getListEntries().add(entryComponent);
    }
    this.getComponents().addAll(this.getListEntries());
    for (final ImageComponent comp : this.getListEntries()) {
      comp.onClicked(e -> {
        this.setSelection(this.getLowerBound() + this.getListEntries().indexOf(comp) % this.getNumberOfShownElements());
        this.refresh();
      });
    }

    this.onChange(s -> {
      if (this.getSlider() != null) {
        this.getSlider().setCurrentValue(this.getLowerBound());
        this.getSlider().getSliderComponent().setLocation(this.getSlider().getRelativeSliderPosition());
      }
    });
    if (this.getSlider() != null) {
      this.getSlider().onChange(sliderValue -> {
        this.setLowerBound(sliderValue.intValue());
        this.getSlider().getSliderComponent().setLocation(this.getSlider().getRelativeSliderPosition());
        this.refresh();
      });
    }
  }

  private void prepareInput() {
    Input.keyboard().onKeyTyped(KeyEvent.VK_UP, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation()) {
        return;
      }
      this.setSelection(this.getSelection() - 1);
    });

    Input.keyboard().onKeyTyped(KeyEvent.VK_DOWN, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation()) {
        return;
      }
      this.setSelection(this.getSelection() + 1);
    });

    this.onMouseWheelScrolled(e -> {
      if (this.isSuspended() || !this.isVisible()) {
        return;
      }
      if (this.isHovered()) {
        if (e.getEvent().getWheelRotation() < 0) {
          this.setSelection(this.getSelection() - 1);
        } else {
          this.setSelection(this.getSelection() + 1);
        }
        return;
      }
    });
  }
}