package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.Zoom;
import de.gurkenlabs.utiliti.controller.tool.Tool;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.menus.AddMenu;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

public class ViewportToolbar extends JPanel {
  private static final Color BAR_BG = new Color(18, 19, 23);
  private static final Color BUTTON_BG = new Color(30, 32, 38);
  private static final Color BUTTON_HOVER = new Color(38, 42, 52);
  private static final Color TOGGLE_SELECTED = new Color(53, 116, 242);
  private static final Dimension BUTTON_SIZE = new Dimension(36, 32);
  private final JLabel zoomLabel;

  public ViewportToolbar() {
    super(new BorderLayout());
    setOpaque(true);
    setBackground(BAR_BG);
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Style.COLOR_BORDER),
        BorderFactory.createEmptyBorder(6, 8, 6, 8)));

    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
    left.setOpaque(false);

    JPanel right = new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 0));
    right.setOpaque(false);

    left.add(button("Select", Icons.POINTER_24, () -> selectTool(0)));
    left.add(button("Undo", Icons.UNDO_24, () -> UndoManager.instance().undo()));
    left.add(button("Redo", Icons.REDO_24, () -> UndoManager.instance().redo()));
    left.add(addButton());

    JButton undo = (JButton) left.getComponent(1);
    JButton redo = (JButton) left.getComponent(2);
    undo.setEnabled(false);
    redo.setEnabled(false);
    UndoManager.onUndoStackChanged(mgr -> {
      undo.setEnabled(UndoManager.instance().canUndo());
      redo.setEnabled(UndoManager.instance().canRedo());
    });

    left.add(separator());
    left.add(toggle("Grid", new GridIcon(), Editor.preferences().showGrid(), selected -> Editor.preferences().setShowGrid(selected)));
    left.add(toggle("Snap", new SnapIcon(), Editor.preferences().snapToGrid(), selected -> Editor.preferences().setSnapToGrid(selected)));
    left.add(toggle("Collision", Icons.COLLISIONBOX_24, Editor.preferences().renderBoundingBoxes(), selected -> Editor.preferences().setRenderBoundingBoxes(selected)));

    this.zoomLabel = new JLabel(formatZoom());
    right.add(createZoomGroup());

    add(left, BorderLayout.WEST);
    add(right, BorderLayout.EAST);
  }

  private JButton addButton() {
    JButton button = button("Add", Icons.ADD_24, () -> {});
    button.addActionListener(e -> createAddPopup().show(button, 0, button.getHeight()));
    return button;
  }

  private static JPopupMenu createAddPopup() {
    JPopupMenu popup = new JPopupMenu();
    addCreateItem(popup, "Prop", Icons.PROP_16, de.gurkenlabs.litiengine.environment.tilemap.MapObjectType.PROP);
    addCreateItem(popup, "Creature", Icons.CREATURE_16, de.gurkenlabs.litiengine.environment.tilemap.MapObjectType.CREATURE);
    addCreateItem(popup, "Collision", Icons.COLLISIONBOX_16, de.gurkenlabs.litiengine.environment.tilemap.MapObjectType.COLLISIONBOX);
    addCreateItem(popup, "Spawn", Icons.SPAWNPOINT_16, de.gurkenlabs.litiengine.environment.tilemap.MapObjectType.SPAWNPOINT);
    addCreateItem(popup, "Area", Icons.MAPAREA_16, de.gurkenlabs.litiengine.environment.tilemap.MapObjectType.AREA);
    addCreateItem(popup, "Light", Icons.BULB_16, de.gurkenlabs.litiengine.environment.tilemap.MapObjectType.LIGHTSOURCE);
    return popup;
  }

  private static void addCreateItem(JPopupMenu popup, String text, javax.swing.Icon icon, de.gurkenlabs.litiengine.environment.tilemap.MapObjectType type) {
    JMenuItem item = new JMenuItem(text, icon);
    item.addActionListener(e -> AddMenu.setCreateMode(type));
    popup.add(item);
  }

  private JButton button(String text, javax.swing.Icon icon, Runnable action) {
    JButton button = new JButton(icon);
    button.setToolTipText(text);
    button.setFocusable(false);
    button.setPreferredSize(BUTTON_SIZE);
    button.setMargin(new Insets(0, 0, 0, 0));
    styleButton(button);
    button.addActionListener(e -> {
      action.run();
      updateZoomLabel();
    });
    return button;
  }

  private JToggleButton toggle(String text, javax.swing.Icon icon, boolean selected, java.util.function.Consumer<Boolean> consumer) {
    JToggleButton button = new JToggleButton(icon, selected);
    button.setToolTipText(text);
    button.setFocusable(false);
    button.setPreferredSize(BUTTON_SIZE);
    button.setMargin(new Insets(0, 0, 0, 0));
    styleToggle(button);
    button.addActionListener(e -> {
      consumer.accept(button.isSelected());
      styleToggle(button);
    });
    return button;
  }

  private static void styleToggle(JToggleButton button) {
    button.setBackground(button.isSelected() ? TOGGLE_SELECTED : BUTTON_BG);
    button.setForeground(button.isSelected() ? Color.WHITE : Style.COLOR_TEXT);
    button.setBorder(BorderFactory.createLineBorder(button.isSelected() ? Style.COLOR_ACCENT_BLUE : Style.COLOR_BORDER));
    button.setContentAreaFilled(true);
    button.setOpaque(true);
  }

  private static void styleButton(JButton button) {
    button.setBackground(BUTTON_BG);
    button.setForeground(Style.COLOR_TEXT);
    button.setBorder(BorderFactory.createLineBorder(Style.COLOR_BORDER));
    button.setContentAreaFilled(true);
    button.setOpaque(true);
  }

  private JPanel createZoomGroup() {
    JPanel group = new JPanel(new BorderLayout(0, 0));
    group.setOpaque(false);
    group.setBorder(BorderFactory.createLineBorder(Style.COLOR_BORDER));

    JButton out = button("−", null, Zoom::out);
    JButton in = button("+", null, Zoom::in);
    out.setText("−");
    in.setText("+");
    out.setFont(out.getFont().deriveFont(18f));
    in.setFont(in.getFont().deriveFont(18f));
    this.zoomLabel.setHorizontalAlignment(JLabel.CENTER);
    this.zoomLabel.setForeground(Style.COLOR_TEXT);
    this.zoomLabel.setBackground(BUTTON_BG);
    this.zoomLabel.setOpaque(true);
    this.zoomLabel.setPreferredSize(new Dimension(74, 32));
    this.zoomLabel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Style.COLOR_BORDER));

    group.add(out, BorderLayout.WEST);
    group.add(this.zoomLabel, BorderLayout.CENTER);
    group.add(in, BorderLayout.EAST);

    JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
    wrapper.setOpaque(false);
    wrapper.add(group);
    wrapper.add(button("Fit", new FitIcon(), this::fitMap));
    return wrapper;
  }

  private void fitMap() {
    if (Game.world() == null || Game.world().environment() == null || Game.world().camera() == null) {
      return;
    }
    IMap map = Game.world().environment().getMap();
    if (map == null || map.getSizeInPixels().width <= 0 || map.getSizeInPixels().height <= 0) {
      return;
    }
    java.awt.Component renderComponent = Game.window().getRenderComponent();
    double availableW = Math.max(1, renderComponent.getWidth() - 48);
    double availableH = Math.max(1, renderComponent.getHeight() - 48);
    float zoom = (float) Math.min(availableW / map.getSizeInPixels().width, availableH / map.getSizeInPixels().height);
    zoom = Math.max(Zoom.getMin(), Math.min(Zoom.getMax(), zoom * 0.95f));
    Game.world().camera().setZoom(zoom, 0);
    Editor.preferences().setZoom(zoom);
    Editor.instance().getMapComponent().centerCameraOnMap();
  }

  private static JPanel separator() {
    JPanel panel = new JPanel();
    panel.setOpaque(true);
    panel.setBackground(Style.COLOR_BORDER);
    panel.setPreferredSize(new java.awt.Dimension(1, 22));
    return panel;
  }

  private static void selectTool(int index) {
    java.util.List<Tool> tools = ToolManager.instance().getTools();
    if (!tools.isEmpty() && index < tools.size()) {
      ToolManager.instance().setActiveTool(tools.get(index));
    }
  }

  private void updateZoomLabel() {
    this.zoomLabel.setText(formatZoom());
  }

  private static String formatZoom() {
    if (Game.world() != null && Game.world().camera() != null) {
      return Math.round(Game.world().camera().getZoom() * 100) + "%";
    }
    return Zoom.getZoom().toString().trim();
  }

  private abstract static class ToolbarIcon implements Icon {
    @Override public int getIconWidth() { return 20; }
    @Override public int getIconHeight() { return 20; }

    @Override
    public final void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(c instanceof JToggleButton toggle && toggle.isSelected() ? Color.WHITE : Style.COLOR_TEXT);
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
      for (int i = 0; i < 3; i++) {
        int p = x + 4 + i * 6;
        g2.fillOval(p - 1, y + 4 - 1, 2, 2);
        g2.fillOval(p - 1, y + 10 - 1, 2, 2);
        g2.fillOval(p - 1, y + 16 - 1, 2, 2);
      }
      g2.drawRoundRect(x + 5, y + 5, 10, 10, 3, 3);
      g2.drawLine(x + 10, y + 2, x + 10, y + 18);
      g2.drawLine(x + 2, y + 10, x + 18, y + 10);
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
    }
  }
}
