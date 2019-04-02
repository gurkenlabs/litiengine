package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Font;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

import de.gurkenlabs.litiengine.resources.Resources;

public final class Style {
  public static final Color COLOR_DEFAULT_BOUNDING_BOX_FILL = new Color(0, 0, 0, 35);
  public static final Color COLOR_DARKBORDER = new Color(30, 30, 30, 200);
  public static final Color COLOR_DEFAULT_GRID = new Color(255, 255, 255, 65);
  public static final Color COLOR_COLLISION_FILL = new Color(255, 0, 0, 15);
  public static final Color COLOR_COLLISION_BORDER = Color.RED;
  public static final Color COLOR_NOCOLLISION_BORDER = new Color(255, 0, 0, 150);
  public static final Color COLOR_TRIGGER_BORDER = Color.YELLOW;
  public static final Color COLOR_TRIGGER_FILL = new Color(255, 255, 0, 15);
  public static final Color COLOR_SPAWNPOINT = Color.GREEN;
  public static final Color COLOR_UNSUPPORTED = new Color(180, 180, 180, 200);
  public static final Color COLOR_UNSUPPORTED_FILL = new Color(180, 180, 180, 15);
  public static final Color COLOR_NEWOBJECT_FILL = new Color(0, 255, 0, 50);
  public static final Color COLOR_NEWOBJECT_BORDER = Color.GREEN.darker();
  public static final Color COLOR_TRANSFORM_RECT_FILL = new Color(255, 255, 255, 100);
  public static final Color COLOR_SHADOW_FILL = new Color(85, 130, 200, 15);
  public static final Color COLOR_SHADOW_BORDER = new Color(30, 85, 170);
  public static final Color COLOR_MOUSE_SELECTION_AREA_FILL = new Color(0, 130, 152, 80);
  public static final Color COLOR_MOUSE_SELECTION_AREA_BORDER = new Color(0, 130, 152, 150);

  public static final Color COLOR_ASSETPANEL_BACKGROUND = new Color(24, 24, 24);
  public static final Color COLOR_DEFAULT_TAG = new Color(99, 113, 118);
  public static final Color COLOR_DEFAULT_TAG_HOVER = COLOR_DEFAULT_TAG.darker();
  public static final Color COLOR_STATUS = Color.WHITE;

  private static final Logger log = Logger.getLogger(Style.class.getName());

  public static void initSwingComponentStyle() {
    try {
      JPopupMenu.setDefaultLightWeightPopupEnabled(false);
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());
      setDefaultSwingFont(new FontUIResource(Resources.fonts().get("OpenSans.ttf", Font.PLAIN, 11)));
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }
  }

  public static void setDefaultSwingFont(FontUIResource font) {
    Enumeration<Object> keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      ;
      Object value = UIManager.get(key);

      if (value instanceof javax.swing.plaf.FontUIResource) {
        UIManager.put(key, font);
      }
    }
  }

  private Style() {
    throw new UnsupportedOperationException();
  }
}
