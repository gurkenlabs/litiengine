package com.litiengine.gui;

import java.awt.Color;
import java.awt.Font;

/**
 * This class contains globally used properties for all the
 * {@link GuiComponent}s that might be added to the game.
 *
 */
public class GuiProperties {
  private static Appearance defaultAppearance = new Appearance(new Color(255, 255, 255));
  private static Appearance defaultAppearanceDisabled = new Appearance(new Color(136, 136, 136));
  private static Appearance defaultAppearanceHovered = new Appearance(new Color(200, 200, 200));
  
  private static Font defaultFont;

  private GuiProperties() {
  }

  public static Appearance getDefaultAppearance() {
    return defaultAppearance;
  }

  public static void setDefaultAppearance(Appearance app) {
    defaultAppearance = app;
  }

  public static Appearance getDefaultAppearanceHovered() {
    return defaultAppearanceHovered;
  }

  public static Appearance getDefaultAppearanceDisabled() {
    return defaultAppearanceDisabled;
  }

  public static void setDefaultAppearanceDisabled(Appearance app) {
    defaultAppearanceDisabled = app;
  }

  public static void setDefaultAppearanceHovered(Appearance app) {
    defaultAppearanceHovered = app;
  }

  public static Font getDefaultFont() {
    return defaultFont;
  }

  public static void setDefaultFont(Font newFont) {
    defaultFont = newFont;
  }
}