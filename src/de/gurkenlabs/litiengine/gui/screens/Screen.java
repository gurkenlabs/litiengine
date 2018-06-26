package de.gurkenlabs.litiengine.gui.screens;

import de.gurkenlabs.litiengine.gui.GuiComponent;

public abstract class Screen extends GuiComponent implements IScreen {
  private final String name;

  protected Screen(final String screenName) {
    super(0, 0);
    this.name = screenName;
  }

  @Override
  public String getName() {
    return this.name;
  }
}
