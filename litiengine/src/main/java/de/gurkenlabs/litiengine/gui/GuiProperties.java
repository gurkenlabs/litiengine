package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import java.awt.Color;
import java.awt.Font;

/**
 * This class contains globally used properties for all the {@link GuiComponent}s that might be added to the game.
 */
public class GuiProperties {
  /**
   * The default appearance for GUI components.
   */
  private static Appearance defaultAppearance = new Appearance(new Color(255, 255, 255));

  /**
   * The default appearance for disabled GUI components.
   */
  private static Appearance defaultAppearanceDisabled = new Appearance(new Color(136, 136, 136));

  /**
   * The default appearance for hovered GUI components.
   */
  private static Appearance defaultAppearanceHovered = new Appearance(new Color(200, 200, 200));

  /**
   * The default font for GUI components.
   */
  private static Font defaultFont;

  /**
   * The default horizontal alignment for text in GUI components.
   */
  private static Align defaultTextAlign = Align.CENTER;

  /**
   * The default vertical alignment for text in GUI components.
   */
  private static Valign defaultTextValign = Valign.MIDDLE;

  /**
   * Indicates whether text antialiasing is enabled by default.
   */
  private static boolean defaultTextAntialiasing = true;

  /**
   * Indicates whether text shadow is enabled by default.
   */
  private static boolean defaultTextShadow = false;

  /**
   * The default color for text shadows.
   */
  private static Color defaultTextShadowColor;

  /**
   * The default radius for text shadows.
   */
  private static float defaultTextShadowRadius = 2f;

  /**
   * The default display time for speech bubbles in milliseconds.
   */
  private static int defaultSpeechBubbleDisplayTime = 2000;

  /**
   * Private constructor to prevent instantiation.
   */
  private GuiProperties() {
  }

  /**
   * Gets the default appearance for GUI components.
   *
   * @return the default appearance
   */
  public static Appearance getDefaultAppearance() {
    return defaultAppearance;
  }

  /**
   * Sets the default appearance for GUI components.
   *
   * @param app the new default appearance
   */
  public static void setDefaultAppearance(Appearance app) {
    defaultAppearance = app;
  }

  /**
   * Gets the default appearance for hovered GUI components.
   *
   * @return the default appearance for hovered components
   */
  public static Appearance getDefaultAppearanceHovered() {
    return defaultAppearanceHovered;
  }

  /**
   * Gets the default appearance for disabled GUI components.
   *
   * @return the default appearance for disabled components
   */
  public static Appearance getDefaultAppearanceDisabled() {
    return defaultAppearanceDisabled;
  }

  /**
   * Sets the default appearance for disabled GUI components.
   *
   * @param app the new default appearance for disabled components
   */
  public static void setDefaultAppearanceDisabled(Appearance app) {
    defaultAppearanceDisabled = app;
  }

  /**
   * Sets the default appearance for hovered GUI components.
   *
   * @param app the new default appearance for hovered components
   */
  public static void setDefaultAppearanceHovered(Appearance app) {
    defaultAppearanceHovered = app;
  }

  /**
   * Gets the default font for GUI components.
   *
   * @return the default font
   */
  public static Font getDefaultFont() {
    return defaultFont;
  }

  /**
   * Sets the default font for GUI components.
   *
   * @param newFont the new default font
   */
  public static void setDefaultFont(Font newFont) {
    defaultFont = newFont;
  }

  /**
   * Gets whether text antialiasing is enabled by default.
   *
   * @return true if text antialiasing is enabled by default, false otherwise
   */
  public static boolean getDefaultTextAntialiasing() {
    return defaultTextAntialiasing;
  }

  /**
   * Sets whether text antialiasing is enabled by default.
   *
   * @param newDefault true to enable text antialiasing by default, false otherwise
   */
  public static void setDefaultTextAntialiasing(boolean newDefault) {
    defaultTextAntialiasing = newDefault;
  }

  /**
   * Gets whether text shadow is enabled by default.
   *
   * @return true if text shadow is enabled by default, false otherwise
   */
  public static boolean getDefaultTextShadow() {
    return defaultTextShadow;
  }

  /**
   * Sets whether text shadow is enabled by default.
   *
   * @param newDefault true to enable text shadow by default, false otherwise
   */
  public static void setDefaultTextShadow(boolean newDefault) {
    defaultTextShadow = newDefault;
  }

  /**
   * Gets the default color for text shadows.
   *
   * @return the default text shadow color
   */
  public static Color getDefaultTextShadowColor() {
    return defaultTextShadowColor;
  }

  /**
   * Sets the default color for text shadows.
   *
   * @param newDefault the new default text shadow color
   */
  public static void setDefaultTextShadowColor(Color newDefault) {
    defaultTextShadowColor = newDefault;
  }

  /**
   * Gets the default radius for text shadows.
   *
   * @return the default text shadow radius
   */
  public static float getDefaultTextShadowRadius() {
    return defaultTextShadowRadius;
  }

  /**
   * Sets the default radius for text shadows.
   *
   * @param newDefault the new default text shadow radius
   */
  public static void setDefaultTextShadowRadius(float newDefault) {
    defaultTextShadowRadius = newDefault;
  }

  /**
   * Gets the default horizontal alignment for text in GUI components.
   *
   * @return the default text alignment
   */
  public static Align getDefaultTextAlign() {
    return defaultTextAlign;
  }

  /**
   * Sets the default horizontal alignment for text in GUI components.
   *
   * @param newDefault the new default text alignment
   */
  public static void setDefaultTextAlign(Align newDefault) {
    defaultTextAlign = newDefault;
  }

  /**
   * Gets the default vertical alignment for text in GUI components.
   *
   * @return the default text vertical alignment
   */
  public static Valign getDefaultTextValign() {
    return defaultTextValign;
  }

  /**
   * Sets the default vertical alignment for text in GUI components.
   *
   * @param newDefault the new default text vertical alignment
   */
  public static void setDefaultTextValign(Valign newDefault) {
    defaultTextValign = newDefault;
  }

  /**
   * Gets the default display time for speech bubbles in milliseconds.
   *
   * @return the default speech bubble display time
   */
  public static int getDefaultSpeechBubbleDisplayTime() {
    return defaultSpeechBubbleDisplayTime;
  }

  /**
   * Sets the default display time for speech bubbles in milliseconds.
   *
   * @param newDefault the new default speech bubble display time
   */
  public static void setDefaultSpeechBubbleDisplayTime(int newDefault) {
    defaultSpeechBubbleDisplayTime = newDefault;
  }
}
