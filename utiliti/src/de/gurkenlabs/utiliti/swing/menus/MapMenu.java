package de.gurkenlabs.utiliti.swing.menus;

import java.awt.Color;
import java.awt.Event;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.swing.dialogs.MapPropertyPanel;

@SuppressWarnings("serial")
public final class MapMenu extends JMenu {
  private static final Logger log = Logger.getLogger(MapMenu.class.getName());

  public MapMenu() {
    super(Resources.strings().get("menu_map"));
    this.setMnemonic('M');

    JMenuItem imp = new JMenuItem(Resources.strings().get("menu_import"));
    imp.addActionListener(a -> EditorScreen.instance().getMapComponent().importMap());

    JMenuItem exp = new JMenuItem(Resources.strings().get("menu_export"));
    exp.addActionListener(a -> EditorScreen.instance().getMapComponent().exportMap());

    JMenuItem saveMapSnapshot = new JMenuItem(Resources.strings().get("menu_exportMapSnapshot"));
    saveMapSnapshot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.CTRL_MASK));
    saveMapSnapshot.addActionListener(a -> EditorScreen.instance().saveMapSnapshot());

    JMenuItem reassignIDs = new JMenuItem(Resources.strings().get("menu_reassignMapIds"));
    reassignIDs.addActionListener(a -> {
      try {
        int minID = Integer.parseInt(JOptionPane.showInputDialog(Resources.strings().get("panel_reassignMapIds"), 1));
        EditorScreen.instance().getMapComponent().reassignIds(EditorScreen.instance().getMapSelectionPanel().getCurrentMap(), minID);
      } catch (Exception e) {
        log.log(Level.SEVERE, "No parseable Integer found upon reading the min Map ID input. Try again.");
      }

    });

    JMenuItem del2 = new JMenuItem(Resources.strings().get("menu_removeMap"));
    del2.addActionListener(a -> EditorScreen.instance().getMapComponent().deleteMap());

    JMenuItem mapProps = new JMenuItem(Resources.strings().get("menu_properties"));
    mapProps.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK));
    mapProps.addActionListener(a -> {
      if (EditorScreen.instance().getMapComponent().getMaps() == null || EditorScreen.instance().getMapComponent().getMaps().isEmpty()) {
        return;
      }

      MapPropertyPanel panel = new MapPropertyPanel();
      panel.bind(Game.world().environment().getMap());

      int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), panel, Resources.strings().get("menu_mapProperties"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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
        EditorScreen.instance().getMapComponent().loadMaps(EditorScreen.instance().getGameFile().getMaps());
        EditorScreen.instance().getMapComponent().loadEnvironment((TmxMap) Game.world().environment().getMap());
      }
    });

    this.add(imp);
    this.add(exp);
    this.add(saveMapSnapshot);
    this.add(reassignIDs);
    this.add(del2);
    this.addSeparator();
    this.add(mapProps);
  }
}
