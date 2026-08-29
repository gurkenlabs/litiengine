package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.KeyBindings;
import de.gurkenlabs.utiliti.model.KeyBindings.Command;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public final class EditMenu extends JMenu {
  public EditMenu() {
    super(Resources.strings().get("menu_edit"));
    this.setMnemonic(this.getText().charAt(0));

    JMenu addMenu = new AddMenu();

    JMenuItem undo = new JMenuItem(Resources.strings().get("menu_edit_undo"), Icons.UNDO_16);
    KeyBindings.bind(undo, Command.UNDO);
    undo.addActionListener(a -> UndoManager.instance().undo());
    undo.setEnabled(false);

    JMenuItem redo = new JMenuItem(Resources.strings().get("menu_edit_redo"), Icons.REDO_16);
    KeyBindings.bind(redo, Command.REDO);
    redo.addActionListener(a -> UndoManager.instance().redo());
    redo.setEnabled(false);

    JMenuItem cut = new JMenuItem(Resources.strings().get("menu_edit_cut"), Icons.CUT_16);
    KeyBindings.bind(cut, Command.CUT);
    cut.addActionListener(a -> Editor.instance().getMapComponent().cut());
    cut.setEnabled(false);

    JMenuItem copy = new JMenuItem(Resources.strings().get("menu_edit_copy"), Icons.COPY_16);
    KeyBindings.bind(copy, Command.COPY);
    copy.addActionListener(a -> Editor.instance().getMapComponent().copy());
    copy.setEnabled(false);

    JMenuItem paste = new JMenuItem(Resources.strings().get("menu_edit_paste"), Icons.PASTE_16);
    KeyBindings.bind(paste, Command.PASTE);
    paste.addActionListener(a -> Editor.instance().getMapComponent().paste());
    paste.setEnabled(false);

    JMenuItem delete = new JMenuItem(Resources.strings().get("menu_edit_delete"), Icons.DELETE_16);
    KeyBindings.bind(delete, Command.DELETE);
    delete.addActionListener(a -> Editor.instance().getMapComponent().delete());
    delete.setEnabled(false);

    JMenuItem selectAll = new JMenuItem(Resources.strings().get("menu_edit_selectAll"), Icons.POINTER_16);
    KeyBindings.bind(selectAll, Command.SELECT_ALL);
    selectAll.addActionListener(a -> Editor.instance().getMapComponent().selectAll());

    JMenuItem deselect = new JMenuItem(Resources.strings().get("menu_edit_deselect"), Icons.CROSS_16);
    KeyBindings.bind(deselect, Command.DESELECT);
    deselect.addActionListener(a -> Editor.instance().getMapComponent().deselect());

    JMenuItem quickSearch = new JMenuItem(Resources.strings().get("menu_edit_quickSearch"), Icons.SEARCH_16);
    KeyBindings.bind(quickSearch, Command.QUICK_SEARCH);
    quickSearch.addActionListener(a -> de.gurkenlabs.utiliti.view.dialogs.QuickSearchDialog.showPalette());

    JMenu layerMenu = new LayerMenu();
    layerMenu.setIcon(Icons.LAYER_16);
    layerMenu.setEnabled(false);

    JMenu renderMenu = new RenderMenu();
    renderMenu.setIcon(Icons.SHOW_16);
    renderMenu.setEnabled(false);

    JMenuItem blueprint =
      new JMenuItem(Resources.strings().get("menu_edit_blueprint"), Icons.BLUEPRINT_16);
    blueprint.addActionListener(e -> Editor.instance().getMapComponent().defineBlueprint());
    blueprint.setEnabled(false);

    Editor.instance()
        .getMapComponent()
        .onFocusChanged(
            mo -> {
              copy.setEnabled(mo != null);
              cut.setEnabled(mo != null);
              delete.setEnabled(mo != null);
              blueprint.setEnabled(mo != null);
              paste.setEnabled(Editor.instance().getMapComponent().getCopiedBlueprint() != null);
              undo.setEnabled(UndoManager.instance().canUndo());
              redo.setEnabled(UndoManager.instance().canRedo());
            });

    Editor.instance()
        .getMapComponent()
        .onCopyTargetChanged(target -> paste.setEnabled(target != null));
    Editor.instance()
        .getMapComponent()
      .onTransformModeChanged(
            mode -> paste.setEnabled(Editor.instance().getMapComponent().getCopiedBlueprint() != null));

    UndoManager.onUndoStackChanged(
        manager -> {
          if (!manager.isCurrentMap()) {
            return;
          }
          undo.setEnabled(manager.canUndo());
          redo.setEnabled(manager.canRedo());
        });

    Editor.instance()
        .getMapComponent()
      .onTransformModeChanged(
            mode -> paste.setEnabled(Editor.instance().getMapComponent().getCopiedBlueprint() != null));

    this.add(addMenu);
    this.addSeparator();
    this.add(quickSearch);
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
