package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.MapComponent;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.view.components.UI;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public final class AddMenu extends JMenu {

  public AddMenu() {
    super(Resources.strings().get("menu_add"));
    this.setIcon(Icons.ADD_16);

    JMenuItem addProp = new JMenuItem(Resources.strings().get("menu_add_prop"), Icons.ENTITY_16);
    addProp.addActionListener(a -> setCreateMode(MapObjectType.PROP));
    addProp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addCreature =
      new JMenuItem(Resources.strings().get("menu_add_creature"), Icons.CREATURE_16);
    addCreature.addActionListener(a -> setCreateMode(MapObjectType.CREATURE));
    addCreature.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addCollisionBox =
      new JMenuItem(Resources.strings().get("menu_add_collisionbox"), Icons.COLLISIONBOX_16);
    addCollisionBox.addActionListener(a -> setCreateMode(MapObjectType.COLLISIONBOX));
    addCollisionBox.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addTrigger =
      new JMenuItem(Resources.strings().get("menu_add_trigger"), Icons.TRIGGER_16);
    addTrigger.addActionListener(a -> setCreateMode(MapObjectType.TRIGGER));
    addTrigger.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addSpawnpoint =
      new JMenuItem(Resources.strings().get("menu_add_spawnpoint"), Icons.SPAWNPOINT_16);
    addSpawnpoint.addActionListener(a -> setCreateMode(MapObjectType.SPAWNPOINT));
    addSpawnpoint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addMapArea = new JMenuItem(Resources.strings().get("menu_add_area"), Icons.MAPAREA_16);
    addMapArea.addActionListener(a -> setCreateMode(MapObjectType.AREA));
    addMapArea.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addLight = new JMenuItem(Resources.strings().get("menu_add_light"), Icons.BULB_16);
    addLight.addActionListener(a -> setCreateMode(MapObjectType.LIGHTSOURCE));
    addLight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addShadow =
      new JMenuItem(Resources.strings().get("menu_add_shadow"), Icons.SHADOWBOX_16);
    addShadow.addActionListener(a -> setCreateMode(MapObjectType.STATICSHADOW));
    addShadow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addEmitter =
      new JMenuItem(Resources.strings().get("menu_add_emitter"), Icons.EMITTER_16);
    addEmitter.addActionListener(a -> setCreateMode(MapObjectType.EMITTER));
    addEmitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK));

    JMenuItem addSoundSource =
      new JMenuItem(Resources.strings().get("menu_add_soundsource"), Icons.SOUND_16);
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
    Editor.instance()
        .onLoaded(() -> this.setEnabled(Editor.instance().getCurrentResourceFile() != null));
  }

  private static void setCreateMode(MapObjectType type) {
    Editor.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_CREATE);
    UI.getInspector().setMapObjectType(type);
  }
}
