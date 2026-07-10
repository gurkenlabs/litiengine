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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

public class ToolBar extends JToolBar {
  private static final int ARC = 8;
  private static final int DOCK_BG_ALPHA = 230;

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
      JToggleButton btn = Style.iconToggleButton(tool.getIcon(), tool.equals(ToolManager.instance().getActiveTool()));
      btn.setToolTipText(tool.getName());
      btn.addActionListener(e -> ToolManager.instance().setActiveTool(tool));
      group.add(btn);
      add(btn);
    }

    ToolManager.instance().addListener(this::updateButtonSelection);

    addSeparator();

    JButton undoBtn = Style.iconButton(Icons.UNDO_24);
    undoBtn.setToolTipText(Resources.strings().get("menu_edit_undo"));
    undoBtn.addActionListener(e -> UndoManager.instance().undo());
    undoBtn.setEnabled(false);
    JButton undoHistoryBtn = Style.iconButton(new DropdownArrowIcon());
    undoHistoryBtn.setToolTipText(Resources.strings().get("menu_edit_undo") + " history");
    undoHistoryBtn.addActionListener(e -> showHistory(undoHistoryBtn, true));
    undoHistoryBtn.setEnabled(false);
    add(splitButton(undoBtn, undoHistoryBtn));

    JButton redoBtn = Style.iconButton(Icons.REDO_24);
    redoBtn.setToolTipText(Resources.strings().get("menu_edit_redo"));
    redoBtn.addActionListener(e -> UndoManager.instance().redo());
    redoBtn.setEnabled(false);
    JButton redoHistoryBtn = Style.iconButton(new DropdownArrowIcon());
    redoHistoryBtn.setToolTipText(Resources.strings().get("menu_edit_redo") + " history");
    redoHistoryBtn.addActionListener(e -> showHistory(redoHistoryBtn, false));
    redoHistoryBtn.setEnabled(false);
    add(splitButton(redoBtn, redoHistoryBtn));

    UndoManager.onUndoStackChanged(mgr -> {
      javax.swing.SwingUtilities.invokeLater(() -> {
          undoBtn.setEnabled(UndoManager.instance().canUndo());
          redoBtn.setEnabled(UndoManager.instance().canRedo());
          undoHistoryBtn.setEnabled(undoBtn.isEnabled());
          redoHistoryBtn.setEnabled(redoBtn.isEnabled());
      });
    });

    addSeparator();

    JButton addBtn = Style.iconButton(new DropdownIcon(Icons.ADD_24));
    addBtn.setPreferredSize(new Dimension(44, 28));
    addBtn.setToolTipText(Resources.strings().get("menu_add"));
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

  private static JPanel splitButton(JButton main, JButton arrow) {
    main.setPreferredSize(new Dimension(28, 28));
    arrow.setPreferredSize(new Dimension(14, 28));
    JPanel split = new JPanel(new BorderLayout(1, 0));
    split.setOpaque(false);
    split.add(main, BorderLayout.WEST);
    split.add(arrow, BorderLayout.CENTER);
    return split;
  }

  private static void showHistory(JButton button, boolean undo) {
    UndoManager manager = UndoManager.instance();
    List<UndoManager.HistoryEntry> history = undo ? manager.getUndoHistory() : manager.getRedoHistory();
    JPopupMenu popup = new JPopupMenu();
    for (int index = 0; index < history.size(); index++) {
      UndoManager.HistoryEntry entry = history.get(index);
      int operations = index + 1;
      JMenuItem item = new JMenuItem((undo ? "Undo " : "Redo ") + entry.description());
      item.addActionListener(e -> {
        for (int operation = 0; operation < operations; operation++) {
          if (undo) {
            manager.undo();
          } else {
            manager.redo();
          }
        }
      });
      popup.add(item);
    }
    if (popup.getComponentCount() == 0) {
      JMenuItem empty = new JMenuItem(undo ? "Nothing to undo" : "Nothing to redo");
      empty.setEnabled(false);
      popup.add(empty);
    }
    popup.show(button, 0, button.getHeight());
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
      g2.setColor(new java.awt.Color(
          Style.COLOR_SURFACE.getRed(),
          Style.COLOR_SURFACE.getGreen(),
          Style.COLOR_SURFACE.getBlue(),
          DOCK_BG_ALPHA));
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

  private static final class DropdownIcon implements Icon {
    private final Icon primary;

    private DropdownIcon(Icon primary) {
      this.primary = primary;
    }

    @Override public int getIconWidth() { return this.primary.getIconWidth() + 10; }

    @Override public int getIconHeight() { return Math.max(this.primary.getIconHeight(), 16); }

    @Override public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
      this.primary.paintIcon(component, graphics, x, y + (getIconHeight() - this.primary.getIconHeight()) / 2);
      Graphics2D g2 = (Graphics2D) graphics.create();
      g2.setColor(component.isEnabled() ? Style.COLOR_TEXT : Style.COLOR_DISABLED_TEXT);
      int arrowX = x + this.primary.getIconWidth() + 3;
      int arrowY = y + getIconHeight() / 2 - 2;
      g2.fillPolygon(new int[] {arrowX, arrowX + 6, arrowX + 3}, new int[] {arrowY, arrowY, arrowY + 5}, 3);
      g2.dispose();
    }
  }

  private static final class DropdownArrowIcon implements Icon {
    @Override public int getIconWidth() { return 8; }

    @Override public int getIconHeight() { return 16; }

    @Override public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
      Graphics2D g2 = (Graphics2D) graphics.create();
      g2.setColor(component.isEnabled() ? Style.COLOR_TEXT : Style.COLOR_DISABLED_TEXT);
      g2.fillPolygon(new int[] {x, x + 8, x + 4}, new int[] {y + 6, y + 6, y + 10}, 3);
      g2.dispose();
    }
  }
}
