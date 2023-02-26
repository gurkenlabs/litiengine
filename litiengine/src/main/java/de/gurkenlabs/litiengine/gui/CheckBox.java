package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class CheckBox extends ImageComponent {
  public static final FontIcon CHECK = new FontIcon(ICON_FONT, "\uE847");
  public static final FontIcon CROSS = new FontIcon(ICON_FONT, "\uE843");
  private final List<Consumer<Boolean>> changeConsumer;
  private boolean checked;

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

  public List<Consumer<Boolean>> getChangeConsumer() {
    return this.changeConsumer;
  }

  public boolean isChecked() {
    return this.checked;
  }

  public void onChange(final Consumer<Boolean> c) {
    this.getChangeConsumer().add(c);
  }

  public void setChecked(final boolean checked) {
    this.checked = checked;
    this.getChangeConsumer().forEach(consumer -> consumer.accept(this.isChecked()));
    this.refreshText();
  }

  private void refreshText() {
    if (this.checked) {
      this.setText(CHECK.getText());
    } else {
      this.setText(CROSS.getText());
    }
  }

  private void toggleChecked() {
    this.setChecked(!this.checked);
  }
}
