package de.gurkenlabs.utiliti.swing.menus;

import java.awt.Event;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.swing.Icons;

@SuppressWarnings("serial")
public final class CanvasPopupMenu extends JPopupMenu {
  public CanvasPopupMenu() {
    JMenuItem delete = new JMenuItem(Resources.strings().get("menu_edit_delete"), Icons.DELETEX16);
    delete.addActionListener(e -> EditorScreen.instance().getMainComponent().delete());
    delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    delete.setEnabled(false);

    JMenuItem copy = new JMenuItem(Resources.strings().get("menu_edit_copy"), Icons.COPYX16);
    copy.addActionListener(e -> EditorScreen.instance().getMainComponent().copy());
    copy.setEnabled(false);
    copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));

    JMenuItem cut = new JMenuItem(Resources.strings().get("menu_edit_cut"), Icons.CUTX16);
    cut.addActionListener(e -> EditorScreen.instance().getMainComponent().cut());
    cut.setEnabled(false);
    cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));

    JMenuItem paste = new JMenuItem(Resources.strings().get("menu_edit_paste"), Icons.PASTEX16);
    paste.addActionListener(e -> EditorScreen.instance().getMainComponent().paste());
    paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
    paste.setEnabled(false);

    JMenuItem blueprint = new JMenuItem(Resources.strings().get("menu_edit_blueprint"), Icons.BLUEPRINT);
    blueprint.addActionListener(e -> EditorScreen.instance().getMainComponent().defineBlueprint());
    blueprint.setEnabled(false);

    JMenu layerMenu = new LayerMenu();
    layerMenu.setEnabled(false);

    JMenu renderMenu = new RenderMenu();
    renderMenu.setEnabled(false);
    
    this.add(new AddMenu());
    this.add(paste);
    this.addSeparator();
    this.add(copy);
    this.add(cut);
    this.add(delete);
    this.addSeparator();
    this.add(layerMenu);
    this.add(renderMenu);
    this.add(blueprint);

    EditorScreen.instance().getMainComponent().onFocusChanged(mo -> {
      copy.setEnabled(mo != null);
      cut.setEnabled(mo != null);
      delete.setEnabled(mo != null);
      blueprint.setEnabled(mo != null);
      paste.setEnabled(EditorScreen.instance().getMainComponent().getCopiedBlueprint() != null);
    });

    EditorScreen.instance().getMainComponent().onEditModeChanged(mode -> paste.setEnabled(EditorScreen.instance().getMainComponent().getCopiedBlueprint() != null));

  }
}
