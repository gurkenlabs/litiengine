package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;

import java.awt.Color;
import java.awt.Font;

/**
 * This class contains globally used properties for all the {@link GuiComponent}s that might be
 * added to the game.
 */
public class GuiProperties {
  private static Appearance defaultAppearance = new Appearance(new Color(255, 255, 255));
  private static Appearance defaultAppearanceDisabled = new Appearance(new Color(136, 136, 136));
  private static Appearance defaultAppearanceHovered = new Appearance(new Color(200, 200, 200));

  private static Font defaultFont;
  private static Align defaultTextAlign = Align.CENTER;
  private static Valign defaultTextValign = Valign.MIDDLE;

  private static boolean defaultTextAntialiasing = true;

  private static boolean defaultTextShadow = false;
  private static Color defaultTextShadowColor;
  private static float defaultTextShadowRadius = 2f;
  private static int defaultSpeechBubbleDisplayTime = 2000;

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

  public static boolean getDefaultTextAntialiasing() {
    return defaultTextAntialiasing;
  }

  public static void setDefaultTextAntialiasing(boolean newDefault) {
    defaultTextAntialiasing = newDefault;
  }

  public static boolean getDefaultTextShadow() {
    return defaultTextShadow;
  }

  public static void setDefaultTextShadow(boolean newDefault) {
    defaultTextShadow = newDefault;
  }

  public static Color getDefaultTextShadowColor() {
    return defaultTextShadowColor;
  }

  public static void setDefaultTextShadowColor(Color newDefault) {
    defaultTextShadowColor = newDefault;
  }

  public static float getDefaultTextShadowRadius() {
    return defaultTextShadowRadius;
  }

  public static void setDefaultTextShadowRadius(float newDefault) {
    defaultTextShadowRadius = newDefault;
  }

  public static Align getDefaultTextAlign() {
    return defaultTextAlign;
  }

  public static void setDefaultTextAlign(Align newDefault) {
    defaultTextAlign = newDefault;
  }

  public static Valign getDefaultTextValign() {
    return defaultTextValign;
  }

  public static void setDefaultTextValign(Valign newDefault) {
    defaultTextValign = newDefault;
  }

  public static int getDefaultSpeechBubbleDisplayTime() {
    return defaultSpeechBubbleDisplayTime;
  }

  public static void setDefaultSpeechBubbleDisplayTime(int newDefault) {
    defaultSpeechBubbleDisplayTime = newDefault;
  }
}
