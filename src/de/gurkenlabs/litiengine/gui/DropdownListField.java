package de.gurkenlabs.litiengine.gui;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;

public class DropdownListField extends GuiComponent {
  /** The drop down button. */
  private ImageComponent dropDownButton, chosenElementComponent;

  private boolean isDroppedDown;
  private ListField contentList;
  private final List<Consumer<Integer>> changeConsumer;

  private Object[] content;
  private int numberOfShownElements;
  private Spritesheet entrySprite, buttonSprite;
  private boolean arrowKeyNavigation;

  public static FontIcon ARROW_DOWN = new FontIcon(ICON_FONT, "\uE804");

  public DropdownListField(final double x, final double y, final double width, final double height, final Object[] content, int elementsShown, final Spritesheet entrySprite, final Spritesheet buttonSprite) {
    super(x, y, width, height);
    this.content = content;
    this.numberOfShownElements = elementsShown;
    this.entrySprite = entrySprite;
    this.buttonSprite = buttonSprite;
    this.changeConsumer = new CopyOnWriteArrayList<Consumer<Integer>>();

  }

  @Override
  public void render(final Graphics2D g) {
    super.render(g);
  }

  public ListField getContentList() {
    return this.contentList;
  }

  public ImageComponent getDropDownButton() {
    return this.dropDownButton;
  }

  public List<Consumer<Integer>> getChangeConsumer() {
    return this.changeConsumer;
  }

  public Spritesheet getEntrySprite() {
    return this.entrySprite;
  }

  public void setEntrySprite(Spritesheet entrySprite) {
    this.entrySprite = entrySprite;
  }

  public Spritesheet getButtonSprite() {
    return this.buttonSprite;
  }

  public void setButtonSprite(Spritesheet buttonSprite) {
    this.buttonSprite = buttonSprite;
  }

  public ImageComponent getChosenElementComponent() {
    return this.chosenElementComponent;
  }

  public boolean isDroppedDown() {
    return this.isDroppedDown;
  }

  public int getNumberOfShownElements() {
    return this.numberOfShownElements;
  }

  public CopyOnWriteArrayList<ImageComponent> getListEntries() {
    return this.getContentList().getListEntries();
  }

  public Object getSelectedObject() {
    if(this.getContentArray().length == 0){
      return null;
    }
    
    return this.getContentArray()[this.getContentList().getSelection()];
  }

  public int getSelectedIndex() {
    return this.getContentList().getSelection();
  }

  public boolean isArrowKeyNavigation() {
    return this.arrowKeyNavigation;
  }

  public void setArrowKeyNavigation(boolean arrowKeyNavigation) {
    this.arrowKeyNavigation = arrowKeyNavigation;
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
    Input.KEYBOARD.onKeyTyped(KeyEvent.VK_UP, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation() || !this.getChosenElementComponent().isHovered()) {
        return;
      }
      this.getContentList().setSelection(this.getSelectedIndex() - 1);
    });

    Input.KEYBOARD.onKeyTyped(KeyEvent.VK_DOWN, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation() || !this.getChosenElementComponent().isHovered()) {
        return;
      }
      this.getContentList().setSelection(this.getSelectedIndex() + 1);
    });

    this.onMouseWheelScrolled(e -> {
      if (this.isSuspended() || !this.isVisible() || !this.getChosenElementComponent().isHovered()) {
        return;
      }
      if (e.getEvent().getWheelRotation() < 0) {
        this.getContentList().setSelection(this.getSelectedIndex() - 1);
      } else {
        this.getContentList().setSelection(this.getSelectedIndex() + 1);
      }
      return;
    });
  }

  public void setSelection(int selectionIndex) {
    if (this.getContentList() == null) {
      return;
    }

    this.getContentList().setSelection(selectionIndex);
  }

  public void setSelection(Object selectedObject) {
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

  @Override
  public void prepare() {
    this.contentList = new ListField(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getContentArray(), this.numberOfShownElements, this.entrySprite, this.buttonSprite);
    this.chosenElementComponent = new ImageComponent(this.getX(), this.getY(), this.getWidth(), this.getHeight() / this.getNumberOfShownElements(), this.getEntrySprite(), "", null);
    this.chosenElementComponent.setTextAlignment(TEXT_ALIGN_LEFT);
    double buttonHeight = this.getHeight() / this.getNumberOfShownElements(), buttonWidth = buttonHeight;
    this.dropDownButton = new ImageComponent(this.getX() - buttonWidth, this.getY(), buttonWidth, buttonHeight, this.getButtonSprite(), ARROW_DOWN.getText(), null);
    this.dropDownButton.setFont(ARROW_DOWN.getFont());

    this.getComponents().clear();
    this.getComponents().add(this.contentList);
    this.getComponents().add(this.chosenElementComponent);
    this.getComponents().add(this.dropDownButton);
    super.prepare();
    this.prepareInput();
    this.getContentList().suspend();

    if (this.getListEntries().size() != 0) {
      this.chosenElementComponent.setText(this.getListEntries().get(this.getSelectedIndex()).getText());
    }

    this.dropDownButton.onClicked(e -> {
      this.toggleDropDown();
    });

    this.onChange(c -> {
      this.chosenElementComponent.setText(this.getContentArray()[c].toString());
      if (this.getContentList().isSuspended() || !this.getContentList().isVisible()) {
        return;
      }
      this.toggleDropDown();
    });

    this.getContentList().onChange(c -> {
      this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getSelectedIndex()));
    });
  }

  public void onChange(final Consumer<Integer> c) {
    this.getChangeConsumer().add(c);
  }

  public Object[] getContentArray() {
    return this.content;
  }

  @Override
  protected void initializeComponents() {

  }
}
