package de.gurkenlabs.utiliti.model;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import java.awt.Color;
import java.awt.Font;
import java.util.Objects;

/**
 * The Style class provides various constants and methods related to the visual style of the application. It includes color definitions, font
 * settings, and methods to retrieve scaled fonts.
 */
public final class Style {
  public enum Theme {
    LIGHT,
    DARK
  }

  // Tokyo Night-inspired color palette
  public static final Color COLOR_BG = new Color(26, 27, 38);
  public static final Color COLOR_SURFACE = new Color(36, 40, 59);
  public static final Color COLOR_BORDER = new Color(65, 72, 104);
  public static final Color COLOR_ACCENT_BLUE = new Color(122, 162, 247);
  public static final Color COLOR_ACCENT_CYAN = new Color(42, 195, 222);
  public static final Color COLOR_GREEN = new Color(158, 206, 106);
  public static final Color COLOR_ORANGE = new Color(224, 175, 104);
  public static final Color COLOR_RED = new Color(247, 118, 142);
  public static final Color COLOR_PURPLE = new Color(187, 154, 247);
  public static final Color COLOR_TEXT = new Color(192, 202, 245);
  public static final Color COLOR_SUBTEXT = new Color(169, 177, 214);
  public static final Color COLOR_COMMENT = new Color(86, 95, 137);

  public static final Color COLOR_DEFAULT_BOUNDING_BOX_FILL = new Color(0, 0, 0, 40);
  public static final Color COLOR_DARKBORDER = new Color(36, 40, 59, 200);
  public static final Color COLOR_DEFAULT_GRID = new Color(255, 255, 255, 65);
  public static final Color COLOR_COLLISION_FILL = new Color(247, 118, 142, 20);
  public static final Color COLOR_COLLISION_BORDER = COLOR_RED;
  public static final Color COLOR_NOCOLLISION_BORDER = new Color(247, 118, 142, 150);
  public static final Color COLOR_TRIGGER_BORDER = COLOR_ORANGE;
  public static final Color COLOR_TRIGGER_FILL = new Color(224, 175, 104, 20);
  public static final Color COLOR_SPAWNPOINT = COLOR_GREEN;

  public static final Color COLOR_LIGHT = Color.WHITE;
  public static final Color COLOR_UNSUPPORTED = new Color(187, 154, 247, 200);
  public static final Color COLOR_UNSUPPORTED_FILL = new Color(187, 154, 247, 20);
  public static final Color COLOR_NEWOBJECT_FILL = new Color(158, 206, 106, 50);
  public static final Color COLOR_NEWOBJECT_BORDER = COLOR_ACCENT_BLUE;
  public static final Color COLOR_TRANSFORM_RECT_FILL = new Color(122, 162, 247, 100);
  public static final Color COLOR_SHADOW_FILL = new Color(122, 162, 247, 20);
  public static final Color COLOR_SHADOW_BORDER = COLOR_BORDER;
  public static final Color COLOR_MOUSE_SELECTION_AREA_FILL = new Color(42, 195, 222, 50);
  public static final Color COLOR_MOUSE_SELECTION_AREA_BORDER = new Color(42, 195, 222, 120);
  public static final Color COLOR_DEFAULT_TAG = COLOR_COMMENT;
  public static final Color COLOR_STATUS = Color.WHITE;

  public static final float FONT_DEFAULT_SIZE = 13;
  public static final float FONT_HEADER_SIZE = 14;
  public static final String FONTNAME_CONSOLE = "Consolas";
  public static final Font FONT_BOLD =
    Resources.fonts().get("Roboto-Black.ttf", Font.BOLD, FONT_HEADER_SIZE);
  private static final Font FONT_DEFAULT =
    Resources.fonts().get("Roboto-Regular.ttf", Font.PLAIN, FONT_DEFAULT_SIZE);
  private static final Font FONT_HEADER =
    Resources.fonts().get("Roboto-Regular.ttf", Font.PLAIN, FONT_HEADER_SIZE);

  private static Font scaledDefaultFont;
  private static Font scaledHeaderFont;

  private Style() {
    throw new UnsupportedOperationException();
  }

  /**
   * Retrieves the default font, scaled according to the user's UI scale preference.
   *
   * @return The scaled default font.
   */
  public static Font getDefaultFont() {
    if (scaledDefaultFont == null) {
      scaledDefaultFont =
        Objects.requireNonNull(FONT_DEFAULT).deriveFont(FONT_DEFAULT_SIZE * Editor.preferences().getUiScale());
    }

    return scaledDefaultFont;
  }

  /**
   * Retrieves the header font, scaled according to the user's UI scale preference.
   *
   * @return The scaled header font.
   */
  public static Font getHeaderFont() {
    if (scaledHeaderFont == null) {
      scaledHeaderFont =
        Objects.requireNonNull(FONT_HEADER).deriveFont(FONT_HEADER_SIZE * Editor.preferences().getUiScale());
    }

    return scaledHeaderFont;
  }
}
