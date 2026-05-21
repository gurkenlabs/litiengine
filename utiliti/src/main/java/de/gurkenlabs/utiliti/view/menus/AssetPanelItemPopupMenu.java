package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.view.components.AssetPanelItem;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public final class AssetPanelItemPopupMenu extends JPopupMenu {

  public AssetPanelItemPopupMenu(AssetPanelItem item) {
    Object origin = item.getOrigin();
    String typeKey = getTypeKey(origin);

    JMenuItem add = new JMenuItem(Resources.strings().get("contextmenu_resource_add"), Icons.ADD_16);
    add.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    add.addActionListener(e -> item.addEntity());
    add.setEnabled(item.canAdd());

    JMenuItem edit = new JMenuItem(Resources.strings().get("contextmenu_resource_edit_" + typeKey), Icons.PENCIL_16);
    edit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
    edit.addActionListener(e -> item.editAsset());
    edit.setEnabled(origin instanceof SpritesheetResource);

    JMenuItem export = new JMenuItem(Resources.strings().get("contextmenu_resource_export_" + typeKey), Icons.EXPORT_16);
    export.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
    export.addActionListener(e -> item.exportAsset());

    JMenuItem delete = new JMenuItem(Resources.strings().get("contextmenu_resource_delete_" + typeKey), Icons.DELETE_16);
    delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    delete.addActionListener(e -> item.deleteAsset());
    delete.setEnabled(!(origin instanceof Tileset));

    add(add);
    add(edit);
    add(export);
    addSeparator();
    add(delete);
  }

  private static String getTypeKey(Object origin) {
    if (origin instanceof SpritesheetResource) {
      return "spritesheet";
    }
    if (origin instanceof Tileset) {
      return "tileset";
    }
    if (origin instanceof EmitterData) {
      return "emitter";
    }
    if (origin instanceof Blueprint || origin instanceof MapObject) {
      return "blueprint";
    }
    if (origin instanceof SoundResource) {
      return "sound";
    }
    return "asset";
  }
}