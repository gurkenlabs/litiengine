package de.gurkenlabs.utiliti.model;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Objects;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

/**
 * The Style class provides various constants and methods related to the visual style of the application. It includes color definitions, font
 * settings, and methods to retrieve scaled fonts.
 */
public final class Style {
  public enum Theme {
    LIGHT,
    DARK
  }

  // Tokyo Night-inspired color palette (2026 refinement)
  public static final Color COLOR_BG = new Color(18, 18, 20);
  public static final Color COLOR_SURFACE = new Color(30, 30, 35);
  public static final Color COLOR_SURFACE2 = new Color(40, 40, 46);
  public static final Color COLOR_BORDER = new Color(55, 55, 64);
  public static final Color COLOR_ACCENT_BLUE = new Color(53, 116, 242);
  public static final Color COLOR_ACCENT_CYAN = new Color(42, 195, 222);
  public static final Color COLOR_GREEN = new Color(158, 206, 106);
  public static final Color COLOR_ORANGE = new Color(224, 175, 104);
  public static final Color COLOR_RED = new Color(247, 118, 142);
  public static final Color COLOR_PURPLE = new Color(187, 154, 247);
  public static final Color COLOR_TEXT = new Color(200, 208, 245);
  public static final Color COLOR_SUBTEXT = new Color(150, 158, 185);
  public static final Color COLOR_COMMENT = new Color(86, 95, 137);

  public static final Color COLOR_SELECTION_INACTIVE = new Color(42, 45, 58);
  public static final Color COLOR_HEADER_COLLAPSED = new Color(24, 24, 28);

  public static final Color COLOR_DEFAULT_BOUNDING_BOX_FILL = new Color(0, 0, 0, 40);
  public static final Color COLOR_DARKBORDER = new Color(30, 30, 35, 220);
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
  public static final Color COLOR_DEFAULT_TAG = new Color(55, 65, 100);
  public static final Color COLOR_TAG_BORDER = new Color(80, 90, 130);
  public static final Color COLOR_TAG_HOVER = new Color(65, 75, 115);
  public static final Color COLOR_STATUS = Color.WHITE;

  // Semantic UI state colors
  public static final Color COLOR_INPUT_BG = new Color(36, 37, 42);
  public static final Color COLOR_HOVER = new Color(38, 42, 52);
  public static final Color COLOR_PLACEHOLDER = new Color(74, 74, 74);
  public static final Color COLOR_SELECT = new Color(59, 66, 97);
  public static final Color COLOR_DISABLED_TEXT = new Color(98, 104, 128);
  public static final Color COLOR_SCROLLBAR_THUMB = new Color(65, 65, 75);
  public static final Color COLOR_ROW_HOVER = new Color(28, 31, 40);
  public static final Color COLOR_BADGE_ID = new Color(42, 65, 112);
  public static final Color COLOR_CARD_HOVER = new Color(255, 255, 255, 12);
  public static final Color COLOR_CARD_SELECTED = new Color(53, 116, 242, 45);

  // --- Shared icon button hover painting ---

  public static void paintButtonBackground(Component c, javax.swing.ButtonModel model, Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      boolean selected = model.isSelected();
      boolean active = selected || model.isPressed();
      Color fill = active ? COLOR_ACCENT_BLUE : model.isRollover() ? COLOR_HOVER : COLOR_SURFACE;
      Color border = selected ? COLOR_ACCENT_BLUE : COLOR_BORDER;
      g2.setColor(fill);
      g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 8, 8);
      g2.setColor(border);
      g2.setStroke(new java.awt.BasicStroke(1.2f));
      g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 8, 8);
    } finally {
      g2.dispose();
    }
  }

  public static JButton iconButton(Icon icon) {
    JButton button = new JButton(icon) {
      @Override protected void paintComponent(Graphics g) {
        paintButtonBackground(this, getModel(), g);
        super.paintComponent(g);
      }
    };
    styleIconButton(button);
    return button;
  }

  public static JButton iconButton(Action action) {
    JButton button = new JButton(action) {
      @Override protected void paintComponent(Graphics g) {
        paintButtonBackground(this, getModel(), g);
        super.paintComponent(g);
      }
    };
    styleIconButton(button);
    return button;
  }

  public static JToggleButton iconToggleButton(Icon icon, boolean selected) {
    JToggleButton button = new JToggleButton(icon, selected) {
      @Override protected void paintComponent(Graphics g) {
        paintButtonBackground(this, getModel(), g);
        super.paintComponent(g);
      }
    };
    styleIconButton(button);
    return button;
  }

  private static void styleIconButton(javax.swing.AbstractButton button) {
    button.setFocusable(false);
    button.setOpaque(false);
    button.setContentAreaFilled(false);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setMargin(new java.awt.Insets(2, 2, 2, 2));
  }

  /**
   * Creates a ghost/clear button with no visible border by default, only a subtle hover fill.
   * Used for inline clear (X) buttons inside search boxes.
   */
  public static JButton clearButton(Icon icon) {
    JButton button = new JButton(icon) {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          if (getModel().isRollover() || getModel().isPressed()) {
            g2.setColor(COLOR_HOVER);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
          }
        } finally {
          g2.dispose();
        }
        super.paintComponent(g);
      }
    };
    styleIconButton(button);
    return button;
  }

  public static final float FONT_DEFAULT_SIZE = 12;
  public static final float FONT_HEADER_SIZE = 12;
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
