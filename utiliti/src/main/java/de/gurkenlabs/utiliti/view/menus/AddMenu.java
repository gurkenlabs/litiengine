package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.ProjectCodeIntegration;
import de.gurkenlabs.utiliti.controller.Transform.TransformMode;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.KeyBindings;
import de.gurkenlabs.utiliti.model.KeyBindings.Command;
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

    JMenuItem addProp = new JMenuItem(Resources.strings().get("menu_add_prop"), Icons.PROP_16);
    addProp.addActionListener(a -> setCreateMode(MapObjectType.PROP));
    KeyBindings.bind(addProp, Command.ADD_PROP);

    JMenuItem addCreature =
      new JMenuItem(Resources.strings().get("menu_add_creature"), Icons.CREATURE_16);
    addCreature.addActionListener(a -> setCreateMode(MapObjectType.CREATURE));
    KeyBindings.bind(addCreature, Command.ADD_CREATURE);

    JMenuItem addCollisionBox =
      new JMenuItem(Resources.strings().get("menu_add_collisionbox"), Icons.COLLISIONBOX_16);
    addCollisionBox.addActionListener(a -> setCreateMode(MapObjectType.COLLISIONBOX));
    KeyBindings.bind(addCollisionBox, Command.ADD_COLLISION);

    JMenuItem addTrigger =
      new JMenuItem(Resources.strings().get("menu_add_trigger"), Icons.TRIGGER_16);
    addTrigger.addActionListener(a -> setCreateMode(MapObjectType.TRIGGER));
    KeyBindings.bind(addTrigger, Command.ADD_TRIGGER);

    JMenuItem addSpawnpoint =
      new JMenuItem(Resources.strings().get("menu_add_spawnpoint"), Icons.SPAWNPOINT_16);
    addSpawnpoint.addActionListener(a -> setCreateMode(MapObjectType.SPAWNPOINT));
    KeyBindings.bind(addSpawnpoint, Command.ADD_SPAWNPOINT);

    JMenuItem addMapArea = new JMenuItem(Resources.strings().get("menu_add_area"), Icons.MAPAREA_16);
    addMapArea.addActionListener(a -> setCreateMode(MapObjectType.AREA));
    KeyBindings.bind(addMapArea, Command.ADD_AREA);

    JMenuItem addLight = new JMenuItem(Resources.strings().get("menu_add_light"), Icons.BULB_16);
    addLight.addActionListener(a -> setCreateMode(MapObjectType.LIGHTSOURCE));
    KeyBindings.bind(addLight, Command.ADD_LIGHT);

    JMenuItem addShadow =
      new JMenuItem(Resources.strings().get("menu_add_shadow"), Icons.SHADOWBOX_16);
    addShadow.addActionListener(a -> setCreateMode(MapObjectType.STATICSHADOW));
    KeyBindings.bind(addShadow, Command.ADD_SHADOW);

    JMenuItem addEmitter =
      new JMenuItem(Resources.strings().get("menu_add_emitter"), Icons.EMITTER_16);
    addEmitter.addActionListener(a -> setCreateMode(MapObjectType.EMITTER));
    KeyBindings.bind(addEmitter, Command.ADD_EMITTER);

    JMenuItem addSoundSource =
      new JMenuItem(Resources.strings().get("menu_add_soundsource"), Icons.SOUND_16);
    addSoundSource.addActionListener(a -> setCreateMode(MapObjectType.SOUNDSOURCE));
    KeyBindings.bind(addSoundSource, Command.ADD_SOUND);

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

    addMenuListener(new javax.swing.event.MenuListener() {
      @Override
      public void menuSelected(javax.swing.event.MenuEvent e) {
        refreshProjectTypes();
      }

      @Override
      public void menuDeselected(javax.swing.event.MenuEvent e) {
      }

      @Override
      public void menuCanceled(javax.swing.event.MenuEvent e) {
      }
    });

    this.setEnabled(false);
    Editor.instance()
        .onLoaded(() -> this.setEnabled(Editor.instance().getCurrentResourceFile() != null));
  }

  public static void setCreateMode(MapObjectType type) {
    Editor.instance().getMapComponent().setCreateMapObjectType(type);
  }

  private void refreshProjectTypes() {
    String projectTypesLabel = Resources.strings().get("menu_add_gameImplementations");
    for (int i = getItemCount() - 1; i >= 0; i--) {
      if (getItem(i) instanceof JMenu menu && projectTypesLabel.equals(menu.getText())) {
        remove(i);
      }
    }
    var definitions = Editor.instance().getProjectCodeIntegration().getDefinitions();
    if (definitions.isEmpty()) {
      return;
    }
    JMenu projectTypes = new JMenu(projectTypesLabel);
    for (ProjectCodeIntegration.Definition definition : definitions) {
      JMenuItem item = new JMenuItem(definition.displayName(), Icons.forMapObjectType(definition.baseType()));
      item.setToolTipText(definition.id());
      item.addActionListener(a -> Editor.instance().getMapComponent().setCreateDefinition(definition));
      projectTypes.add(item);
    }
    add(projectTypes);
  }
}
