package de.gurkenlabs.utiliti;

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
      Object value = UIManager.get(key);

      if (value instanceof javax.swing.plaf.FontUIResource) {
        UIManager.put(key, font);
      }
    }
  }
}
