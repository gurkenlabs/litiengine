package de.gurkenlabs.utiliti.swing.menus;

import java.awt.Event;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.swing.Icons;

@SuppressWarnings("serial")
public final class EditMenu extends JMenu {
  public EditMenu() {
    super(Resources.strings().get("menu_edit"));
    this.setMnemonic('E');

    JMenu addMenu = new AddMenu();

    JMenuItem undo = new JMenuItem(Resources.strings().get("menu_edit_undo"));
    undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK));
    undo.addActionListener(a -> UndoManager.instance().undo());
    undo.setEnabled(false);

    JMenuItem redo = new JMenuItem(Resources.strings().get("menu_edit_redo"));
    redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK));
    redo.addActionListener(a -> UndoManager.instance().redo());
    redo.setEnabled(false);

    JMenuItem cut = new JMenuItem(Resources.strings().get("menu_edit_cut"));
    cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
    cut.addActionListener(a -> Editor.instance().getMapComponent().cut());
    cut.setEnabled(false);

    JMenuItem copy = new JMenuItem(Resources.strings().get("menu_edit_copy"));
    copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
    copy.addActionListener(a -> Editor.instance().getMapComponent().copy());
    copy.setEnabled(false);

    JMenuItem paste = new JMenuItem(Resources.strings().get("menu_edit_paste"));
    paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
    paste.addActionListener(a -> Editor.instance().getMapComponent().paste());
    paste.setEnabled(false);

    JMenuItem delete = new JMenuItem(Resources.strings().get("menu_edit_delete"));
    delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    delete.addActionListener(a -> Editor.instance().getMapComponent().delete());
    delete.setEnabled(false);

    JMenuItem selectAll = new JMenuItem(Resources.strings().get("menu_edit_selectAll"));
    selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK));
    selectAll.addActionListener(a -> Editor.instance().getMapComponent().selectAll());

    JMenuItem deselect = new JMenuItem(Resources.strings().get("menu_edit_deselect"));
    deselect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK));
    deselect.addActionListener(a -> Editor.instance().getMapComponent().deselect());

    JMenu layerMenu = new LayerMenu();
    layerMenu.setEnabled(false);
    
    JMenu renderMenu = new RenderMenu();
    renderMenu.setEnabled(false);

    JMenuItem blueprint = new JMenuItem(Resources.strings().get("menu_edit_blueprint"), Icons.BLUEPRINT);
    blueprint.addActionListener(e -> Editor.instance().getMapComponent().defineBlueprint());
    blueprint.setEnabled(false);

    Editor.instance().getMapComponent().onFocusChanged(mo -> {
      copy.setEnabled(mo != null);
      cut.setEnabled(mo != null);
      delete.setEnabled(mo != null);
      blueprint.setEnabled(mo != null);
      paste.setEnabled(Editor.instance().getMapComponent().getCopiedBlueprint() != null);
      undo.setEnabled(UndoManager.instance().canUndo());
      redo.setEnabled(UndoManager.instance().canRedo());
    });

    Editor.instance().getMapComponent().onCopyTargetChanged(target -> paste.setEnabled(target != null));
    Editor.instance().getMapComponent().onEditModeChanged(mode -> paste.setEnabled(Editor.instance().getMapComponent().getCopiedBlueprint() != null));

    UndoManager.onUndoStackChanged(manager -> {
      undo.setEnabled(UndoManager.instance().canUndo());
      redo.setEnabled(UndoManager.instance().canRedo());
    });

    Editor.instance().getMapComponent().onEditModeChanged(mode -> paste.setEnabled(Editor.instance().getMapComponent().getCopiedBlueprint() != null));

    this.add(addMenu);
    this.addSeparator();
    this.add(undo);
    this.add(redo);
    this.addSeparator();
    this.add(cut);
    this.add(copy);
    this.add(paste);
    this.add(delete);
    this.addSeparator();
    this.add(selectAll);
    this.add(deselect);
    this.addSeparator();
    this.add(layerMenu);
    this.add(renderMenu);
    this.add(blueprint);
  }
}
