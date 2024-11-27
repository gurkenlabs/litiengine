package de.gurkenlabs.utiliti;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.components.Editor;
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

  public static final Color COLOR_DEFAULT_BOUNDING_BOX_FILL = new Color(0, 0, 0, 35);
  public static final Color COLOR_DARKBORDER = new Color(30, 30, 30, 200);
  public static final Color COLOR_DEFAULT_GRID = new Color(255, 255, 255, 65);
  public static final Color COLOR_COLLISION_FILL = new Color(255, 0, 0, 15);
  public static final Color COLOR_COLLISION_BORDER = Color.RED;
  public static final Color COLOR_NOCOLLISION_BORDER = new Color(255, 0, 0, 150);
  public static final Color COLOR_TRIGGER_BORDER = new Color(255, 190, 86);
  public static final Color COLOR_TRIGGER_FILL = new Color(255, 255, 0, 15);
  public static final Color COLOR_SPAWNPOINT = new Color(18, 186, 113);

  public static final Color COLOR_LIGHT = Color.WHITE;
  public static final Color COLOR_UNSUPPORTED = new Color(180, 180, 180, 200);
  public static final Color COLOR_UNSUPPORTED_FILL = new Color(180, 180, 180, 15);
  public static final Color COLOR_NEWOBJECT_FILL = new Color(0, 255, 0, 50);
  public static final Color COLOR_NEWOBJECT_BORDER = Color.GREEN.darker();
  public static final Color COLOR_TRANSFORM_RECT_FILL = new Color(255, 255, 255, 100);
  public static final Color COLOR_SHADOW_FILL = new Color(85, 130, 200, 15);
  public static final Color COLOR_SHADOW_BORDER = new Color(30, 85, 170);
  public static final Color COLOR_MOUSE_SELECTION_AREA_FILL = new Color(0, 130, 152, 80);
  public static final Color COLOR_MOUSE_SELECTION_AREA_BORDER = new Color(0, 130, 152, 150);
  public static final Color COLOR_DEFAULT_TAG = new Color(99, 113, 118);
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
