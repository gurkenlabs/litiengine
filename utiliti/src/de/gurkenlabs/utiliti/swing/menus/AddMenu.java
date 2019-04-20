package de.gurkenlabs.utiliti.swing.menus;

import java.awt.Event;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.components.MainComponent;
import de.gurkenlabs.utiliti.swing.Icons;
import de.gurkenlabs.utiliti.swing.UI;

@SuppressWarnings("serial")
public final class AddMenu extends JMenu {

  public AddMenu() {
    super(Resources.strings().get("menu_add"));
    this.setIcon(Icons.ADD);

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

    this.add(addProp);
    this.add(addCreature);
    this.add(addCollisionBox);
    this.add(addTrigger);
    this.add(addSpawnpoint);
    this.add(addMapArea);
    this.add(addLight);
    this.add(addShadow);
    this.add(addEmitter);
    
    this.setEnabled(false);
    EditorScreen.instance().onLoaded(() -> this.setEnabled(EditorScreen.instance().getCurrentResourceFile() != null));
  }

  private static void setCreateMode(MapObjectType tpye) {
    EditorScreen.instance().getMainComponent().setEditMode(MainComponent.EDITMODE_CREATE);
    UI.getMapObjectPanel().setMapObjectType(tpye);
  }
}
