package de.gurkenlabs.utiliti.swing.menus;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

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

    JMenuItem addCreature = new JMenuItem(Resources.strings().get("menu_add_creature"), Icons.CREATURE);
    addCreature.addActionListener(a -> setCreateMode(MapObjectType.CREATURE));

    JMenuItem addLight = new JMenuItem(Resources.strings().get("menu_add_light"), Icons.LIGHT);
    addLight.addActionListener(a -> setCreateMode(MapObjectType.LIGHTSOURCE));

    JMenuItem addTrigger = new JMenuItem(Resources.strings().get("menu_add_trigger"), Icons.TRIGGER);
    addTrigger.addActionListener(a -> setCreateMode(MapObjectType.TRIGGER));

    JMenuItem addSpawnpoint = new JMenuItem(Resources.strings().get("menu_add_spawnpoint"), Icons.SPAWNPOINT);
    addSpawnpoint.addActionListener(a -> setCreateMode(MapObjectType.SPAWNPOINT));

    JMenuItem addCollisionBox = new JMenuItem(Resources.strings().get("menu_add_collisionbox"), Icons.COLLISIONBOX);
    addCollisionBox.addActionListener(a -> setCreateMode(MapObjectType.COLLISIONBOX));

    JMenuItem addMapArea = new JMenuItem(Resources.strings().get("menu_add_area"), Icons.MAPAREA);
    addMapArea.addActionListener(a -> setCreateMode(MapObjectType.AREA));

    JMenuItem addShadow = new JMenuItem(Resources.strings().get("menu_add_shadow"), Icons.SHADOWBOX);
    addShadow.addActionListener(a -> setCreateMode(MapObjectType.STATICSHADOW));

    JMenuItem addEmitter = new JMenuItem(Resources.strings().get("menu_add_emitter"), Icons.EMITTER);
    addEmitter.addActionListener(a -> setCreateMode(MapObjectType.EMITTER));

    addMenu.add(addProp);
    addMenu.add(addCreature);
    addMenu.add(addLight);
    addMenu.add(addTrigger);
    addMenu.add(addSpawnpoint);
    addMenu.add(addCollisionBox);
    addMenu.add(addMapArea);
    addMenu.add(addShadow);
    addMenu.add(addEmitter);
  }

  private static void setCreateMode(MapObjectType tpye) {
    EditorScreen.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_CREATE);
    EditorScreen.instance().getMapObjectPanel().setMapObjectType(tpye);
  }
}
