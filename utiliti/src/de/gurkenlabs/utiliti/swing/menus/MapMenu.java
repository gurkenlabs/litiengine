package de.gurkenlabs.utiliti.swing.menus;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.swing.UI;
import de.gurkenlabs.utiliti.swing.dialogs.MapPropertyPanel;

@SuppressWarnings("serial")
public final class MapMenu extends JMenu {
  private static final Logger log = Logger.getLogger(MapMenu.class.getName());

  public MapMenu() {
    super(Resources.strings().get("menu_map"));
    this.setMnemonic('M');

    JMenuItem imp = new JMenuItem(Resources.strings().get("menu_map_import"));
    imp.addActionListener(a -> Editor.instance().getMapComponent().importMap());

    JMenuItem exp = new JMenuItem(Resources.strings().get("menu_map_export"));
    exp.addActionListener(a -> Editor.instance().getMapComponent().exportMap());

    JMenuItem saveMapSnapshot = new JMenuItem(Resources.strings().get("menu_map_snapshot"));
    saveMapSnapshot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, InputEvent.SHIFT_DOWN_MASK));
    saveMapSnapshot.addActionListener(a -> Editor.instance().getMapComponent().saveMapSnapshot());

    JMenuItem reassignIDs = new JMenuItem(Resources.strings().get("menu_map_reassignMapIds"));
    reassignIDs.addActionListener(a -> {
      try {
        String min = JOptionPane.showInputDialog(Resources.strings().get("panel_reassignMapIds"), 1);
        if (min == null || min.isEmpty()) {
          return;
        }

        int minID = Integer.parseInt(min);
        Editor.instance().getMapComponent().reassignIds(UI.getMapController().getCurrentMap(), minID);
      } catch (Exception e) {
        log.log(Level.SEVERE, "No parseable Integer found upon reading the min Map ID input. Try again.");
      }

    });

    JMenuItem del2 = new JMenuItem(Resources.strings().get("menu_map_delete"));
    del2.addActionListener(a -> Editor.instance().getMapComponent().deleteMap());

    JMenuItem mapProps = new JMenuItem(Resources.strings().get("menu_map_properties"));
    mapProps.addActionListener(a -> {
      if (Editor.instance().getMapComponent().getMaps() == null || Editor.instance().getMapComponent().getMaps().isEmpty()) {
        return;
      }

      MapPropertyPanel panel = new MapPropertyPanel();
      panel.bind(Game.world().environment().getMap());

      int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), panel, Resources.strings().get("menu_map_properties"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (option == JOptionPane.OK_OPTION) {
        panel.saveChanges();

        final String colorProp = Game.world().environment().getMap().getStringValue(MapProperty.AMBIENTCOLOR);
        try {
          if (colorProp != null && !colorProp.isEmpty()) {
            Color ambientColor = ColorHelper.decode(colorProp);
            Game.world().environment().getAmbientLight().setColor(ambientColor);
          }
        } catch (final NumberFormatException nfe) {
          log.log(Level.SEVERE, nfe.getLocalizedMessage(), nfe);
        }
        UndoManager.instance().recordChanges();
      }
    });

    JCheckBoxMenuItem sync = new JCheckBoxMenuItem(Resources.strings().get("menu_map_syncMaps"));
    sync.setState(Editor.preferences().syncMaps());
    sync.addItemListener(e -> Editor.preferences().setSyncMaps(sync.getState()));

    this.add(imp);
    this.add(exp);
    this.add(del2);
    this.addSeparator();
    this.add(saveMapSnapshot);
    this.add(sync);
    this.add(reassignIDs);
    this.addSeparator();
    this.add(mapProps);

    this.setEnabled(false);
    Editor.instance().onLoaded(() -> this.setEnabled(Editor.instance().getProjectPath() != null));
  }
}
