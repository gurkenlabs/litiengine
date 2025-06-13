package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public final class CanvasPopupMenu extends JPopupMenu {
  public CanvasPopupMenu() {
    JMenuItem delete = new JMenuItem(Resources.strings().get("menu_edit_delete"), Icons.DELETE);
    delete.addActionListener(e -> Editor.instance().getMapComponent().delete());
    delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    delete.setEnabled(false);

    JMenuItem copy = new JMenuItem(Resources.strings().get("menu_edit_copy"), Icons.COPY);
    copy.addActionListener(e -> Editor.instance().getMapComponent().copy());
    copy.setEnabled(false);
    copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));

    JMenuItem cut = new JMenuItem(Resources.strings().get("menu_edit_cut"), Icons.CUT);
    cut.addActionListener(e -> Editor.instance().getMapComponent().cut());
    cut.setEnabled(false);
    cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));

    JMenuItem paste = new JMenuItem(Resources.strings().get("menu_edit_paste"), Icons.PASTE);
    paste.addActionListener(e -> Editor.instance().getMapComponent().paste());
    paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
    paste.setEnabled(false);

    JMenuItem blueprint =
        new JMenuItem(Resources.strings().get("menu_edit_blueprint"), Icons.BLUEPRINT);
    blueprint.addActionListener(e -> Editor.instance().getMapComponent().defineBlueprint());
    blueprint.setEnabled(false);

    JMenuItem emitter = new JMenuItem(Resources.strings().get("menu_save_emitter"), Icons.EMITTER);
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
              emitter.setEnabled(mo != null && mo.getType().equals(MapObjectType.EMITTER.name()));
              paste.setEnabled(Editor.instance().getMapComponent().getCopiedBlueprint() != null);
            });

    Editor.instance()
        .getMapComponent()
        .onEditModeChanged(
            mode -> paste.setEnabled(Editor.instance().getMapComponent().getCopiedBlueprint() != null));
  }
}
