package de.gurkenlabs.utiliti.model;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javax.swing.Action;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

/**
 * The Style class provides various constants and methods related to the visual style of the application. It includes color definitions, font
 * settings, and methods to retrieve scaled fonts.
 */
public final class Style {
  public enum ButtonVariant {
    PRIMARY,
    SECONDARY,
    TOOLBAR,
    GHOST,
    DESTRUCTIVE
  }

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
  public static final Color COLOR_TRANSPARENT = new Color(0, 0, 0, 0);
  public static final Color COLOR_DISABLED_OVERLAY = new Color(0, 0, 0, 80);
  public static final Color COLOR_LIGHT_GRID = new Color(220, 220, 220);
  public static final Color COLOR_SCENE_ROW_HOVER = new Color(COLOR_BG.getRed(), COLOR_BG.getGreen(), COLOR_BG.getBlue(), 200);
  public static final Color COLOR_SCENE_ROW_SELECTED = new Color(COLOR_ACCENT_BLUE.getRed(), COLOR_ACCENT_BLUE.getGreen(), COLOR_ACCENT_BLUE.getBlue(), 30);
  public static final Color COLOR_WORKSPACE_TOP = new Color(24, 24, 28);
  public static final Color COLOR_WORKSPACE_BOTTOM = new Color(14, 14, 17);
  public static final Color COLOR_ASSET_EXPLORER = new Color(14, 14, 16);
  public static final Color COLOR_ASSET_EXPLORER_LIGHT = new Color(245, 245, 247);
  public static final Color COLOR_MAP_BACKING = new Color(10, 10, 12);
  public static final Color COLOR_MAP_BORDER = new Color(92, 92, 104, 180);

  public static final int CONTROL_HEIGHT = 28;
  public static final int TOOLBAR_BUTTON_SIZE = 28;
  public static final int TREE_ROW_HEIGHT = 26;
  public static final int ICON_SIZE = 16;
  public static final int CORNER_RADIUS = 6;
  public static final int SPACE_SMALL = 4;
  public static final int SPACE_MEDIUM = 8;
  public static final int SPACE_LARGE = 12;

  // --- Shared icon button hover painting ---

  public static void paintButtonBackground(Component c, javax.swing.ButtonModel model, Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      boolean enabled = c.isEnabled();
      boolean selected = model.isSelected();
      boolean pressed = model.isPressed();
      boolean grouped = c instanceof javax.swing.JComponent component
          && Boolean.TRUE.equals(component.getClientProperty("Editor.groupedToolbarButton"));
      boolean subtleToggle = c instanceof javax.swing.JComponent component
          && Boolean.TRUE.equals(component.getClientProperty("Editor.subtleToolbarToggle"));
      ButtonVariant variant = c instanceof javax.swing.JComponent component
          && component.getClientProperty("Editor.buttonVariant") instanceof ButtonVariant value
          ? value
          : ButtonVariant.TOOLBAR;
      if (variant == ButtonVariant.DESTRUCTIVE && c instanceof AbstractButton button) {
        button.setForeground(enabled ? COLOR_RED : COLOR_DISABLED_TEXT);
      }
      Color surface = surface();
      Color hover = hover();
      Color accent = accent();
      Color fill = switch (variant) {
        case PRIMARY -> enabled ? (pressed ? accent.darker() : accent) : surface;
        case DESTRUCTIVE -> enabled && (selected || pressed)
            ? COLOR_RED.darker()
            : enabled && model.isRollover() ? new Color(COLOR_RED.getRed(), COLOR_RED.getGreen(), COLOR_RED.getBlue(), 55) : surface;
        case GHOST -> pressed || selected ? selection() : model.isRollover() ? hover : COLOR_TRANSPARENT;
        case SECONDARY, TOOLBAR -> selected ? accent : pressed ? selection() : model.isRollover() ? hover : surface;
      };
      if (selected && subtleToggle) {
        fill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 48);
      }
      Color border = switch (variant) {
        case PRIMARY -> accent;
        case DESTRUCTIVE -> enabled ? COLOR_RED : border();
        case GHOST -> COLOR_TRANSPARENT;
        case SECONDARY, TOOLBAR -> selected ? accent : border();
      };
      if (grouped && variant != ButtonVariant.DESTRUCTIVE) {
        border = COLOR_TRANSPARENT;
      } else if (grouped && !enabled) {
        border = COLOR_TRANSPARENT;
      }
      int inset = grouped ? 2 : 0;
      int arc = CORNER_RADIUS * 2;
      if (fill.getAlpha() > 0) {
        g2.setColor(fill);
        g2.fillRoundRect(
            inset,
            inset,
            c.getWidth() - 1 - inset * 2,
            c.getHeight() - 1 - inset * 2,
            arc,
            arc);
      }
      if (border.getAlpha() > 0) {
        g2.setColor(border);
        g2.setStroke(new java.awt.BasicStroke(1f));
        g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, arc, arc);
      }
      if (!enabled) {
        g2.setColor(COLOR_DISABLED_OVERLAY);
        g2.fillRoundRect(
            inset,
            inset,
            c.getWidth() - 1 - inset * 2,
            c.getHeight() - 1 - inset * 2,
            arc,
            arc);
      }
      if (enabled && c.isFocusOwner() && !subtleToggle) {
        g2.setColor(accent);
        g2.setStroke(new java.awt.BasicStroke(2f));
        g2.drawRoundRect(2, 2, c.getWidth() - 5, c.getHeight() - 5, arc - 2, arc - 2);
      }
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
    styleButton(button, ButtonVariant.TOOLBAR);
    return button;
  }

  public static JButton iconButton(Action action) {
    JButton button = new JButton(action) {
      @Override protected void paintComponent(Graphics g) {
        paintButtonBackground(this, getModel(), g);
        super.paintComponent(g);
      }
    };
    styleButton(button, ButtonVariant.TOOLBAR);
    return button;
  }

  public static JButton textButton(String text) {
    JButton button = new JButton(text) {
      @Override protected void paintComponent(Graphics g) {
        paintButtonBackground(this, getModel(), g);
        super.paintComponent(g);
      }
    };
    styleButton(button, ButtonVariant.SECONDARY);
    Dimension size = new Dimension(CONTROL_HEIGHT, CONTROL_HEIGHT);
    button.setPreferredSize(size);
    button.setMinimumSize(size);
    button.setMaximumSize(size);
    button.setForeground(COLOR_TEXT);
    button.setFont(button.getFont().deriveFont(16f));
    button.setMargin(new Insets(0, 0, 0, 0));
    return button;
  }

  public static JToggleButton iconToggleButton(Icon icon, boolean selected) {
    JToggleButton button = new JToggleButton(icon, selected) {
      @Override protected void paintComponent(Graphics g) {
        paintButtonBackground(this, getModel(), g);
        super.paintComponent(g);
      }
    };
    styleButton(button, ButtonVariant.TOOLBAR);
    return button;
  }

  public static void styleButton(AbstractButton button, ButtonVariant variant) {
    button.putClientProperty("Editor.buttonVariant", variant);
    if (variant == ButtonVariant.DESTRUCTIVE
        && button.getIcon() != null
        && !(button.getIcon() instanceof ForegroundIcon)) {
      button.setIcon(new ForegroundIcon(button.getIcon()));
    }
    button.setFocusable(true);
    button.setRequestFocusEnabled(true);
    button.setOpaque(false);
    button.setContentAreaFilled(false);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setMargin(new Insets(2, 2, 2, 2));
    button.setForeground(variant == ButtonVariant.PRIMARY ? Color.WHITE : text());
    Dimension size = new Dimension(TOOLBAR_BUTTON_SIZE, TOOLBAR_BUTTON_SIZE);
    button.setPreferredSize(size);
    button.setMinimumSize(size);
    button.setMaximumSize(size);
    updateAccessibleName(button, null);
    if (!Boolean.TRUE.equals(button.getClientProperty("Editor.accessibleNameListener"))) {
      button.putClientProperty("Editor.accessibleNameListener", true);
      button.addPropertyChangeListener("ToolTipText", event -> updateAccessibleName(button, event.getOldValue()));
    }
  }

  private static void updateAccessibleName(AbstractButton button, Object previousTooltip) {
    String current = button.getAccessibleContext().getAccessibleName();
    Object generatedValue = button.getClientProperty("Editor.generatedAccessibleName");
    String generated = generatedValue instanceof String value ? value : null;
    if (current != null && !current.isBlank()
        && !Objects.equals(current, previousTooltip) && !Objects.equals(current, generated)
        && !Objects.equals(current, button.getText())) {
      return;
    }
    String name = button.getToolTipText();
    if (name == null || name.isBlank()) {
      name = button.getText();
    }
    if ((name == null || name.isBlank()) && button.getAction() != null) {
      Object actionName = button.getAction().getValue(Action.NAME);
      name = actionName instanceof String value ? value : null;
    }
    if ("+".equals(name)) {
      name = Resources.strings().get("accessibility_add");
    } else if ("-".equals(name) || "−".equals(name)) {
      name = Resources.strings().get("accessibility_remove");
    }
    if (name != null && !name.isBlank()) {
      button.getAccessibleContext().setAccessibleName(name);
      button.putClientProperty("Editor.generatedAccessibleName", name);
    }
  }

  private static final class ForegroundIcon implements Icon {
    private final Icon delegate;

    private ForegroundIcon(Icon delegate) {
      this.delegate = delegate;
    }

    @Override
    public int getIconWidth() {
      return this.delegate.getIconWidth();
    }

    @Override
    public int getIconHeight() {
      return this.delegate.getIconHeight();
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      BufferedImage image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D imageGraphics = image.createGraphics();
      try {
        this.delegate.paintIcon(component, imageGraphics, 0, 0);
        imageGraphics.setComposite(AlphaComposite.SrcIn);
        imageGraphics.setColor(component.getForeground());
        imageGraphics.fillRect(0, 0, image.getWidth(), image.getHeight());
      } finally {
        imageGraphics.dispose();
      }
      graphics.drawImage(image, x, y, null);
    }
  }

  /**
   * Creates a ghost/clear button with no visible border by default, only a subtle hover fill.
   * Used for inline clear (X) buttons inside search boxes.
   */
  public static JButton clearButton(Icon icon) {
    JButton button = new JButton(icon) {
      @Override protected void paintComponent(Graphics g) {
        paintButtonBackground(this, getModel(), g);
        super.paintComponent(g);
      }
    };
    styleButton(button, ButtonVariant.GHOST);
    return button;
  }

  public static Color background() {
    return uiColor("Panel.background", COLOR_BG);
  }

  public static Color surface() {
    return uiColor("Editor.surface", COLOR_SURFACE);
  }

  public static Color raisedSurface() {
    return uiColor("Editor.surfaceRaised", COLOR_SURFACE2);
  }

  public static Color border() {
    return uiColor("Editor.border", COLOR_BORDER);
  }

  public static Color text() {
    return uiColor("Label.foreground", COLOR_TEXT);
  }

  public static Color mutedText() {
    return uiColor("Editor.mutedText", COLOR_SUBTEXT);
  }

  public static Color accent() {
    return uiColor("Editor.accent", COLOR_ACCENT_BLUE);
  }

  public static Color hover() {
    return uiColor("Editor.hover", COLOR_HOVER);
  }

  public static Color selection() {
    return uiColor("Editor.selection", COLOR_SELECTION_INACTIVE);
  }

  public static Color sceneRowSelected() {
    return Editor.preferences().getTheme() == Theme.DARK
        ? new Color(COLOR_ACCENT_BLUE.getRed(), COLOR_ACCENT_BLUE.getGreen(), COLOR_ACCENT_BLUE.getBlue(), 48)
        : new Color(COLOR_ACCENT_BLUE.getRed(), COLOR_ACCENT_BLUE.getGreen(), COLOR_ACCENT_BLUE.getBlue(), 30);
  }

  public static Color sceneRowHover() {
    return Editor.preferences().getTheme() == Theme.DARK
        ? new Color(255, 255, 255, 20)
        : new Color(0, 0, 0, 14);
  }

  public static Color selectionOutline() {
    return new Color(COLOR_ACCENT_BLUE.getRed(), COLOR_ACCENT_BLUE.getGreen(), COLOR_ACCENT_BLUE.getBlue(), 150);
  }

  public static Color cardSelected() {
    return Editor.preferences().getTheme() == Theme.DARK
        ? COLOR_CARD_SELECTED
        : new Color(COLOR_ACCENT_BLUE.getRed(), COLOR_ACCENT_BLUE.getGreen(), COLOR_ACCENT_BLUE.getBlue(), 24);
  }

  public static Color cardHover() {
    return Editor.preferences().getTheme() == Theme.DARK
        ? new Color(255, 255, 255, 18)
        : new Color(0, 0, 0, 12);
  }

  public static Color workspaceTop() {
    return uiColor("Editor.workspaceTop", COLOR_WORKSPACE_TOP);
  }

  public static Color workspaceBottom() {
    return uiColor("Editor.workspaceBottom", COLOR_WORKSPACE_BOTTOM);
  }

  public static Color assetExplorerBackground() {
    return Editor.preferences().getTheme() == Theme.DARK
      ? COLOR_ASSET_EXPLORER
      : COLOR_ASSET_EXPLORER_LIGHT;
  }

  public static Color mapBacking() {
    return uiColor("Editor.mapBacking", COLOR_MAP_BACKING);
  }

  public static Color mapBorder() {
    return uiColor("Editor.mapBorder", COLOR_MAP_BORDER);
  }

  private static Color uiColor(String key, Color fallback) {
    Color value = UIManager.getColor(key);
    Color resolved = value != null ? value : fallback;
    return resolved instanceof ColorUIResource ? resolved : new ColorUIResource(resolved);
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
