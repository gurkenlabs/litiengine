package com.litiengine.gui;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntConsumer;

import com.litiengine.Align;
import com.litiengine.graphics.Spritesheet;
import com.litiengine.input.Input;

public class DropdownListField extends GuiComponent {
  public static final FontIcon ARROW_DOWN = new FontIcon(ICON_FONT, "\uE804");

  private boolean arrowKeyNavigation;
  private final List<IntConsumer> changeConsumer;
  private final Object[] content;

  private ListField contentList;
  /** The drop down button. */
  private ImageComponent dropDownButton;
  private ImageComponent chosenElementComponent;
  private Spritesheet entrySprite;
  private Spritesheet buttonSprite;
  private boolean isDroppedDown;

  private final int numberOfShownElements;

  public DropdownListField(final double x, final double y, final double width, final double height, final Object[] content, final int elementsShown) {
    super(x, y, width, height);
    this.content = content;
    this.numberOfShownElements = elementsShown;
    this.changeConsumer = new CopyOnWriteArrayList<>();

  }

  public Spritesheet getButtonSprite() {
    return this.buttonSprite;
  }

  public List<IntConsumer> getChangeConsumer() {
    return this.changeConsumer;
  }

  public ImageComponent getChosenElementComponent() {
    return this.chosenElementComponent;
  }

  public Object[] getContentArray() {
    return this.content;
  }

  public ListField getContentList() {
    return this.contentList;
  }

  public ImageComponent getDropDownButton() {
    return this.dropDownButton;
  }

  public Spritesheet getEntrySprite() {
    return this.entrySprite;
  }

  public List<ImageComponent> getListEntries() {
    return this.getContentList().getListEntry(0);
  }

  public int getNumberOfShownElements() {
    return this.numberOfShownElements;
  }

  public int getSelectedIndex() {
    return this.getContentList().getSelectionRow();
  }

  public Object getSelectedObject() {
    if (this.getContentArray().length == 0) {
      return null;
    }

    return this.getContentArray()[this.getContentList().getSelectionRow()];
  }

  public boolean isArrowKeyNavigation() {
    return this.arrowKeyNavigation;
  }

  public boolean isDroppedDown() {
    return this.isDroppedDown;
  }

  public void onChange(final IntConsumer c) {
    this.getChangeConsumer().add(c);
  }

  @Override
  public void prepare() {
    this.contentList = new ListField(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getContentArray(), this.numberOfShownElements);
    this.contentList.setButtonSprite(this.buttonSprite);
    this.contentList.setEntrySprite(this.entrySprite);
    this.chosenElementComponent = new ImageComponent(this.getX(), this.getY(), this.getWidth(), this.getHeight() / this.getNumberOfShownElements(), this.getEntrySprite(), "", null);
    this.chosenElementComponent.setTextAlign(Align.LEFT);
    final double buttonHeight = this.getHeight() / this.getNumberOfShownElements();
    double buttonWidth = buttonHeight;
    this.dropDownButton = new ImageComponent(this.getX() - buttonWidth, this.getY(), buttonWidth, buttonHeight, this.getButtonSprite(), ARROW_DOWN.getText(), null);
    this.dropDownButton.setFont(ARROW_DOWN.getFont());

    this.getComponents().clear();
    this.getComponents().add(this.contentList);
    this.getComponents().add(this.chosenElementComponent);
    this.getComponents().add(this.dropDownButton);
    super.prepare();
    this.prepareInput();
    this.getContentList().suspend();

    if (!this.getListEntries().isEmpty()) {
      this.chosenElementComponent.setText(this.getListEntries().get(0).getText());
    }

    this.dropDownButton.onClicked(e -> this.toggleDropDown());

    this.onChange(c -> {
      this.chosenElementComponent.setText(this.getContentArray()[c].toString());
      if (this.getContentList().isSuspended() || !this.getContentList().isVisible()) {
        return;
      }
      this.toggleDropDown();
    });

    this.getContentList().onChange(c -> this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getSelectedIndex())));
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

  public void setSelection(final int selectionIndex) {
    if (this.getContentList() == null) {
      return;
    }

    this.getContentList().setSelection(0, selectionIndex);
  }

  public void setSelection(final Object selectedObject) {
    if (selectedObject == null) {
      return;
    }

    for (int i = 0; i < this.getContentArray().length; i++) {
      if (this.getContentArray()[i] != null && this.getContentArray()[i].equals(selectedObject)) {
        this.setSelection(i);
        return;
      }
    }
  }

  /**
   * Toggle drop down.
   */
  public void toggleDropDown() {
    if (this.isDroppedDown()) {
      this.getContentList().suspend();
      this.chosenElementComponent.prepare();
    } else {
      this.chosenElementComponent.suspend();
      this.getContentList().prepare();
    }
    this.isDroppedDown = !this.isDroppedDown;
    this.getContentList().refresh();
  }

  private void prepareInput() {
    Input.keyboard().onKeyTyped(KeyEvent.VK_UP, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation() || !this.getChosenElementComponent().isHovered()) {
        return;
      }
      this.getContentList().setSelection(0, this.getSelectedIndex() - 1);
    });

    Input.keyboard().onKeyTyped(KeyEvent.VK_DOWN, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation() || !this.getChosenElementComponent().isHovered()) {
        return;
      }
      this.getContentList().setSelection(0, this.getSelectedIndex() + 1);
    });

    this.onMouseWheelScrolled(e -> {
      if (this.isSuspended() || !this.isVisible() || !this.getChosenElementComponent().isHovered()) {
        return;
      }
      if (e.getEvent().getWheelRotation() < 0) {
        this.getContentList().setSelection(0, this.getSelectedIndex() - 1);
      } else {
        this.getContentList().setSelection(0, this.getSelectedIndex() + 1);
      }
      return;
    });
  }
}
