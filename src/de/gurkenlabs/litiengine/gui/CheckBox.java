package de.gurkenlabs.litiengine.gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.Spritesheet;

public class CheckBox extends ImageComponent {
  private boolean checked;
  public static Icon CHECK = new Icon(FontLoader.getIconFontThree(), "\uE847");
  public static Icon CROSS = new Icon(FontLoader.getIconFontThree(), "\uE843");
  private final List<Consumer<Boolean>> changeConsumer;

  public CheckBox(double x, double y, double width, double height, Spritesheet spritesheet, boolean checked) {
    super(x, y, width, height, spritesheet, "", null);
    this.changeConsumer = new CopyOnWriteArrayList<Consumer<Boolean>>();
    this.setFont(CHECK.getFont());
    this.setChecked(checked);
    this.refreshText();
    this.onClicked(e -> {
      this.toggleChecked();
    });

  }

  public List<Consumer<Boolean>> getChangeConsumer() {
    return this.changeConsumer;
  }

  private void toggleChecked() {
    this.setChecked(!this.checked);
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
    this.getChangeConsumer().forEach(consumer -> consumer.accept(this.isChecked()));
    this.refreshText();
  }

  public boolean isChecked() {
    return this.checked;
  }

  public void onChange(final Consumer<Boolean> c) {
    this.getChangeConsumer().add(c);
  }

  private void refreshText() {
    if (this.checked) {
      this.setText(CHECK.getText());
    } else {
      this.setText(CROSS.getText());
    }
  }
}
