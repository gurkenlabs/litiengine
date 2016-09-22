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

  public static Icon ARROW_DOWN = new Icon(FontLoader.getIconFontThree(), "\uE804");

  public DropdownListField(final int x, final int y, final int width, final int height, final Object[] content, int elementsShown, final Spritesheet entrySprite, final Spritesheet buttonSprite) {
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
    return contentList;
  }

  public List<Consumer<Integer>> getChangeConsumer() {
    return this.changeConsumer;
  }

  public Spritesheet getEntrySprite() {
    return entrySprite;
  }

  public void setEntrySprite(Spritesheet entrySprite) {
    this.entrySprite = entrySprite;
  }

  public Spritesheet getButtonSprite() {
    return buttonSprite;
  }

  public void setButtonSprite(Spritesheet buttonSprite) {
    this.buttonSprite = buttonSprite;
  }

  public ImageComponent getChosenElementComponent() {
    return chosenElementComponent;
  }

  public boolean isDroppedDown() {
    return isDroppedDown;
  }

  public int getNumberOfShownElements() {
    return numberOfShownElements;
  }

  public CopyOnWriteArrayList<ImageComponent> getListEntries() {
    return this.getContentList().getListEntries();
  }

  public int getSelection() {
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
      this.getContentList().setSelection(this.getSelection() - 1);
    });

    Input.KEYBOARD.onKeyTyped(KeyEvent.VK_DOWN, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation() || !this.getChosenElementComponent().isHovered()) {
        return;
      }
      this.getContentList().setSelection(this.getSelection() + 1);
    });

    Input.MOUSE.onWheelMoved(e -> {
      if (this.isSuspended() || !this.isVisible() || !this.getChosenElementComponent().isHovered()) {
        return;
      }
      if (e.getWheelRotation() < 0) {
        this.getContentList().setSelection(this.getSelection() - 1);
      } else {
        this.getContentList().setSelection(this.getSelection() + 1);
      }
      return;
    });
  }

  @Override
  public void prepare() {
    this.contentList = new ListField(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.content, this.numberOfShownElements, this.entrySprite, this.buttonSprite);
    this.chosenElementComponent = new ImageComponent(this.getX(), this.getY(), this.getWidth(), this.getHeight() / this.getNumberOfShownElements(), this.getEntrySprite(), "", null);
    this.chosenElementComponent.setTextAlignment(TEXT_ALIGN_LEFT);
    double buttonHeight = this.getHeight() / this.getNumberOfShownElements(), buttonWidth = buttonHeight;
    this.dropDownButton = new ImageComponent(this.getX() - buttonWidth, this.getY(), buttonWidth, buttonHeight, this.getButtonSprite(), ARROW_DOWN.getText(), null);
    this.dropDownButton.setFont(ARROW_DOWN.getFont());

    this.getComponents().add(this.contentList);
    this.getComponents().add(this.chosenElementComponent);
    this.getComponents().add(this.dropDownButton);
    super.prepare();
    this.prepareInput();
    this.getContentList().suspend();

    if (this.getListEntries().size() != 0) {
      this.chosenElementComponent.setText(this.getListEntries().get(this.getSelection()).getText());
    }

    this.dropDownButton.onClicked(e -> {
      this.toggleDropDown();
    });

    this.onChange(c -> {
      this.chosenElementComponent.setText(this.content[c].toString());
      if (this.getContentList().isSuspended() || !this.getContentList().isVisible()) {
        return;
      }
      this.toggleDropDown();
    });

    this.getContentList().onChange(c -> {
      this.getChangeConsumer().forEach(consumer -> consumer.accept(this.getSelection()));
    });
  }

  public void onChange(final Consumer<Integer> c) {
    this.getChangeConsumer().add(c);
  }

  @Override
  protected void initializeComponents() {
    // TODO Auto-generated method stub

  }

}
