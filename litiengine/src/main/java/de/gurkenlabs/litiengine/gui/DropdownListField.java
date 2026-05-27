package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntConsumer;

/**
 * A GUI component that displays a single selected entry from a list of choices and reveals the full list as a drop-down when the user clicks the
 * accompanying arrow button. The selection can also be navigated with arrow keys or the mouse wheel when
 * {@linkplain #isArrowKeyNavigation() arrow key navigation} is enabled.
 */
public class DropdownListField extends GuiComponent {
  /**
   * Down-pointing arrow icon shown on the drop-down toggle button.
   */
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

  /**
   * Constructs a new {@code DropdownListField}.
   *
   * @param x             the x coordinate of the component
   * @param y             the y coordinate of the component
   * @param width         the width of the component
   * @param height        the total height of the component including the drop-down list
   * @param content       the selectable values
   * @param elementsShown the number of list entries that are simultaneously shown when the drop-down is open
   */
  public DropdownListField(
      final double x,
      final double y,
      final double width,
      final double height,
      final Object[] content,
      final int elementsShown) {
    super(x, y, width, height);
    this.content = content;
    this.numberOfShownElements = elementsShown;
    this.changeConsumer = new CopyOnWriteArrayList<>();
  }

  /**
   * Gets the spritesheet used to render the drop-down button.
   *
   * @return the button spritesheet
   */
  public Spritesheet getButtonSprite() {
    return this.buttonSprite;
  }

  /**
   * Gets the list of registered change listeners.
   *
   * @return the change listener list
   */
  public List<IntConsumer> getChangeConsumer() {
    return this.changeConsumer;
  }

  /**
   * Gets the component that displays the currently selected element when the drop-down is collapsed.
   *
   * @return the chosen element component
   */
  public ImageComponent getChosenElementComponent() {
    return this.chosenElementComponent;
  }

  /**
   * Gets the array of selectable values.
   *
   * @return the content array
   */
  public Object[] getContentArray() {
    return this.content;
  }

  /**
   * Gets the internal list field used to render the drop-down entries.
   *
   * @return the content list
   */
  public ListField getContentList() {
    return this.contentList;
  }

  /**
   * Gets the toggle button used to open and close the drop-down.
   *
   * @return the drop-down toggle button
   */
  public ImageComponent getDropDownButton() {
    return this.dropDownButton;
  }

  /**
   * Gets the spritesheet used to render the drop-down entries.
   *
   * @return the entry spritesheet
   */
  public Spritesheet getEntrySprite() {
    return this.entrySprite;
  }

  /**
   * Gets the list of rendered drop-down entry components.
   *
   * @return the list entries
   */
  public List<ImageComponent> getListEntries() {
    return this.getContentList().getListEntry(0);
  }

  /**
   * Gets the maximum number of list entries that are shown simultaneously.
   *
   * @return the number of shown elements
   */
  public int getNumberOfShownElements() {
    return this.numberOfShownElements;
  }

  /**
   * Gets the index of the currently selected entry.
   *
   * @return the selected index
   */
  public int getSelectedIndex() {
    return this.getContentList().getSelectionRow();
  }

  /**
   * Gets the currently selected value.
   *
   * @return the selected value, or {@code null} if the content array is empty
   */
  public Object getSelectedObject() {
    if (this.getContentArray().length == 0) {
      return null;
    }

    return this.getContentArray()[this.getContentList().getSelectionRow()];
  }

  /**
   * Returns whether arrow-key navigation is enabled.
   *
   * @return {@code true} if the selection can be changed using arrow keys / mouse wheel while the component is hovered
   */
  public boolean isArrowKeyNavigation() {
    return this.arrowKeyNavigation;
  }

  /**
   * Returns whether the drop-down is currently expanded.
   *
   * @return {@code true} if the drop-down list is shown
   */
  public boolean isDroppedDown() {
    return this.isDroppedDown;
  }

  /**
   * Registers a callback that is invoked with the new selected index whenever the selection changes.
   *
   * @param c the change consumer
   */
  public void onChange(final IntConsumer c) {
    this.getChangeConsumer().add(c);
  }

  @Override
  public void prepare() {
    // ...existing code...
    this.contentList =
        new ListField(
            this.getX(),
            this.getY(),
            this.getWidth(),
            this.getHeight(),
            this.getContentArray(),
            this.numberOfShownElements);
    this.contentList.setButtonSprite(this.buttonSprite);
    this.contentList.setEntrySprite(this.entrySprite);
    this.chosenElementComponent =
        new ImageComponent(
            this.getX(),
            this.getY(),
            this.getWidth(),
            this.getHeight() / this.getNumberOfShownElements(),
            this.getEntrySprite(),
            "",
            null);
    this.chosenElementComponent.setTextAlign(Align.LEFT);
    final double buttonHeight = this.getHeight() / this.getNumberOfShownElements();
    double buttonWidth = buttonHeight;
    this.dropDownButton =
        new ImageComponent(
            this.getX() - buttonWidth,
            this.getY(),
            buttonWidth,
            buttonHeight,
            this.getButtonSprite(),
            ARROW_DOWN.getText(),
            null);
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

    this.onChange(
        c -> {
          this.chosenElementComponent.setText(this.getContentArray()[c].toString());
          if (this.getContentList().isSuspended() || !this.getContentList().isVisible()) {
            return;
          }
          this.toggleDropDown();
        });

    this.getContentList()
        .onChange(
            c -> this.getChangeConsumer()
                .forEach(consumer -> consumer.accept(this.getSelectedIndex())));
  }

  /**
   * Sets whether arrow-key (and mouse-wheel) navigation should be used to change the selection while the component is hovered.
   *
   * @param arrowKeyNavigation {@code true} to enable arrow-key navigation
   */
  public void setArrowKeyNavigation(final boolean arrowKeyNavigation) {
    this.arrowKeyNavigation = arrowKeyNavigation;
  }

  /**
   * Sets the spritesheet used to render the drop-down toggle button.
   *
   * @param buttonSprite the spritesheet
   */
  public void setButtonSprite(final Spritesheet buttonSprite) {
    this.buttonSprite = buttonSprite;
  }

  /**
   * Sets the spritesheet used to render the drop-down entries.
   *
   * @param entrySprite the spritesheet
   */
  public void setEntrySprite(final Spritesheet entrySprite) {
    this.entrySprite = entrySprite;
  }

  /**
   * Sets the selection by index.
   *
   * @param selectionIndex the index of the entry to select
   */
  public void setSelection(final int selectionIndex) {
    if (this.getContentList() == null) {
      return;
    }

    this.getContentList().setSelection(0, selectionIndex);
  }

  /**
   * Sets the selection by value. The first entry that {@linkplain Object#equals(Object) equals} the given object is selected.
   *
   * @param selectedObject the value to select; ignored if {@code null}
   */
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

  /** Toggle drop down. */
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
    Input.keyboard()
        .onKeyTyped(
            KeyEvent.VK_UP,
            e -> {
              if (this.isSuspended()
                  || !this.isVisible()
                  || !this.isArrowKeyNavigation()
                  || !this.getChosenElementComponent().isHovered()) {
                return;
              }
              this.getContentList().setSelection(0, this.getSelectedIndex() - 1);
            });

    Input.keyboard()
        .onKeyTyped(
            KeyEvent.VK_DOWN,
            e -> {
              if (this.isSuspended()
                  || !this.isVisible()
                  || !this.isArrowKeyNavigation()
                  || !this.getChosenElementComponent().isHovered()) {
                return;
              }
              this.getContentList().setSelection(0, this.getSelectedIndex() + 1);
            });

    this.onMouseWheelScrolled(
        e -> {
          if (this.isSuspended()
              || !this.isVisible()
              || !this.getChosenElementComponent().isHovered()) {
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
