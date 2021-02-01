package com.litiengine.utiliti.swing.menus;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import com.litiengine.resources.Resources;
import com.litiengine.utiliti.components.Editor;
import com.litiengine.utiliti.swing.Icons;

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

    JMenuItem blueprint = new JMenuItem(Resources.strings().get("menu_edit_blueprint"), Icons.BLUEPRINT);
    blueprint.addActionListener(e -> Editor.instance().getMapComponent().defineBlueprint());
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

    Editor.instance().getMapComponent().onFocusChanged(mo -> {
      copy.setEnabled(mo != null);
      cut.setEnabled(mo != null);
      delete.setEnabled(mo != null);
      blueprint.setEnabled(mo != null);
      paste.setEnabled(Editor.instance().getMapComponent().getCopiedBlueprint() != null);
    });

    Editor.instance().getMapComponent().onEditModeChanged(mode -> paste.setEnabled(Editor.instance().getMapComponent().getCopiedBlueprint() != null));

  }
}
