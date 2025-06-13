package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Tray {
  private static final Logger log = Logger.getLogger(Tray.class.getName());
  private static TrayIcon trayIcon;

  private Tray() {}

  public static void init() {
    // add system tray icon with popup menu
    if (SystemTray.isSupported()) {
      SystemTray tray = SystemTray.getSystemTray();
      PopupMenu menu = new PopupMenu();
      MenuItem exitItem = new MenuItem(Resources.strings().get("menu_exit"));
      exitItem.addActionListener(a -> Game.exit());
      menu.add(exitItem);

      trayIcon =
          new TrayIcon(Resources.images().get("liti-logo-x16.png"), Game.info().toString(), menu);
      trayIcon.setImageAutoSize(true);
      try {
        tray.add(trayIcon);
      } catch (AWTException e) {
        log.log(Level.SEVERE, e.getLocalizedMessage(), e);
      }
    }
  }

  public static void setToolTip(String tooltipText) {
    if (!SystemTray.isSupported() || trayIcon == null) {
      return;
    }

    trayIcon.setToolTip(tooltipText);
  }
}
