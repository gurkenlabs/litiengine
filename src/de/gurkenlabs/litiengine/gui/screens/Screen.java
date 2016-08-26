/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui.screens;

import java.lang.annotation.AnnotationFormatError;

import de.gurkenlabs.litiengine.annotation.ScreenInfo;
import de.gurkenlabs.litiengine.gui.GuiComponent;

// TODO: Auto-generated Javadoc
/**
 * The Class Screen.
 *
 * @param <TData>
 *          the generic type
 */
public abstract class Screen extends GuiComponent implements IScreen {
  public static Screen GAME_SCREEN = new GameScreen();
  private final String name;

  /**
   * Instantiates a new screen.
   *
   * @param screenData
   *          the screen data
   */
  protected Screen() {
    super(0, 0);
    final ScreenInfo info = this.getClass().getAnnotation(ScreenInfo.class);
    if (info == null) {
      throw new AnnotationFormatError("No ScreenInfo annotation found on screen " + this.getClass());
    }

    this.name = info.name();
  }

  protected Screen(String screenName) {
    super(0, 0);
    this.name = screenName;
  }

  @Override
  public String getName() {
    return this.name;
  }
}
