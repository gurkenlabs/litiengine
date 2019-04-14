package de.gurkenlabs.utiliti.swing.menus;

import java.awt.Event;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.components.MapComponent;
import de.gurkenlabs.utiliti.swing.Icons;

public final class AddMenu {
  private static JPopupMenu addPopupMenu;

  private AddMenu() {
  }

  public static JMenu create() {
    JMenu addMenu = new JMenu(Resources.strings().get("menu_add"));
    addMenu.setIcon(Icons.ADD);
    initAddMenu(addMenu);

    return addMenu;
  }

  public static void initPopup() {
    addPopupMenu = new JPopupMenu();
    initAddMenu(addPopupMenu);
  }

  public static JPopupMenu getPopup() {
    return addPopupMenu;
  }

  private static void initAddMenu(JComponent addMenu) {
    JMenuItem addProp = new JMenuItem(Resources.strings().get("menu_add_prop"), Icons.PROP);
    addProp.addActionListener(a -> setCreateMode(MapObjectType.PROP));
    addProp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, Event.CTRL_MASK));

    JMenuItem addCreature = new JMenuItem(Resources.strings().get("menu_add_creature"), Icons.CREATURE);
    addCreature.addActionListener(a -> setCreateMode(MapObjectType.CREATURE));
    addCreature.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, Event.CTRL_MASK));
    
    JMenuItem addCollisionBox = new JMenuItem(Resources.strings().get("menu_add_collisionbox"), Icons.COLLISIONBOX);
    addCollisionBox.addActionListener(a -> setCreateMode(MapObjectType.COLLISIONBOX));
    addCollisionBox.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, Event.CTRL_MASK));

    JMenuItem addTrigger = new JMenuItem(Resources.strings().get("menu_add_trigger"), Icons.TRIGGER);
    addTrigger.addActionListener(a -> setCreateMode(MapObjectType.TRIGGER));
    addTrigger.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, Event.CTRL_MASK));

    JMenuItem addSpawnpoint = new JMenuItem(Resources.strings().get("menu_add_spawnpoint"), Icons.SPAWNPOINT);
    addSpawnpoint.addActionListener(a -> setCreateMode(MapObjectType.SPAWNPOINT));
    addSpawnpoint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, Event.CTRL_MASK));
    
    JMenuItem addMapArea = new JMenuItem(Resources.strings().get("menu_add_area"), Icons.MAPAREA);
    addMapArea.addActionListener(a -> setCreateMode(MapObjectType.AREA));
    addMapArea.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, Event.CTRL_MASK));
    
    JMenuItem addLight = new JMenuItem(Resources.strings().get("menu_add_light"), Icons.LIGHT);
    addLight.addActionListener(a -> setCreateMode(MapObjectType.LIGHTSOURCE));
    addLight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, Event.CTRL_MASK));

    JMenuItem addShadow = new JMenuItem(Resources.strings().get("menu_add_shadow"), Icons.SHADOWBOX);
    addShadow.addActionListener(a -> setCreateMode(MapObjectType.STATICSHADOW));
    addShadow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, Event.CTRL_MASK));

    JMenuItem addEmitter = new JMenuItem(Resources.strings().get("menu_add_emitter"), Icons.EMITTER);
    addEmitter.addActionListener(a -> setCreateMode(MapObjectType.EMITTER));
    addEmitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, Event.CTRL_MASK));

    addMenu.add(addProp);
    addMenu.add(addCreature);
    addMenu.add(addCollisionBox);
    addMenu.add(addTrigger);
    addMenu.add(addSpawnpoint);
    addMenu.add(addMapArea);
    addMenu.add(addLight);
    addMenu.add(addShadow);
    addMenu.add(addEmitter);
  }

  private static void setCreateMode(MapObjectType tpye) {
    EditorScreen.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_CREATE);
    EditorScreen.instance().getMapObjectPanel().setMapObjectType(tpye);
  }
}
