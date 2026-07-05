package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.tool.Tool;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.menus.AddMenu;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

public class ToolBar extends JToolBar {
  private static final int ARC = 8;
  private static final Color DOCK_BG = new Color(30, 31, 34, 230);

  public ToolBar() {
    super("Tools");
    setFloatable(false);
    setOpaque(false);
    setMargin(new Insets(4, 8, 4, 8));
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
      JPopupMenu popup = createAddPopup();
      popup.show(addBtn, 0, addBtn.getHeight());
    });
    addBtn.setEnabled(false);
    Editor.instance()
        .onLoaded(() -> addBtn.setEnabled(Editor.instance().getCurrentResourceFile() != null));
    add(addBtn);
  }

  private static JPopupMenu createAddPopup() {
    JPopupMenu popup = new JPopupMenu();
    addCreateItem(popup, Resources.strings().get("menu_add_prop"), Icons.PROP_16, MapObjectType.PROP);
    addCreateItem(popup, Resources.strings().get("menu_add_creature"), Icons.CREATURE_16, MapObjectType.CREATURE);
    addCreateItem(popup, Resources.strings().get("menu_add_collisionbox"), Icons.COLLISIONBOX_16, MapObjectType.COLLISIONBOX);
    addCreateItem(popup, Resources.strings().get("menu_add_trigger"), Icons.TRIGGER_16, MapObjectType.TRIGGER);
    addCreateItem(popup, Resources.strings().get("menu_add_spawnpoint"), Icons.SPAWNPOINT_16, MapObjectType.SPAWNPOINT);
    addCreateItem(popup, Resources.strings().get("menu_add_area"), Icons.MAPAREA_16, MapObjectType.AREA);
    addCreateItem(popup, Resources.strings().get("menu_add_light"), Icons.BULB_16, MapObjectType.LIGHTSOURCE);
    addCreateItem(popup, Resources.strings().get("menu_add_shadow"), Icons.SHADOWBOX_16, MapObjectType.STATICSHADOW);
    addCreateItem(popup, Resources.strings().get("menu_add_emitter"), Icons.EMITTER_16, MapObjectType.EMITTER);
    addCreateItem(popup, Resources.strings().get("menu_add_soundsource"), Icons.SOUND_16, MapObjectType.SOUNDSOURCE);
    return popup;
  }

  private static void addCreateItem(JPopupMenu popup, String text, javax.swing.Icon icon, MapObjectType type) {
    JMenuItem item = new JMenuItem(text, icon);
    item.addActionListener(e -> AddMenu.setCreateMode(type));
    popup.add(item);
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (g instanceof Graphics2D g2) {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(DOCK_BG);
      g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
    }
    super.paintComponent(g);
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
