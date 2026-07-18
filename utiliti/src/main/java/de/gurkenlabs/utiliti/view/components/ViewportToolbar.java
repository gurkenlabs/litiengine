package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrainSet;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangColor;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangSet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.Zoom;
import de.gurkenlabs.utiliti.controller.tool.BucketFillTool;
import de.gurkenlabs.utiliti.controller.tool.EraserTool;
import de.gurkenlabs.utiliti.controller.tool.PointerTool;
import de.gurkenlabs.utiliti.controller.tool.StampBrushTool;
import de.gurkenlabs.utiliti.controller.tool.Tool;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.controller.tool.TerrainBrushTool;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.menus.AddMenu;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;

public class ViewportToolbar extends JPanel {
  private static final Dimension BUTTON_SIZE = new Dimension(Style.TOOLBAR_BUTTON_SIZE, Style.TOOLBAR_BUTTON_SIZE);
  private static final int BUTTON_HORIZONTAL_PADDING = 10;
  private static final int DROPDOWN_BUTTON_WIDTH = 22;
  private static final int TOOLBAR_VERTICAL_PADDING = 8;
  private static final int ICON_TEXT_GAP = 5;
  private static final Insets BUTTON_MARGIN = new Insets(0, BUTTON_HORIZONTAL_PADDING, 0, BUTTON_HORIZONTAL_PADDING);
  private final JLabel zoomLabel;
  private final JButton btnUndo;
  private final JButton btnRedo;
  private final JButton btnUndoHistory;
  private final JButton btnRedoHistory;
  private final JButton btnCopy;
  private final JButton btnCut;
  private final JButton btnDelete;
  private final JButton btnPaste;
  private final JToggleButton btnGrid;
  private final JToggleButton btnSnap;
  private final JToggleButton btnCollision;
  private final List<JPanel> controlGroups = new ArrayList<>();
  private final List<JPanel> groupDividers = new ArrayList<>();

  public ViewportToolbar(JComboBox<?> mapSelector) {
    super(new BorderLayout());
    setOpaque(true);
    setBackground(Style.background());
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()),
        BorderFactory.createEmptyBorder(
            0, 0, TOOLBAR_VERTICAL_PADDING, 0)));

    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEADING, Style.SPACE_MEDIUM, 0));
    left.setOpaque(false);

    JPanel right = new JPanel();
    right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
    right.setOpaque(false);

    mapSelector.setPreferredSize(new Dimension(232, Style.CONTROL_HEIGHT));
    mapSelector.setMinimumSize(new Dimension(140, Style.CONTROL_HEIGHT));
    mapSelector.setBackground(Style.surface());
    mapSelector.setForeground(Style.text());
    mapSelector.setOpaque(false);
    mapSelector.setBorder(BorderFactory.createEmptyBorder());
    mapSelector.putClientProperty("JComponent.arc", Style.CORNER_RADIUS * 2);
    mapSelector.putClientProperty("JComponent.roundRect", true);
    mapSelector.putClientProperty("JComponent.outline", "none");
    mapSelector.getAccessibleContext().setAccessibleName(Resources.strings().get("toolbar_activeMap"));
    left.add(controlGroup(mapSelector));

    ButtonGroup toolButtons = new ButtonGroup();
    JPanel toolGroup = controlGroup();
    for (Tool tool : ToolManager.instance().getTools()) {
      if (tool.showInToolbar()) {
        JToggleButton button = toolButton(tool);
        toolButtons.add(button);
        addToControlGroup(
            toolGroup, tool instanceof TerrainBrushTool ? terrainSplitButton(button, tool) : button);
      }
    }
    left.add(toolGroup);
    this.btnUndo = button(Resources.strings().get("menu_edit_undo"), Icons.UNDO_16, () -> UndoManager.instance().undo(), shortcut(KeyEvent.VK_Z));
    this.btnUndoHistory = button(Resources.strings().get("toolbar_undoHistory"), new DropdownArrowIcon(), () -> {});
    makeIconOnly(this.btnUndoHistory, DROPDOWN_BUTTON_WIDTH);
    this.btnUndoHistory.addActionListener(e -> showHistory(this.btnUndoHistory, true));
    this.btnRedo = button(Resources.strings().get("menu_edit_redo"), Icons.REDO_16, () -> UndoManager.instance().redo(), shortcut(KeyEvent.VK_Y));
    this.btnRedoHistory = button(Resources.strings().get("toolbar_redoHistory"), new DropdownArrowIcon(), () -> {});
    makeIconOnly(this.btnRedoHistory, DROPDOWN_BUTTON_WIDTH);
    this.btnRedoHistory.addActionListener(e -> showHistory(this.btnRedoHistory, false));
    left.add(controlGroup(splitButton(this.btnUndo, this.btnUndoHistory), splitButton(this.btnRedo, this.btnRedoHistory)));
    left.add(controlGroup(addButton()));
    this.btnCopy = button(Resources.strings().get("menu_edit_copy"), Icons.COPY_16, () -> {
      if (Editor.instance().getMapComponent() != null) {
        Editor.instance().getMapComponent().copy();
      }
    }, shortcut(KeyEvent.VK_C));
    this.btnCut = button(Resources.strings().get("menu_edit_cut"), Icons.CUT_16, () -> {
      if (Editor.instance().getMapComponent() != null) {
        Editor.instance().getMapComponent().cut();
      }
    }, shortcut(KeyEvent.VK_X));
    this.btnDelete = button(Resources.strings().get("menu_edit_delete"), Icons.DELETE_16, () -> {
      if (Editor.instance().getMapComponent() != null) {
        Editor.instance().getMapComponent().delete();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    Style.styleButton(this.btnDelete, Style.ButtonVariant.DESTRUCTIVE);
    sizeLabeledButton(this.btnDelete);
    this.btnPaste = button(Resources.strings().get("menu_edit_paste"), Icons.PASTE_16, () -> {
      if (Editor.instance().getMapComponent() != null) {
        Editor.instance().getMapComponent().paste();
      }
    }, shortcut(KeyEvent.VK_V));
    left.add(controlGroup(this.btnCut, this.btnCopy, this.btnPaste, this.btnDelete));

    this.btnUndo.setEnabled(false);
    this.btnRedo.setEnabled(false);
    this.btnUndoHistory.setEnabled(false);
    this.btnRedoHistory.setEnabled(false);
    UndoManager.onUndoStackChanged(mgr -> {
      javax.swing.SwingUtilities.invokeLater(() -> {
          this.btnUndo.setEnabled(UndoManager.instance().canUndo());
          this.btnRedo.setEnabled(UndoManager.instance().canRedo());
          this.btnUndoHistory.setEnabled(this.btnUndo.isEnabled());
          this.btnRedoHistory.setEnabled(this.btnRedo.isEnabled());
      });
    });

    this.btnCopy.setEnabled(false);
    this.btnCut.setEnabled(false);
    this.btnDelete.setEnabled(false);
    this.btnPaste.setEnabled(false);
    if (Editor.instance().getMapComponent() != null) {
      Editor.instance().getMapComponent().onSelectionChanged(selection -> {
        boolean hasSelection = selection != null && !selection.isEmpty();
        javax.swing.SwingUtilities.invokeLater(() -> {
          this.btnCopy.setEnabled(hasSelection);
          this.btnCut.setEnabled(hasSelection);
          this.btnDelete.setEnabled(hasSelection);
        });
      });
      Editor.instance().getMapComponent().onCopyTargetChanged(bp -> {
        javax.swing.SwingUtilities.invokeLater(() -> this.btnPaste.setEnabled(bp != null));
      });
    }

    this.btnGrid = viewToggle(Resources.strings().get("toolbar_grid"), new GridIcon(), Editor.preferences().showGrid(), selected -> Editor.preferences().setShowGrid(selected), shortcut(KeyEvent.VK_G));
    this.btnSnap = viewToggle(Resources.strings().get("toolbar_snap"), new SnapIcon(), Editor.preferences().snapToGrid(), selected -> Editor.preferences().setSnapToGrid(selected), null);
    this.btnCollision = viewToggle(Resources.strings().get("panel_collision"), new CollisionIcon(), Editor.preferences().renderBoundingBoxes(), selected -> Editor.preferences().setRenderBoundingBoxes(selected), shortcut(KeyEvent.VK_H));
    JPanel viewControls = controlGroup(this.btnGrid, this.btnSnap, this.btnCollision);

    this.zoomLabel = new JLabel(formatZoom());
    right.add(viewControls);
    right.add(Box.createHorizontalStrut(Style.SPACE_MEDIUM));
    right.add(createZoomGroup());
    right.add(Box.createHorizontalStrut(Style.SPACE_MEDIUM));

    add(left, BorderLayout.WEST);
    add(right, BorderLayout.EAST);
  }

  public void syncPreferenceButtons() {
    syncToggle(this.btnGrid, Editor.preferences().showGrid());
    syncToggle(this.btnSnap, Editor.preferences().snapToGrid());
    syncToggle(this.btnCollision, Editor.preferences().renderBoundingBoxes());
  }

  void refreshTheme() {
    setBackground(Style.background());
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()),
        BorderFactory.createEmptyBorder(
            0, 0, TOOLBAR_VERTICAL_PADDING, 0)));
    this.zoomLabel.setForeground(Style.text());
    this.zoomLabel.setBackground(Style.surface());
    this.zoomLabel.setBorder(zoomLabelBorder());
    for (JPanel group : this.controlGroups) {
      group.setBackground(Style.surface());
      group.setBorder(new ToolbarGroupBorder(Style.border()));
    }
    for (JPanel divider : this.groupDividers) {
      divider.setBackground(Style.border());
    }
    repaint();
  }

  private static void syncToggle(JToggleButton button, boolean selected) {
    if (button.isSelected() != selected) {
      button.setSelected(selected);
    }
    styleToggle(button);
    button.repaint();
  }

  private JPanel addButton() {
    JButton main = button(Resources.strings().get("toolbar_add"), Icons.ADD_16, () -> {});
    JButton arrow = button(Resources.strings().get("toolbar_addMenu"), new DropdownArrowIcon(), () -> {});
    makeIconOnly(arrow, DROPDOWN_BUTTON_WIDTH);
    main.addActionListener(e -> createAddPopup().show(main, 0, main.getHeight()));
    arrow.addActionListener(e -> createAddPopup().show(arrow, 0, arrow.getHeight()));
    return splitButton(main, arrow);
  }

  private JToggleButton toolButton(Tool tool) {
    JToggleButton button = new ToolbarToggleButton(toolbarIcon(tool), tool.equals(ToolManager.instance().getActiveTool()));
    button.setText(tool.getName());
    button.setIconTextGap(ICON_TEXT_GAP);
    button.setToolTipText(tool.getName());
    button.getAccessibleContext().setAccessibleName(tool.getName());
    button.setFocusable(true);
    sizeLabeledButton(button);
    button.setMargin(BUTTON_MARGIN);
    button.setFocusPainted(false);
    styleToggle(button);
    button.addActionListener(e -> ToolManager.instance().setActiveTool(tool));
    ToolManager.instance().addListener(() -> javax.swing.SwingUtilities.invokeLater(() -> {
      button.setSelected(tool.equals(ToolManager.instance().getActiveTool()));
      styleToggle(button);
      button.repaint();
    }));
    return button;
  }

  private JPanel terrainSplitButton(JToggleButton main, Tool tool) {
    JButton arrow = button(Resources.strings().get("toolbar_selectTerrain"), new DropdownArrowIcon(), () -> {});
    main.setIcon(selectedTerrainIcon(tool));
    makeIconOnly(arrow, DROPDOWN_BUTTON_WIDTH);
    arrow.addActionListener(e -> createTerrainPopup(tool).show(arrow, 0, arrow.getHeight()));
    ToolManager.instance().addListener(() -> javax.swing.SwingUtilities.invokeLater(() -> {
      main.setIcon(selectedTerrainIcon(tool));
      main.repaint();
    }));
    JPanel split = new JPanel(new BorderLayout(0, 0));
    split.setOpaque(false);
    split.add(main, BorderLayout.WEST);
    split.add(arrow, BorderLayout.CENTER);
    return split;
  }

  private static JPopupMenu createTerrainPopup(Tool tool) {
    JPopupMenu popup = new JPopupMenu();
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      JMenuItem empty = new JMenuItem(Resources.strings().get("toolbar_noMapLoaded"));
      empty.setEnabled(false);
      popup.add(empty);
      return popup;
    }
    boolean found = false;
    for (ITileset tileset : Game.world().environment().getMap().getTilesets()) {
      if (tileset.getTerrainSets() == null || tileset.getTerrainSets().isEmpty()) {
        continue;
      }
      for (ITerrainSet terrainSet : tileset.getTerrainSets()) {
        if (!(terrainSet instanceof WangSet wangSet) || terrainSet.getTerrains() == null) {
          continue;
        }
        for (ITerrain terrain : terrainSet.getTerrains()) {
          if (!(terrain instanceof WangColor wangColor)) {
            continue;
          }
          String tilesetName = tileset.getName() != null ? tileset.getName() : Resources.strings().get("toolbar_unnamedTileset");
          JMenuItem item = new JMenuItem(
            tilesetName + " - " + (wangColor.getName() != null ? wangColor.getName() : Resources.strings().get("tool_terrain")),
            terrainIcon(tileset, wangColor));
          item.addActionListener(e -> {
            ToolManager.instance().setSelectedTerrain(wangSet, wangColor);
            ToolManager.instance().setActiveTool(tool);
          });
          popup.add(item);
          found = true;
        }
      }
    }
    if (!found) {
      JMenuItem empty = new JMenuItem(Resources.strings().get("toolbar_noTerrains"));
      empty.setEnabled(false);
      popup.add(empty);
    }
    return popup;
  }

  private static JPanel splitButton(JButton main, JButton arrow) {
    makeIconOnly(arrow, DROPDOWN_BUTTON_WIDTH);
    JPanel split = new JPanel(new BorderLayout(0, 0));
    split.setOpaque(false);
    split.add(main, BorderLayout.WEST);
    split.add(arrow, BorderLayout.CENTER);
    return split;
  }

  private static Icon terrainIcon(ITileset tileset, WangColor terrain) {
    if (terrain.getTileId() >= 0 && tileset.getTile(terrain.getTileId()) != null) {
      try {
        BufferedImage image = tileset.getTile(terrain.getTileId()).getBasicImage();
        if (image != null) {
          return new ImageIcon(image.getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
        }
      } catch (RuntimeException ignored) {
        // Fall back to the generic terrain icon when the representative image is unavailable.
      }
    }
    return Icons.TERRAIN_16;
  }

  private static Icon selectedTerrainIcon(Tool tool) {
    WangSet terrainSet = ToolManager.instance().getSelectedTerrainSet();
    WangColor terrain = ToolManager.instance().getSelectedTerrain();
    if (terrainSet == null || terrain == null || Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return toolbarIcon(tool);
    }
    return Game.world().environment().getMap().getTilesets().stream()
      .filter(tileset -> tileset.getTerrainSets() != null && tileset.getTerrainSets().contains(terrainSet))
      .findFirst()
      .map(tileset -> terrainIcon(tileset, terrain))
      .orElseGet(() -> toolbarIcon(tool));
  }

  private static void showHistory(JButton button, boolean undo) {
    UndoManager manager = UndoManager.instance();
    List<UndoManager.HistoryEntry> history = undo ? manager.getUndoHistory() : manager.getRedoHistory();
    JPopupMenu popup = new JPopupMenu();
    if (history.isEmpty()) {
      JMenuItem empty = new JMenuItem(Resources.strings().get(
        undo ? "history_nothingToUndo" : "history_nothingToRedo"));
      empty.setEnabled(false);
      popup.add(empty);
    } else {
      for (int index = 0; index < history.size(); index++) {
        UndoManager.HistoryEntry entry = history.get(index);
        int operations = index + 1;
        String label = Resources.strings().get(
          undo ? "history_undoEntry" : "history_redoEntry", entry.description());
        if (operations > 1) {
          label = Resources.strings().get(
            "history_multipleOperations", label, Integer.toString(operations));
        }
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(e -> {
          if (undo) {
            manager.undo(operations);
          } else {
            manager.redo(operations);
          }
        });
        popup.add(item);
      }
    }
    popup.show(button, 0, button.getHeight());
  }

  private static JPopupMenu createAddPopup() {
    JPopupMenu popup = new JPopupMenu();
    addCreateItem(popup, Resources.strings().get("menu_add_prop"), Icons.PROP_16, MapObjectType.PROP, KeyEvent.VK_1);
    addCreateItem(popup, Resources.strings().get("menu_add_creature"), Icons.CREATURE_16, MapObjectType.CREATURE, KeyEvent.VK_2);
    addCreateItem(popup, Resources.strings().get("menu_add_collisionbox"), Icons.COLLISIONBOX_16, MapObjectType.COLLISIONBOX, KeyEvent.VK_3);
    addCreateItem(popup, Resources.strings().get("menu_add_trigger"), Icons.TRIGGER_16, MapObjectType.TRIGGER, KeyEvent.VK_4);
    addCreateItem(popup, Resources.strings().get("menu_add_spawnpoint"), Icons.SPAWNPOINT_16, MapObjectType.SPAWNPOINT, KeyEvent.VK_5);
    addCreateItem(popup, Resources.strings().get("menu_add_area"), Icons.MAPAREA_16, MapObjectType.AREA, KeyEvent.VK_6);
    addCreateItem(popup, Resources.strings().get("menu_add_light"), Icons.BULB_16, MapObjectType.LIGHTSOURCE, KeyEvent.VK_7);
    addCreateItem(popup, Resources.strings().get("menu_add_shadow"), Icons.SHADOWBOX_16, MapObjectType.STATICSHADOW, KeyEvent.VK_8);
    addCreateItem(popup, Resources.strings().get("menu_add_emitter"), Icons.EMITTER_16, MapObjectType.EMITTER, KeyEvent.VK_9);
    addCreateItem(popup, Resources.strings().get("menu_add_soundsource"), Icons.SOUND_16, MapObjectType.SOUNDSOURCE, KeyEvent.VK_0);
    return popup;
  }

  private static void addCreateItem(JPopupMenu popup, String text, javax.swing.Icon icon, MapObjectType type, int keyCode) {
    JMenuItem item = new JMenuItem(text, icon);
    item.setAccelerator(KeyStroke.getKeyStroke(keyCode, InputEvent.CTRL_DOWN_MASK));
    item.addActionListener(e -> AddMenu.setCreateMode(type));
    popup.add(item);
  }

  private JButton button(String text, javax.swing.Icon icon, Runnable action) {
    return button(text, icon, action, null);
  }

  private JButton button(String text, javax.swing.Icon icon, Runnable action, KeyStroke shortcut) {
    JButton button = new ToolbarButton(icon);
    button.setText(text);
    button.setIconTextGap(ICON_TEXT_GAP);
    button.setToolTipText(tooltip(text, shortcut));
    button.getAccessibleContext().setAccessibleName(text);
    Style.styleButton(button, Style.ButtonVariant.TOOLBAR);
    sizeLabeledButton(button);
    button.setMargin(BUTTON_MARGIN);
    button.setFocusPainted(false);
    styleButton(button);
    button.addActionListener(e -> action.run());
    return button;
  }

  private JToggleButton toggle(String text, javax.swing.Icon icon, boolean selected, java.util.function.Consumer<Boolean> consumer) {
    return toggle(text, icon, selected, consumer, null);
  }

  private JToggleButton viewToggle(
      String text,
      Icon icon,
      boolean selected,
      java.util.function.Consumer<Boolean> consumer,
      KeyStroke shortcut) {
    JToggleButton button = toggle(text, icon, selected, consumer, shortcut);
    button.putClientProperty("Editor.subtleToolbarToggle", true);
    styleToggle(button);
    return button;
  }

  private JToggleButton toggle(String text, javax.swing.Icon icon, boolean selected, java.util.function.Consumer<Boolean> consumer, KeyStroke shortcut) {
    JToggleButton button = new ToolbarToggleButton(icon, selected);
    button.setText(text);
    button.setIconTextGap(ICON_TEXT_GAP);
    button.setToolTipText(tooltip(text, shortcut));
    button.getAccessibleContext().setAccessibleName(text);
    Style.styleButton(button, Style.ButtonVariant.TOOLBAR);
    sizeLabeledButton(button);
    button.setMargin(BUTTON_MARGIN);
    button.setFocusPainted(false);
    styleToggle(button);
    button.addActionListener(e -> {
      consumer.accept(button.isSelected());
      styleToggle(button);
    });
    return button;
  }

  private static void styleToggle(JToggleButton button) {
    boolean subtle = Boolean.TRUE.equals(button.getClientProperty("Editor.subtleToolbarToggle"));
    button.setBackground(button.isSelected() ? Style.accent() : Style.surface());
    button.setForeground(
        button.isSelected() ? subtle ? Style.accent() : Style.COLOR_STATUS : Style.text());
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setContentAreaFilled(false);
    button.setOpaque(false);
  }

  private static void styleButton(JButton button) {
    button.setBackground(Style.surface());
    button.setForeground(Style.text());
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setContentAreaFilled(false);
    button.setOpaque(false);
  }

  private JPanel createZoomGroup() {
    JPanel group = controlGroup();
    group.setLayout(new BorderLayout(0, 0));

    JButton out = button(Resources.strings().get("menu_view_zoomOut"), null, () -> { Zoom.out(); updateZoomLabel(); }, shortcut(KeyEvent.VK_MINUS));
    JButton in = button(Resources.strings().get("menu_view_zoomIn"), null, () -> { Zoom.in(); updateZoomLabel(); }, shortcut(KeyEvent.VK_PLUS));
    out.setText("−");
    in.setText("+");
    out.setMargin(new Insets(0, 0, 0, 0));
    in.setMargin(new Insets(0, 0, 0, 0));
    out.setPreferredSize(BUTTON_SIZE);
    in.setPreferredSize(BUTTON_SIZE);
    out.setFont(out.getFont().deriveFont(18f));
    in.setFont(in.getFont().deriveFont(18f));
    this.zoomLabel.setHorizontalAlignment(JLabel.CENTER);
    this.zoomLabel.setForeground(Style.text());
    this.zoomLabel.setBackground(Style.surface());
    this.zoomLabel.setOpaque(true);
    this.zoomLabel.getAccessibleContext().setAccessibleName(Resources.strings().get("toolbar_zoomLevel"));
    this.zoomLabel.setPreferredSize(new Dimension(58, Style.CONTROL_HEIGHT));
    this.zoomLabel.setBorder(zoomLabelBorder());

    markGrouped(out);
    markGrouped(in);
    group.add(out, BorderLayout.WEST);
    group.add(this.zoomLabel, BorderLayout.CENTER);
    group.add(in, BorderLayout.EAST);

    JPanel wrapper = new JPanel();
    wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
    wrapper.setOpaque(false);
    wrapper.add(group);
    wrapper.add(Box.createHorizontalStrut(Style.SPACE_MEDIUM));
    wrapper.add(controlGroup(button(Resources.strings().get("toolbar_fit"), Icons.FIT_16, this::fitMap)));
    return wrapper;
  }

  public void fitMap() {
    Editor.instance().getMapComponent().fitMap();
  }

  private static void sizeLabeledButton(AbstractButton button) {
    int width = BUTTON_HORIZONTAL_PADDING * 2
        + button.getFontMetrics(button.getFont()).stringWidth(button.getText());
    if (button.getIcon() != null) {
      width += button.getIcon().getIconWidth() + button.getIconTextGap();
    }
    Dimension size = new Dimension(Math.max(BUTTON_SIZE.width, width), BUTTON_SIZE.height);
    button.setPreferredSize(size);
    button.setMinimumSize(size);
    button.setMaximumSize(size);
  }

  private static void makeIconOnly(AbstractButton button, int width) {
    button.setText(null);
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setPreferredSize(new Dimension(width, BUTTON_SIZE.height));
  }

  private static javax.swing.border.Border zoomLabelBorder() {
    return BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 1, 0, 1, Style.border()),
        BorderFactory.createEmptyBorder(0, Style.SPACE_SMALL, 0, Style.SPACE_SMALL));
  }

  private JPanel controlGroup(java.awt.Component... components) {
    JPanel group = new ToolbarGroupPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    group.setOpaque(false);
    group.setBackground(Style.surface());
    group.setBorder(new ToolbarGroupBorder(Style.border()));
    for (java.awt.Component component : components) {
      addToControlGroup(group, component);
    }
    this.controlGroups.add(group);
    return group;
  }

  private void addToControlGroup(JPanel group, java.awt.Component component) {
    if (group.getComponentCount() > 0) {
      JPanel divider = new JPanel();
      divider.setOpaque(true);
      divider.setBackground(Style.border());
      divider.setPreferredSize(new Dimension(1, 18));
      this.groupDividers.add(divider);
      group.add(divider);
    }
    markGrouped(component);
    group.add(component);
  }

  private static void markGrouped(java.awt.Component component) {
    if (component instanceof AbstractButton button) {
      button.putClientProperty("Editor.groupedToolbarButton", true);
    } else if (component instanceof java.awt.Container container) {
      for (java.awt.Component child : container.getComponents()) {
        markGrouped(child);
      }
    }
  }

  private static Icon toolbarIcon(Tool tool) {
    if (tool instanceof PointerTool) {
      return Icons.POINTER_16;
    }
    if (tool instanceof StampBrushTool) {
      return Icons.PENCIL_16;
    }
    if (tool instanceof TerrainBrushTool) {
      return Icons.TERRAIN_16;
    }
    if (tool instanceof EraserTool) {
      return Icons.ERASER_16;
    }
    if (tool instanceof BucketFillTool) {
      return Icons.FILL_16;
    }
    return tool.getIcon();
  }

  private static KeyStroke shortcut(int keyCode) {
    return KeyStroke.getKeyStroke(keyCode, InputEvent.CTRL_DOWN_MASK);
  }

  private static String tooltip(String text, KeyStroke shortcut) {
    if (shortcut == null) {
      return text;
    }
    String modifiers = InputEvent.getModifiersExText(shortcut.getModifiers());
    String key = KeyEvent.getKeyText(shortcut.getKeyCode());
    return text + " (" + (modifiers.isEmpty() ? key : modifiers + "+" + key) + ")";
  }

  private void updateZoomLabel() {
    this.zoomLabel.setText(formatZoom());
  }

  public void refreshZoomLabel() {
    updateZoomLabel();
  }

  private static String formatZoom() {
    if (Game.world() != null && Game.world().camera() != null) {
      return Math.round(Game.world().camera().getZoom() * 100) + "%";
    }
    return Zoom.getZoom().toString().trim();
  }

  private static void paintToolbarButton(java.awt.Component c, javax.swing.ButtonModel model, Graphics g) {
    Style.paintButtonBackground(c, model, g);
  }

  private static void paintButtonContents(AbstractButton button, Graphics graphics) {
    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      java.awt.FontMetrics metrics = g2.getFontMetrics(button.getFont());
      Rectangle view = new Rectangle(0, 0, button.getWidth(), button.getHeight());
      Rectangle iconBounds = new Rectangle();
      Rectangle textBounds = new Rectangle();
      Icon icon = button.getIcon();
      String text = button.getText();
      SwingUtilities.layoutCompoundLabel(
          button,
          metrics,
          text,
          icon,
          button.getVerticalAlignment(),
          button.getHorizontalAlignment(),
          button.getVerticalTextPosition(),
          button.getHorizontalTextPosition(),
          view,
          iconBounds,
          textBounds,
          button.getIconTextGap());

      if (icon != null) {
        icon.paintIcon(button, g2, iconBounds.x, iconBounds.y);
      }
      if (text != null && !text.isEmpty()) {
        g2.setFont(button.getFont());
        g2.setColor(button.isEnabled() ? button.getForeground() : Style.COLOR_DISABLED_TEXT);
        g2.drawString(text, textBounds.x, textBounds.y + metrics.getAscent());
      }
    } finally {
      g2.dispose();
    }
  }

  private static final class ToolbarButton extends JButton {
    private ToolbarButton(Icon icon) {
      super(icon);
    }

    @Override
    protected void paintComponent(Graphics g) {
      paintToolbarButton(this, getModel(), g);
      super.paintComponent(g);
    }
  }

  private static final class ToolbarToggleButton extends JToggleButton {
    private ToolbarToggleButton(Icon icon, boolean selected) {
      super(icon, selected);
    }

    @Override
    public Color getForeground() {
      if (isSelected()
          && Boolean.TRUE.equals(getClientProperty("Editor.subtleToolbarToggle"))) {
        return Style.accent();
      }
      return super.getForeground();
    }

    @Override
    protected void paintComponent(Graphics g) {
      paintToolbarButton(this, getModel(), g);
      if (Boolean.TRUE.equals(getClientProperty("Editor.subtleToolbarToggle"))) {
        paintButtonContents(this, g);
      } else {
        super.paintComponent(g);
      }
    }
  }

  private static final class DropdownArrowIcon implements Icon {
    @Override public int getIconWidth() { return 8; }

    @Override public int getIconHeight() { return 16; }

    @Override public void paintIcon(java.awt.Component component, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setColor(component.isEnabled() ? Style.text() : Style.COLOR_DISABLED_TEXT);
      g2.fillPolygon(new int[] {x, x + 8, x + 4}, new int[] {y + 6, y + 6, y + 10}, 3);
      g2.dispose();
    }
  }

  private abstract static class ToolbarIcon implements Icon {
    @Override public int getIconWidth() { return Style.ICON_SIZE; }
    @Override public int getIconHeight() { return Style.ICON_SIZE; }

    @Override
    public final void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (!c.isEnabled()) {
        g2.setColor(Style.COLOR_DISABLED_TEXT);
      } else {
        g2.setColor(c.getForeground());
      }
      paint(g2, x, y);
      g2.dispose();
    }

    protected abstract void paint(Graphics2D g2, int x, int y);
  }

  private static final class GridIcon extends ToolbarIcon {
    @Override protected void paint(Graphics2D g2, int x, int y) {
      for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 3; col++) {
          g2.fillRoundRect(x + 1 + col * 5, y + 1 + row * 5, 3, 3, 1, 1);
        }
      }
    }
  }

  private static final class SnapIcon extends ToolbarIcon {
    @Override protected void paint(Graphics2D g2, int x, int y) {
      g2.drawOval(x + 3, y + 3, 10, 10);
      g2.drawLine(x + 8, y + 1, x + 8, y + 5);
      g2.drawLine(x + 8, y + 11, x + 8, y + 15);
      g2.drawLine(x + 1, y + 8, x + 5, y + 8);
      g2.drawLine(x + 11, y + 8, x + 15, y + 8);
      g2.fillOval(x + 6, y + 6, 5, 5);
    }
  }

  private static final class CollisionIcon extends ToolbarIcon {
    @Override
    protected void paint(Graphics2D g2, int x, int y) {
      g2.drawRect(x + 3, y + 3, 10, 10);
      g2.fillRect(x + 1, y + 1, 3, 3);
      g2.fillRect(x + 12, y + 1, 3, 3);
      g2.fillRect(x + 1, y + 12, 3, 3);
      g2.fillRect(x + 12, y + 12, 3, 3);
    }
  }

  private static final class ToolbarGroupPanel extends JPanel {
    private ToolbarGroupPanel(LayoutManager layout) {
      super(layout);
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      Graphics2D g2 = (Graphics2D) graphics.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Style.surface());
        g2.fillRoundRect(
            0, 0, getWidth(), getHeight(), Style.CORNER_RADIUS * 2, Style.CORNER_RADIUS * 2);
      } finally {
        g2.dispose();
      }
      super.paintComponent(graphics);
    }
  }

  private static final class ToolbarGroupBorder extends AbstractBorder {
    private final Color color;

    private ToolbarGroupBorder(Color color) {
      this.color = color;
    }

    @Override
    public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
      Graphics2D g2 = (Graphics2D) graphics.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(this.color);
        g2.drawRoundRect(
            x, y, width - 1, height - 1, Style.CORNER_RADIUS * 2, Style.CORNER_RADIUS * 2);
      } finally {
        g2.dispose();
      }
    }
  }
}
