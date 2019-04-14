package de.gurkenlabs.utiliti.swing.menus;

import java.awt.Event;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.swing.Icons;

@SuppressWarnings("serial")
public final class EditMenu extends JMenu {
  public EditMenu() {
    super(Resources.strings().get("menu_edit"));
    this.setMnemonic('E');

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
    cut.addActionListener(a -> EditorScreen.instance().getMapComponent().cut());
    cut.setEnabled(false);

    JMenuItem copy = new JMenuItem(Resources.strings().get("menu_edit_copy"));
    copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
    copy.addActionListener(a -> EditorScreen.instance().getMapComponent().copy());
    copy.setEnabled(false);

    JMenuItem paste = new JMenuItem(Resources.strings().get("menu_edit_paste"));
    paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
    paste.addActionListener(a -> EditorScreen.instance().getMapComponent().paste());
    paste.setEnabled(false);

    JMenuItem delete = new JMenuItem(Resources.strings().get("menu_edit_delete"));
    delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    delete.addActionListener(a -> EditorScreen.instance().getMapComponent().delete());
    delete.setEnabled(false);

    JMenuItem blueprint = new JMenuItem(Resources.strings().get("menu_edit_blueprint"), Icons.BLUEPRINT);
    blueprint.addActionListener(e -> EditorScreen.instance().getMapComponent().defineBlueprint());
    blueprint.setEnabled(false);

    EditorScreen.instance().getMapComponent().onFocusChanged(mo -> {
      copy.setEnabled(mo != null);
      cut.setEnabled(mo != null);
      delete.setEnabled(mo != null);
      blueprint.setEnabled(mo != null);
      paste.setEnabled(EditorScreen.instance().getMapComponent().getCopiedBlueprint() != null);
      undo.setEnabled(UndoManager.instance().canUndo());
      redo.setEnabled(UndoManager.instance().canRedo());
    });

    EditorScreen.instance().getMapComponent().onCopyTargetChanged(target -> paste.setEnabled(target != null));
    EditorScreen.instance().getMapComponent().onEditModeChanged(mode -> paste.setEnabled(EditorScreen.instance().getMapComponent().getCopiedBlueprint() != null));
    
    UndoManager.onUndoStackChanged(manager -> {
      undo.setEnabled(UndoManager.instance().canUndo());
      redo.setEnabled(UndoManager.instance().canRedo());
    });

    EditorScreen.instance().getMapComponent().onEditModeChanged(mode -> paste.setEnabled(EditorScreen.instance().getMapComponent().getCopiedBlueprint() != null));

    this.add(undo);
    this.add(redo);
    this.addSeparator();
    this.add(cut);
    this.add(copy);
    this.add(paste);
    this.add(delete);
    this.addSeparator();
    this.add(blueprint);
  }
}
