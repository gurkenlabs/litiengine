package de.gurkenlabs.litiengine.gui.screens;

import de.gurkenlabs.litiengine.gui.GuiComponent;

public abstract class Screen extends GuiComponent {
  protected Screen(final String screenName) {
    super(0, 0);
    this.setName(screenName);
  }

  @Override
  public void setX(final double x) {
    // do nothing because screens always start at 0/0
  }

  @Override
  public void setY(final double y) {
    // do nothing because screens always start at 0/0
  }
}
