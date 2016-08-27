/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.gurkenlabs.litiengine.graphics.RenderEngine;

// TODO: Auto-generated Javadoc
/**
 * The Class Icon.
 */
public class Icon {

  /** The arrow down. */
  public static Icon ARROW_DOWN = new Icon(FontLoader.getIconFontOne(), "B");

  /** The arrow left. */
  public static Icon ARROW_LEFT = new Icon(FontLoader.getIconFontOne(), "D");

  /** The arrow right. */
  public static Icon ARROW_RIGHT = new Icon(FontLoader.getIconFontOne(), "C");

  /** The arrow up. */
  // Icon Font One
  public static Icon ARROW_UP = new Icon(FontLoader.getIconFontOne(), "A");

  /** The atom. */
  public static Icon ATOM = new Icon(FontLoader.getIconFontTwo(), "e");

  /** The check. */
  public static Icon CHECK = new Icon(FontLoader.getIconFontTwo(), "0");

  /** The claw. */
  public static Icon CLAW = new Icon(FontLoader.getAnimalIconFont(), "c");

  /** The computer. */
  // Icon Font Two
  public static Icon COMPUTER = new Icon(FontLoader.getIconFontTwo(), "Q");

  /** The heal empty. */
  public static Icon HEAL_EMPTY = new Icon(FontLoader.getIconFontOne(), "z");

  /** The heal filled. */
  public static Icon HEAL_FILLED = new Icon(FontLoader.getIconFontOne(), "y");

  /** The heart empty. */
  public static Icon HEART_EMPTY = new Icon(FontLoader.getIconFontOne(), "v");

  /** The heart filled. */
  public static Icon HEART_FILLED = new Icon(FontLoader.getIconFontOne(), "w");

  /** The light bulb. */
  public static Icon LIGHT_BULB = new Icon(FontLoader.getIconFontTwo(), "y");

  /** The light bulb two. */
  public static Icon LIGHT_BULB_TWO = new Icon(FontLoader.getIconFontTwo(), "z");

  /** The lightning. */
  public static Icon LIGHTNING = new Icon(FontLoader.getIconFontTwo(), "u");

  /** The microphone. */
  public static Icon MICROPHONE = new Icon(FontLoader.getIconFontTwo(), "i");

  /** The note. */
  public static Icon NOTE = new Icon(FontLoader.getIconFontTwo(), "g");

  /** The note two. */
  public static Icon NOTE_TWO = new Icon(FontLoader.getIconFontTwo(), "h");

  /** The panther claw. */
  public static Icon PANTHER_CLAW = new Icon(FontLoader.getAnimalIconFont(), "g");

  /** The skull. */
  public static Icon SKULL = new Icon(FontLoader.getIconFontTwo(), "f");

  /** The sound. */
  public static Icon SOUND = new Icon(FontLoader.getIconFontOne(), "k");

  /** The zoom in. */
  public static Icon ZOOM_IN = new Icon(FontLoader.getIconFontTwo(), "j");

  /** The zoom out. */
  public static Icon ZOOM_OUT = new Icon(FontLoader.getIconFontTwo(), "k");

  /** The font. */
  private final Font font;

  /** The text. */
  private final String text;

  /**
   * Instantiates a new icon.
   *
   * @param font
   *          the font
   * @param text
   *          the text
   */
  private Icon(final Font font, final String text) {
    this.font = font;
    this.text = text;
  }

  /**
   * Gets the font.
   *
   * @return the font
   */
  public Font getFont() {
    return this.font;
  }

  /**
   * Gets the text.
   *
   * @return the text
   */
  public String getText() {
    return this.text;
  }

  /**
   * Render.
   *
   * @param g
   *          the g
   * @param color
   *          the color
   * @param fontSize
   *          the font size
   * @param x
   *          the x
   * @param y
   *          the y
   * @param bold
   *          the bold
   */
  public void render(final Graphics2D g, final Color color, final float fontSize, final int x, final int y, final boolean bold) {
    final Color oldColor = g.getColor();
    final Font oldFont = g.getFont();
    g.setColor(color);
    if (bold) {
      g.setFont(this.getFont().deriveFont(Font.BOLD, fontSize));
    } else {
      g.setFont(this.getFont().deriveFont(fontSize));
    }
    RenderEngine.drawText(g, this.getText(), x, y);
    g.setColor(oldColor);
    g.setFont(oldFont);
  }
}
