package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.KeyBindings;
import de.gurkenlabs.utiliti.model.KeyBindings.Command;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public final class CanvasPopupMenu extends JPopupMenu {
  public CanvasPopupMenu() {
    JMenuItem delete = new JMenuItem(Resources.strings().get("menu_edit_delete"), Icons.DELETE_16);
    delete.addActionListener(e -> Editor.instance().getMapComponent().delete());
    KeyBindings.bind(delete, Command.DELETE);
    delete.setEnabled(false);

    JMenuItem copy = new JMenuItem(Resources.strings().get("menu_edit_copy"), Icons.COPY_16);
    copy.addActionListener(e -> Editor.instance().getMapComponent().copy());
    copy.setEnabled(false);
    KeyBindings.bind(copy, Command.COPY);

    JMenuItem cut = new JMenuItem(Resources.strings().get("menu_edit_cut"), Icons.CUT_16);
    cut.addActionListener(e -> Editor.instance().getMapComponent().cut());
    cut.setEnabled(false);
    KeyBindings.bind(cut, Command.CUT);

    JMenuItem paste = new JMenuItem(Resources.strings().get("menu_edit_paste"), Icons.PASTE_16);
    paste.addActionListener(e -> Editor.instance().getMapComponent().paste());
    KeyBindings.bind(paste, Command.PASTE);
    paste.setEnabled(false);

    JMenuItem blueprint =
        new JMenuItem(Resources.strings().get("menu_edit_blueprint"), Icons.BLUEPRINT_16);
    blueprint.addActionListener(e -> Editor.instance().getMapComponent().defineBlueprint());
    blueprint.setEnabled(false);

    JMenuItem emitter = new JMenuItem(Resources.strings().get("menu_save_emitter"), Icons.EMITTER_16);
    emitter.addActionListener(e -> Editor.instance().getMapComponent().saveEmitter());
    emitter.setEnabled(false);

    JMenu layerMenu = new LayerMenu();
    layerMenu.setEnabled(false);

    JMenu renderMenu = new RenderMenu();
    renderMenu.setEnabled(false);

    add(new AddMenu());
    add(paste);
    addSeparator();
    add(copy);
    add(cut);
    add(delete);
    addSeparator();
    add(layerMenu);
    add(renderMenu);
    add(blueprint);
    add(emitter);

    Editor.instance()
        .getMapComponent()
        .onFocusChanged(
            mo -> {
              copy.setEnabled(mo != null);
              cut.setEnabled(mo != null);
              delete.setEnabled(mo != null);
              blueprint.setEnabled(mo != null);
               emitter.setEnabled(mo != null && MapObjectType.EMITTER.name().equals(mo.getType()));
              paste.setEnabled(Editor.instance().getMapComponent().getCopiedBlueprint() != null);
            });

    Editor.instance()
        .getMapComponent()
      .onTransformModeChanged(
            mode -> paste.setEnabled(Editor.instance().getMapComponent().getCopiedBlueprint() != null));
  }
}
