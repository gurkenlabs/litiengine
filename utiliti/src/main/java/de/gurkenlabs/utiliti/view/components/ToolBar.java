package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.tool.Tool;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

public class ToolBar extends JToolBar {
  public ToolBar() {
    super("Tools");
    setFloatable(false);
    setMargin(new Insets(2, 4, 2, 4));
    setFont(Style.getDefaultFont());

    ButtonGroup group = new ButtonGroup();

    for (Tool tool : ToolManager.instance().getTools()) {
      if (!tool.showInToolbar()) {
        continue;
      }
      JToggleButton btn = new JToggleButton(tool.getIcon());
      btn.setToolTipText(tool.getName());
      btn.setSelected(tool.equals(ToolManager.instance().getActiveTool()));
      btn.addActionListener(e -> ToolManager.instance().setActiveTool(tool));
      group.add(btn);
      add(btn);
    }

    ToolManager.instance().addListener(this::updateButtonSelection);

    addSeparator();

    JButton undoBtn = new JButton(Icons.UNDO_24);
    undoBtn.setToolTipText(Resources.strings().get("menu_edit_undo"));
    undoBtn.setMargin(new Insets(2, 4, 2, 4));
    undoBtn.addActionListener(e -> UndoManager.instance().undo());
    undoBtn.setEnabled(false);
    add(undoBtn);

    JButton redoBtn = new JButton(Icons.REDO_24);
    redoBtn.setToolTipText(Resources.strings().get("menu_edit_redo"));
    redoBtn.setMargin(new Insets(2, 4, 2, 4));
    redoBtn.addActionListener(e -> UndoManager.instance().redo());
    redoBtn.setEnabled(false);
    add(redoBtn);

    UndoManager.onUndoStackChanged(mgr -> {
      undoBtn.setEnabled(UndoManager.instance().canUndo());
      redoBtn.setEnabled(UndoManager.instance().canRedo());
    });

    addSeparator();

    JButton addBtn = new JButton(Icons.ADD_24);
    addBtn.setToolTipText(Resources.strings().get("menu_add"));
    addBtn.setMargin(new Insets(2, 4, 2, 4));
    addBtn.addActionListener(e -> {
      JPopupMenu popup = UI.getCanvasPopup();
      popup.show(addBtn, 0, addBtn.getHeight());
    });
    add(addBtn);
  }

  private void updateButtonSelection() {
    Tool active = ToolManager.instance().getActiveTool();
    int toolIdx = 0;
    for (java.awt.Component comp : getComponents()) {
      if (comp instanceof JToggleButton btn) {
        while (toolIdx < ToolManager.instance().getTools().size()
            && !ToolManager.instance().getTools().get(toolIdx).showInToolbar()) {
          toolIdx++;
        }
        if (toolIdx < ToolManager.instance().getTools().size()) {
          btn.setSelected(ToolManager.instance().getTools().get(toolIdx).equals(active));
          toolIdx++;
        }
      }
    }
  }
}
