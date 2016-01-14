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

  private final String name;

  /**
   * Instantiates a new screen.
   *
   * @param screenData
   *          the screen data
   */
  protected Screen(int width, int height) {
    super(0, 0, width, height);
    ScreenInfo info = this.getClass().getAnnotation(ScreenInfo.class);
    if (info == null) {
      throw new AnnotationFormatError("No ScreenInfo annotation found on screen " + this.getClass());
    }
    
    this.name = info.name();
  }


  @Override
  public String getName() {
    return this.name;
  }
}
