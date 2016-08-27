/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui;

import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.sound.Sound;

// TODO: Auto-generated Javadoc
/**
 * The Class Menu.
 */
public class Menu extends ImageComponentList {

  /** The menu buttons. */
  private final String[] items;

  public Menu(final int x, final int y, final int width, final int height, final String[] items, final Spritesheet background, final Sound hoverSound) {
    super(x, y, width, height, items.length, 1, null, background,hoverSound);
    this.items = items;
    for (int i = 0; i < this.items.length; i++) {
      final ImageComponent menuButton = this.getCellComponents().get(i);
      menuButton.setText(items[i]);
      this.getComponents().add(menuButton);
    }
  }

  @Override
  public void render(final Graphics2D g) {
    super.render(g);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.gui.GuiComponent#initializeComponents()
   */
  @Override
  protected void initializeComponents() {

  }
}
