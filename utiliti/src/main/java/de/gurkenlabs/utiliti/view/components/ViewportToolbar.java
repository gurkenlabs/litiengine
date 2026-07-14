package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrainSet;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangColor;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangSet;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.Zoom;
import de.gurkenlabs.utiliti.controller.tool.Tool;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.controller.tool.TerrainBrushTool;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.menus.AddMenu;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

public class ViewportToolbar extends JPanel {
  private static final Dimension BUTTON_SIZE = new Dimension(Style.TOOLBAR_BUTTON_SIZE, Style.TOOLBAR_BUTTON_SIZE);
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
  private final List<JPanel> separators = new ArrayList<>();

  public ViewportToolbar() {
    super(new BorderLayout());
    setOpaque(true);
    setBackground(Style.background());
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()),
        BorderFactory.createEmptyBorder(4, 8, 4, 8)));

    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
    left.setOpaque(false);

    JPanel right = new JPanel(new FlowLayout(FlowLayout.TRAILING, 4, 0));
    right.setOpaque(false);

    ButtonGroup toolButtons = new ButtonGroup();
    for (Tool tool : ToolManager.instance().getTools()) {
      if (tool.showInToolbar()) {
        JToggleButton button = toolButton(tool);
        toolButtons.add(button);
        left.add(tool instanceof TerrainBrushTool ? terrainSplitButton(button, tool) : button);
      }
    }
    left.add(separator());
    this.btnUndo = button("Undo", Icons.UNDO_24, () -> UndoManager.instance().undo(), shortcut(KeyEvent.VK_Z));
    this.btnUndoHistory = button("Undo history", new DropdownArrowIcon(), () -> {});
    this.btnUndoHistory.addActionListener(e -> showHistory(this.btnUndoHistory, true));
    this.btnRedo = button("Redo", Icons.REDO_24, () -> UndoManager.instance().redo(), shortcut(KeyEvent.VK_Y));
    this.btnRedoHistory = button("Redo history", new DropdownArrowIcon(), () -> {});
    this.btnRedoHistory.addActionListener(e -> showHistory(this.btnRedoHistory, false));
    left.add(splitButton(this.btnUndo, this.btnUndoHistory));
    left.add(splitButton(this.btnRedo, this.btnRedoHistory));
    left.add(separator());
    left.add(addButton());
    this.btnCopy = button("Copy", Icons.COPY_24, () -> {
      if (Editor.instance().getMapComponent() != null) {
        Editor.instance().getMapComponent().copy();
      }
    }, shortcut(KeyEvent.VK_C));
    this.btnCut = button("Cut", Icons.CUT_24, () -> {
      if (Editor.instance().getMapComponent() != null) {
        Editor.instance().getMapComponent().cut();
      }
    }, shortcut(KeyEvent.VK_X));
    this.btnDelete = button("Delete", Icons.DELETE_24, () -> {
      if (Editor.instance().getMapComponent() != null) {
        Editor.instance().getMapComponent().delete();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    left.add(this.btnCopy);
    left.add(this.btnCut);
    left.add(this.btnDelete);
    this.btnPaste = button("Paste", Icons.PASTE_24, () -> {
      if (Editor.instance().getMapComponent() != null) {
        Editor.instance().getMapComponent().paste();
      }
    }, shortcut(KeyEvent.VK_V));
    left.add(this.btnPaste);

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

    left.add(separator());
    this.btnGrid = toggle("Grid", new GridIcon(), Editor.preferences().showGrid(), selected -> Editor.preferences().setShowGrid(selected), shortcut(KeyEvent.VK_G));
    this.btnSnap = toggle("Snap", new SnapIcon(), Editor.preferences().snapToGrid(), selected -> Editor.preferences().setSnapToGrid(selected));
    this.btnCollision = toggle("Collision", Icons.COLLISIONBOX_24, Editor.preferences().renderBoundingBoxes(), selected -> Editor.preferences().setRenderBoundingBoxes(selected), shortcut(KeyEvent.VK_H));
    left.add(this.btnGrid);
    left.add(this.btnSnap);
    left.add(this.btnCollision);

    this.zoomLabel = new JLabel(formatZoom());
    right.add(createZoomGroup());

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
        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    this.zoomLabel.setForeground(Style.text());
    this.zoomLabel.setBackground(Style.surface());
    this.zoomLabel.setBorder(new RoundedBorder(Style.border(), Style.CORNER_RADIUS, 4));
    for (JPanel separator : this.separators) {
      separator.setBackground(Style.border());
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

  private JButton addButton() {
    JButton button = button("Add", new DropdownIcon(Icons.ADD_24), () -> {});
    button.setPreferredSize(new Dimension(38, BUTTON_SIZE.height));
    button.addActionListener(e -> createAddPopup().show(button, 0, button.getHeight()));
    return button;
  }

  private JToggleButton toolButton(Tool tool) {
    JToggleButton button = new ToolbarToggleButton(tool.getIcon(), tool.equals(ToolManager.instance().getActiveTool()));
    button.setToolTipText(tool.getName());
    button.getAccessibleContext().setAccessibleName(tool.getName());
    button.setFocusable(true);
    button.setPreferredSize(BUTTON_SIZE);
    button.setMargin(new Insets(0, 0, 0, 0));
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
    JButton arrow = button("Select terrain", new DropdownArrowIcon(), () -> {});
    main.setIcon(selectedTerrainIcon(tool));
    main.setPreferredSize(new Dimension(24, BUTTON_SIZE.height));
    arrow.setPreferredSize(new Dimension(12, BUTTON_SIZE.height));
    arrow.addActionListener(e -> createTerrainPopup(tool).show(arrow, 0, arrow.getHeight()));
    ToolManager.instance().addListener(() -> javax.swing.SwingUtilities.invokeLater(() -> {
      main.setIcon(selectedTerrainIcon(tool));
      main.repaint();
    }));
    JPanel split = new JPanel(new BorderLayout(1, 0));
    split.setOpaque(false);
    split.add(main, BorderLayout.WEST);
    split.add(arrow, BorderLayout.CENTER);
    return split;
  }

  private static JPopupMenu createTerrainPopup(Tool tool) {
    JPopupMenu popup = new JPopupMenu();
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      JMenuItem empty = new JMenuItem("No map loaded");
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
          String tilesetName = tileset.getName() != null ? tileset.getName() : "Tileset";
          JMenuItem item = new JMenuItem(
            tilesetName + " - " + (wangColor.getName() != null ? wangColor.getName() : "Terrain"),
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
      JMenuItem empty = new JMenuItem("No terrains in this map");
      empty.setEnabled(false);
      popup.add(empty);
    }
    return popup;
  }

  private static JPanel splitButton(JButton main, JButton arrow) {
    main.setPreferredSize(new Dimension(24, BUTTON_SIZE.height));
    arrow.setPreferredSize(new Dimension(12, BUTTON_SIZE.height));
    JPanel split = new JPanel(new BorderLayout(1, 0));
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
      return tool.getIcon();
    }
    return Game.world().environment().getMap().getTilesets().stream()
      .filter(tileset -> tileset.getTerrainSets() != null && tileset.getTerrainSets().contains(terrainSet))
      .findFirst()
      .map(tileset -> terrainIcon(tileset, terrain))
      .orElseGet(tool::getIcon);
  }

  private static void showHistory(JButton button, boolean undo) {
    UndoManager manager = UndoManager.instance();
    List<UndoManager.HistoryEntry> history = undo ? manager.getUndoHistory() : manager.getRedoHistory();
    JPopupMenu popup = new JPopupMenu();
    if (history.isEmpty()) {
      JMenuItem empty = new JMenuItem(undo ? "Nothing to undo" : "Nothing to redo");
      empty.setEnabled(false);
      popup.add(empty);
    } else {
      for (int index = 0; index < history.size(); index++) {
        UndoManager.HistoryEntry entry = history.get(index);
        int operations = index + 1;
        String label = (undo ? "Undo " : "Redo ") + entry.description();
        if (operations > 1) {
          label += " (" + operations + " operations)";
        }
        JMenuItem item = new JMenuItem(label);
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
    }
    popup.show(button, 0, button.getHeight());
  }

  private static JPopupMenu createAddPopup() {
    JPopupMenu popup = new JPopupMenu();
    addCreateItem(popup, "Prop", Icons.PROP_16, MapObjectType.PROP, KeyEvent.VK_1);
    addCreateItem(popup, "Creature", Icons.CREATURE_16, MapObjectType.CREATURE, KeyEvent.VK_2);
    addCreateItem(popup, "Collisionbox", Icons.COLLISIONBOX_16, MapObjectType.COLLISIONBOX, KeyEvent.VK_3);
    addCreateItem(popup, "Trigger", Icons.TRIGGER_16, MapObjectType.TRIGGER, KeyEvent.VK_4);
    addCreateItem(popup, "Spawnpoint", Icons.SPAWNPOINT_16, MapObjectType.SPAWNPOINT, KeyEvent.VK_5);
    addCreateItem(popup, "Area", Icons.MAPAREA_16, MapObjectType.AREA, KeyEvent.VK_6);
    addCreateItem(popup, "Light", Icons.BULB_16, MapObjectType.LIGHTSOURCE, KeyEvent.VK_7);
    addCreateItem(popup, "Static Shadow", Icons.SHADOWBOX_16, MapObjectType.STATICSHADOW, KeyEvent.VK_8);
    addCreateItem(popup, "Emitter", Icons.EMITTER_16, MapObjectType.EMITTER, KeyEvent.VK_9);
    addCreateItem(popup, "Sound", Icons.SOUND_16, MapObjectType.SOUNDSOURCE, KeyEvent.VK_0);
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
    button.setToolTipText(tooltip(text, shortcut));
    button.getAccessibleContext().setAccessibleName(text);
    Style.styleButton(button, Style.ButtonVariant.TOOLBAR);
    button.setPreferredSize(BUTTON_SIZE);
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setFocusPainted(false);
    styleButton(button);
    button.addActionListener(e -> action.run());
    return button;
  }

  private JToggleButton toggle(String text, javax.swing.Icon icon, boolean selected, java.util.function.Consumer<Boolean> consumer) {
    return toggle(text, icon, selected, consumer, null);
  }

  private JToggleButton toggle(String text, javax.swing.Icon icon, boolean selected, java.util.function.Consumer<Boolean> consumer, KeyStroke shortcut) {
    JToggleButton button = new ToolbarToggleButton(icon, selected);
    button.setToolTipText(tooltip(text, shortcut));
    button.getAccessibleContext().setAccessibleName(text);
    Style.styleButton(button, Style.ButtonVariant.TOOLBAR);
    button.setPreferredSize(BUTTON_SIZE);
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setFocusPainted(false);
    styleToggle(button);
    button.addActionListener(e -> {
      consumer.accept(button.isSelected());
      styleToggle(button);
    });
    return button;
  }

  private static void styleToggle(JToggleButton button) {
    button.setBackground(button.isSelected() ? Style.accent() : Style.surface());
    button.setForeground(button.isSelected() ? Style.COLOR_STATUS : Style.text());
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
    JPanel group = new JPanel(new BorderLayout(0, 0));
    group.setOpaque(false);

    JButton out = button("Zoom out", null, () -> { Zoom.out(); updateZoomLabel(); }, shortcut(KeyEvent.VK_MINUS));
    JButton in = button("Zoom in", null, () -> { Zoom.in(); updateZoomLabel(); }, shortcut(KeyEvent.VK_PLUS));
    out.setText("−");
    in.setText("+");
    out.setFont(out.getFont().deriveFont(18f));
    in.setFont(in.getFont().deriveFont(18f));
    this.zoomLabel.setHorizontalAlignment(JLabel.CENTER);
    this.zoomLabel.setForeground(Style.text());
    this.zoomLabel.setBackground(Style.surface());
    this.zoomLabel.setOpaque(true);
    this.zoomLabel.getAccessibleContext().setAccessibleName("Zoom level");
    this.zoomLabel.setPreferredSize(new Dimension(58, Style.CONTROL_HEIGHT));
    this.zoomLabel.setBorder(new RoundedBorder(Style.border(), Style.CORNER_RADIUS, 4));

    group.add(out, BorderLayout.WEST);
    group.add(this.zoomLabel, BorderLayout.CENTER);
    group.add(in, BorderLayout.EAST);

    JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
    wrapper.setOpaque(false);
    wrapper.add(group);
    wrapper.add(button("Fit", new FitIcon(), this::fitMap));
    return wrapper;
  }

  public void fitMap() {
    Editor.instance().getMapComponent().fitMap();
  }

  private JPanel separator() {
    JPanel panel = new JPanel();
    panel.setOpaque(true);
    panel.setBackground(Style.border());
    panel.setPreferredSize(new java.awt.Dimension(1, 22));
    this.separators.add(panel);
    return panel;
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
    protected void paintComponent(Graphics g) {
      paintToolbarButton(this, getModel(), g);
      super.paintComponent(g);
    }
  }

  private static final class DropdownIcon implements Icon {
    private final Icon primary;

    private DropdownIcon(Icon primary) {
      this.primary = primary;
    }

    @Override public int getIconWidth() {
      return this.primary.getIconWidth() + 10;
    }

    @Override public int getIconHeight() {
      return Math.max(this.primary.getIconHeight(), 16);
    }

    @Override public void paintIcon(java.awt.Component component, Graphics g, int x, int y) {
      this.primary.paintIcon(component, g, x, y + (getIconHeight() - this.primary.getIconHeight()) / 2);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setColor(component.isEnabled() ? Style.text() : Style.COLOR_DISABLED_TEXT);
      int arrowX = x + this.primary.getIconWidth() + 3;
      int arrowY = y + getIconHeight() / 2 - 2;
      g2.fillPolygon(new int[] {arrowX, arrowX + 6, arrowX + 3}, new int[] {arrowY, arrowY, arrowY + 5}, 3);
      g2.dispose();
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
    @Override public int getIconWidth() { return 20; }
    @Override public int getIconHeight() { return 20; }

    @Override
    public final void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (!c.isEnabled()) {
        g2.setColor(Style.COLOR_DISABLED_TEXT);
      } else {
        g2.setColor(c instanceof JToggleButton toggle && toggle.isSelected() ? Style.COLOR_STATUS : Style.text());
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
          g2.fillRoundRect(x + 2 + col * 6, y + 2 + row * 6, 4, 4, 1, 1);
        }
      }
    }
  }

  private static final class SnapIcon extends ToolbarIcon {
    @Override protected void paint(Graphics2D g2, int x, int y) {
      g2.drawLine(x + 4, y + 4, x + 4, y + 16);
      g2.drawLine(x + 10, y + 4, x + 10, y + 16);
      g2.drawLine(x + 16, y + 4, x + 16, y + 16);
      g2.drawLine(x + 4, y + 4, x + 16, y + 4);
      g2.drawLine(x + 4, y + 10, x + 16, y + 10);
      g2.drawLine(x + 4, y + 16, x + 16, y + 16);
      g2.fillOval(x + 9, y + 9, 3, 3);
      g2.drawLine(x + 2, y + 2, x + 7, y + 7);
      g2.drawLine(x + 7, y + 7, x + 4, y + 7);
      g2.drawLine(x + 7, y + 7, x + 7, y + 4);
      g2.drawLine(x + 18, y + 18, x + 13, y + 13);
      g2.drawLine(x + 13, y + 13, x + 16, y + 13);
      g2.drawLine(x + 13, y + 13, x + 13, y + 16);
    }
  }

  private static final class FitIcon extends ToolbarIcon {
    @Override protected void paint(Graphics2D g2, int x, int y) {
      g2.drawLine(x + 3, y + 8, x + 3, y + 3);
      g2.drawLine(x + 3, y + 3, x + 8, y + 3);
      g2.drawLine(x + 17, y + 8, x + 17, y + 3);
      g2.drawLine(x + 17, y + 3, x + 12, y + 3);
      g2.drawLine(x + 3, y + 12, x + 3, y + 17);
      g2.drawLine(x + 3, y + 17, x + 8, y + 17);
      g2.drawLine(x + 17, y + 12, x + 17, y + 17);
      g2.drawLine(x + 17, y + 17, x + 12, y + 17);
      g2.drawLine(x + 7, y + 13, x + 13, y + 7);
      g2.drawLine(x + 7, y + 13, x + 7, y + 10);
      g2.drawLine(x + 7, y + 13, x + 10, y + 13);
      g2.drawLine(x + 13, y + 7, x + 10, y + 7);
      g2.drawLine(x + 13, y + 7, x + 13, y + 10);
    }
  }
}
