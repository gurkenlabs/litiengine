package com.litiengine.utiliti.swing.menus;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.litiengine.environment.tilemap.MapObjectType;
import com.litiengine.resources.Resources;
import com.litiengine.utiliti.components.Editor;
import com.litiengine.utiliti.components.MapComponent;
import com.litiengine.utiliti.swing.Icons;
import com.litiengine.utiliti.swing.UI;

@SuppressWarnings("serial")
public final class AddMenu extends JMenu {

  public AddMenu() {
    super(Resources.strings().get("menu_add"));
    this.setIcon(Icons.ADD);

    JMenuItem addProp = new JMenuItem(Resources.strings().get("menu_add_prop"), Icons.PROP);
    addProp.addActionListener(a -> setCreateMode(MapObjectType.PROP));
    addProp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addCreature = new JMenuItem(Resources.strings().get("menu_add_creature"), Icons.CREATURE);
    addCreature.addActionListener(a -> setCreateMode(MapObjectType.CREATURE));
    addCreature.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addCollisionBox = new JMenuItem(Resources.strings().get("menu_add_collisionbox"), Icons.COLLISIONBOX);
    addCollisionBox.addActionListener(a -> setCreateMode(MapObjectType.COLLISIONBOX));
    addCollisionBox.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addTrigger = new JMenuItem(Resources.strings().get("menu_add_trigger"), Icons.TRIGGER);
    addTrigger.addActionListener(a -> setCreateMode(MapObjectType.TRIGGER));
    addTrigger.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addSpawnpoint = new JMenuItem(Resources.strings().get("menu_add_spawnpoint"), Icons.SPAWNPOINT);
    addSpawnpoint.addActionListener(a -> setCreateMode(MapObjectType.SPAWNPOINT));
    addSpawnpoint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addMapArea = new JMenuItem(Resources.strings().get("menu_add_area"), Icons.MAPAREA);
    addMapArea.addActionListener(a -> setCreateMode(MapObjectType.AREA));
    addMapArea.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addLight = new JMenuItem(Resources.strings().get("menu_add_light"), Icons.LIGHT);
    addLight.addActionListener(a -> setCreateMode(MapObjectType.LIGHTSOURCE));
    addLight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addShadow = new JMenuItem(Resources.strings().get("menu_add_shadow"), Icons.SHADOWBOX);
    addShadow.addActionListener(a -> setCreateMode(MapObjectType.STATICSHADOW));
    addShadow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addEmitter = new JMenuItem(Resources.strings().get("menu_add_emitter"), Icons.EMITTER);
    addEmitter.addActionListener(a -> setCreateMode(MapObjectType.EMITTER));
    addEmitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK));
    
    JMenuItem addSoundSource = new JMenuItem(Resources.strings().get("menu_add_soundsource"), Icons.SOUND);
    addSoundSource.addActionListener(a -> setCreateMode(MapObjectType.SOUNDSOURCE));
    addSoundSource.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));

    this.add(addProp);
    this.add(addCreature);
    this.add(addCollisionBox);
    this.add(addTrigger);
    this.add(addSpawnpoint);
    this.add(addMapArea);
    this.add(addLight);
    this.add(addShadow);
    this.add(addEmitter);
    this.add(addSoundSource);
    
    this.setEnabled(false);
    Editor.instance().onLoaded(() -> this.setEnabled(Editor.instance().getCurrentResourceFile() != null));
  }

  private static void setCreateMode(MapObjectType type) {
    Editor.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_CREATE);
    UI.getInspector().setMapObjectType(type);
  }
}
