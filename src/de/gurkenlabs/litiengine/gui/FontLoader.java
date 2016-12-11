/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.util.io.FileUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class FontLoader.
 */
public class FontLoader {
  private static final Logger log = Logger.getLogger(FontLoader.class.getName());
  /** The Constant fonts. */
  private static final HashMap<String, Font> fonts = new HashMap<String, Font>();

  /**
   * Draw icon.
   *
   * @param g
   *          the g
   * @param icon
   *          the icon
   * @param color
   *          the color
   * @param size
   *          the size
   * @param x
   *          the x
   * @param y
   *          the y
   */
  public static void drawIcon(final Graphics2D g, final Icon icon, final Color color, final float size, final double x, final double y) {
    // TODO: Improve accuracy by using Affine transform instead of int
    // coordinates
    final Font oldFont = g.getFont();
    final Color oldColor = g.getColor();

    g.setFont(icon.getFont().deriveFont(size));
    g.setColor(color);
    RenderEngine.drawText(g, icon.getText(), (int) x, (int) y);

    // reset graphics to previous state settings
    g.setFont(oldFont);
    g.setColor(oldColor);
  }

  /**
   * Gets the animal icon font.
   *
   * @return the animal icon font
   */
  public static Font getAnimalIconFont() {
    return getFont("animal.ttf", 16f);
  }

  /**
   * Gets the default font.
   *
   * @return the default font
   */
  public static Font getDefaultFont() {
    return new Font("Arial", Font.PLAIN, 12);
  }

  /**
   * Gets the gui font.
   *
   * @return the gui font
   */
  public static Font getGuiFont() {
    return getFont("04B_08.ttf", 8f);
  }

  /**
   * Gets the icon font one.
   *
   * @return the icon font one
   */
  public static Font getIconFontOne() {
    return getFont("icon-bit-one.ttf", 16f);
  }

  /**
   * Gets the icon font two.
   *
   * @return the icon font two
   */
  public static Font getIconFontTwo() {
    return getFont("icon-bit-two.ttf", 16f);
  }
  
  public static Font getIconFontThree() {
    return getFont("fontello.ttf", 16f);
  }

  /**
   * Gets the menu font1.
   *
   * @return the menu font1
   */
  public static Font getMenuFont1() {
    return getFont("04B_08.ttf", 16f);
  }

  public static Font getSmallDefaultFont() {
    return new Font("Arial", Font.PLAIN, 5);
  }

  /**
   * Gets the title font.
   *
   * @return the title font
   */
  public static Font getTitleFont() {
    return getFont("04B_30.ttf", 45f);
  }

  /**
   * Gets the font.
   *
   * @param fontName
   *          the font name
   * @param fontSize
   *          the font size
   * @return the font
   */
  private static Font getFont(final String fontName, final float fontSize) {
    if (fonts.containsKey(fontName)) {
      return fonts.get(fontName).deriveFont(fontSize);
    }

    try {
      final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

      InputStream fontStream = FileUtilities.getGameFile(fontName);
      if(fontStream == null){
        log.severe("font '" + fontName +"' could not be loaded");
      }

      final Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
      ge.registerFont(font);
      fonts.put(fontName, font);
      return font.deriveFont(fontSize);
    } catch (final FontFormatException e) {
      final StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      final String stacktrace = sw.toString();
      log.severe(stacktrace);
      e.printStackTrace();
    } catch (final IOException e) {
      final StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      final String stacktrace = sw.toString();
      log.severe(stacktrace);
      e.printStackTrace();
    }
    return null;
  }
}
