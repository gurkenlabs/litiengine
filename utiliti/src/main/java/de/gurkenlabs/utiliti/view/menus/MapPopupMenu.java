package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.view.components.UI;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public class MapPopupMenu extends JPopupMenu {
  private static final Logger log = Logger.getLogger(MapPopupMenu.class.getName());

  public MapPopupMenu() {
    JMenuItem exp = new JMenuItem(Resources.strings().get("menu_map_export"));
    exp.addActionListener(a -> Editor.instance().getMapComponent().exportMap());

    JMenuItem saveMapSnapshot = new JMenuItem(Resources.strings().get("menu_map_snapshot"));
    saveMapSnapshot.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, InputEvent.SHIFT_DOWN_MASK));
    saveMapSnapshot.addActionListener(a -> Editor.instance().getMapComponent().saveMapSnapshot());

    JMenuItem reassignIDs = new JMenuItem(Resources.strings().get("menu_map_reassignMapIds"));
    reassignIDs.addActionListener(
        a -> {
          try {
            String min =
                JOptionPane.showInputDialog(Resources.strings().get("panel_reassignMapIds"), 1);
            if (min == null || min.isEmpty()) {
              return;
            }

            int minID = Integer.parseInt(min);
            Editor.instance()
                .getMapComponent()
                .reassignIds(UI.getMapController().getCurrentMap(), minID);
          } catch (Exception e) {
            log.log(
                Level.SEVERE,
                "No parseable Integer found upon reading the min Map ID input. Try again.");
          }
        });

    JMenuItem del2 = new JMenuItem(Resources.strings().get("menu_map_delete"));
    del2.addActionListener(a -> Editor.instance().getMapComponent().deleteMap());

    JMenuItem mapProps = new JMenuItem(Resources.strings().get("menu_map_properties"));
    mapProps.addActionListener(a -> MapMenu.handleMapPropertiesChanges());
    this.add(exp);
    this.add(del2);
    this.addSeparator();
    this.add(saveMapSnapshot);
    this.add(reassignIDs);
    this.addSeparator();
    this.add(mapProps);
  }
}
