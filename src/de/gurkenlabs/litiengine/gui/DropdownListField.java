package de.gurkenlabs.litiengine.gui;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.sound.Sound;

public class DropdownListField extends ListField {
  /** The drop down button. */
  private final ImageComponent dropDownButton, chosenElementComponent;
  private boolean isDroppedDown;

  public DropdownListField(final int x, final int y, final int width, final int height, final Object[] content, final Spritesheet entrySprite, final Spritesheet buttonSprite, final Sound hoverSound) {
    super(x, y, width, height, content, entrySprite, null, hoverSound);
    this.chosenElementComponent = new ImageComponent(this.getX(), this.getY(), this.getWidth(), this.getHeight() / this.getListEntries().size(), entrySprite, "", null, hoverSound);
    this.dropDownButton = new ImageComponent(this.getX() + this.getWidth(), this.getY(), this.getHeight() / this.getListEntries().size(), this.getHeight() / this.getListEntries().size(), buttonSprite, "", null, hoverSound);

    this.getComponents().add(this.dropDownButton);
    this.getComponents().add(this.chosenElementComponent);
  }

  @Override
  public void render(final Graphics2D g) {
    this.dropDownButton.render(g);
    if (!this.isDroppedDown) {
      this.chosenElementComponent.render(g);
    } else {
      for (final ImageComponent c : this.getListEntries()) {
        c.render(g);
      }
      super.render(g);
    }
  }

  /**
   * Toggle drop down.
   */
  public void toggleDropDown() {
    this.isDroppedDown = !this.isDroppedDown;
    this.chosenElementComponent.setVisible(!this.chosenElementComponent.isVisible());
    for (final ImageComponent e : this.getListEntries()) {
      e.setVisible(!e.isVisible());
    }
  }

  @Override
  public void prepare() {
    super.prepare();
    this.chosenElementComponent.setText(this.getListEntries().get(this.getSelection()).getText());
    this.chosenElementComponent.setVisible(true);
    this.dropDownButton.onClicked(e -> this.toggleDropDown());
    this.dropDownButton.setFont(FontLoader.getIconFontOne().deriveFont((int) this.dropDownButton.getWidth() / 1.5f));
    this.dropDownButton.setText(Icon.ARROW_DOWN.getText());
    for (final ImageComponent comp : this.getListEntries()) {
      comp.onClicked(e -> {
        if (this.isDroppedDown) {
          this.chosenElementComponent.setText(comp.getText());
          this.toggleDropDown();
        }
      });

      comp.setVisible(false);
    }
  }

}
