package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A GUI component representing a checkbox with an optional spritesheet.
 */
public class CheckBox extends ImageComponent {
  /** The icon representing a checked state. */
  public static final FontIcon CHECK = new FontIcon(ICON_FONT, "\uE847");
  /** The icon representing an unchecked state. */
  public static final FontIcon CROSS = new FontIcon(ICON_FONT, "\uE843");
  /** A list of consumers to be notified when the checked state changes. */
  private final List<Consumer<Boolean>> changeConsumer;
  /** The current checked state of the checkbox. */
  private boolean checked;

  /**
   * Constructs a new CheckBox.
   *
   * @param x the x-coordinate of the checkbox
   * @param y the y-coordinate of the checkbox
   * @param width the width of the checkbox
   * @param height the height of the checkbox
   * @param spritesheet the spritesheet for the checkbox
   * @param checked the initial checked state of the checkbox
   */
  public CheckBox(
    final double x,
    final double y,
    final double width,
    final double height,
    final Spritesheet spritesheet,
    final boolean checked) {
    super(x, y, width, height, spritesheet, "", null);
    this.changeConsumer = new CopyOnWriteArrayList<>();
    this.setFont(CHECK.getFont());
    this.setChecked(checked);
    this.refreshText();
    this.onClicked(e -> this.toggleChecked());
  }

  /**
   * Gets the list of consumers to be notified when the checked state changes.
   *
   * @return the list of change consumers
   */
  public List<Consumer<Boolean>> getChangeConsumer() {
    return this.changeConsumer;
  }

  /**
   * Checks if the checkbox is currently checked.
   *
   * @return true if the checkbox is checked, false otherwise
   */
  public boolean isChecked() {
    return this.checked;
  }

  /**
   * Adds a consumer to be notified when the checked state changes.
   *
   * @param c the consumer to be added
   */
  public void onChange(final Consumer<Boolean> c) {
    this.getChangeConsumer().add(c);
  }

  /**
   * Sets the checked state of the checkbox.
   *
   * @param checked the new checked state
   */
  public void setChecked(final boolean checked) {
    this.checked = checked;
    this.getChangeConsumer().forEach(consumer -> consumer.accept(this.isChecked()));
    this.refreshText();
  }

  /**
   * Updates the text of the checkbox based on its checked state.
   */
  private void refreshText() {
    if (this.checked) {
      this.setText(CHECK.getText());
    } else {
      this.setText(CROSS.getText());
    }
  }

  /**
   * Toggles the checked state of the checkbox.
   */
  private void toggleChecked() {
    this.setChecked(!this.checked);
  }
}
