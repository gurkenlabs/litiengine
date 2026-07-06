package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

public class ViewportToolbar extends JPanel {
  private static final Dimension BUTTON_SIZE = new Dimension(36, 32);
  private final JLabel zoomLabel;
  private final JButton btnCopy;
  private final JButton btnCut;
  private final JButton btnDelete;
  private final JButton btnPaste;

  public ViewportToolbar() {
    super(new BorderLayout());
    setOpaque(true);
    setBackground(Style.COLOR_BG);
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
    left.add(separator());
    left.add(addButton());
    this.btnCopy = button("Copy", Icons.COPY_24, () -> Editor.instance().getMapComponent().copy());
    this.btnCut = button("Cut", Icons.CUT_24, () -> Editor.instance().getMapComponent().cut());
    this.btnDelete = button("Delete", Icons.DELETE_24, () -> Editor.instance().getMapComponent().delete());
    left.add(this.btnCopy);
    left.add(this.btnCut);
    left.add(this.btnDelete);
    this.btnPaste = button("Paste", Icons.PASTE_24, () -> Editor.instance().getMapComponent().paste());
    left.add(this.btnPaste);

    JButton undo = (JButton) left.getComponent(1);
    JButton redo = (JButton) left.getComponent(2);
    undo.setEnabled(false);
    redo.setEnabled(false);
    UndoManager.onUndoStackChanged(mgr -> {
      undo.setEnabled(UndoManager.instance().canUndo());
      redo.setEnabled(UndoManager.instance().canRedo());
    });

    this.btnCopy.setEnabled(false);
    this.btnCut.setEnabled(false);
    this.btnDelete.setEnabled(false);
    this.btnPaste.setEnabled(false);
    Editor.instance().getMapComponent().onSelectionChanged(selection -> {
      boolean hasSelection = selection != null && !selection.isEmpty();
      this.btnCopy.setEnabled(hasSelection);
      this.btnCut.setEnabled(hasSelection);
      this.btnDelete.setEnabled(hasSelection);
    });
    Editor.instance().getMapComponent().onCopyTargetChanged(bp -> {
      this.btnPaste.setEnabled(bp != null);
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
    JButton button = new ToolbarButton(icon);
    button.setToolTipText(text);
    button.setFocusable(false);
    button.setPreferredSize(BUTTON_SIZE);
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setFocusPainted(false);
    styleButton(button);
    button.addActionListener(e -> {
      action.run();
      updateZoomLabel();
    });
    return button;
  }

  private JToggleButton toggle(String text, javax.swing.Icon icon, boolean selected, java.util.function.Consumer<Boolean> consumer) {
    JToggleButton button = new ToolbarToggleButton(icon, selected);
    button.setToolTipText(text);
    button.setFocusable(false);
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
    button.setBackground(button.isSelected() ? Style.COLOR_ACCENT_BLUE : Style.COLOR_SURFACE);
    button.setForeground(button.isSelected() ? Color.WHITE : Style.COLOR_TEXT);
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setContentAreaFilled(false);
    button.setOpaque(false);
  }

  private static void styleButton(JButton button) {
    button.setBackground(Style.COLOR_SURFACE);
    button.setForeground(Style.COLOR_TEXT);
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setContentAreaFilled(false);
    button.setOpaque(false);
  }

  private JPanel createZoomGroup() {
    JPanel group = new JPanel(new BorderLayout(0, 0));
    group.setOpaque(false);

    JButton out = button("−", null, Zoom::out);
    JButton in = button("+", null, Zoom::in);
    out.setText("−");
    in.setText("+");
    out.setFont(out.getFont().deriveFont(18f));
    in.setFont(in.getFont().deriveFont(18f));
    this.zoomLabel.setHorizontalAlignment(JLabel.CENTER);
    this.zoomLabel.setForeground(Style.COLOR_TEXT);
    this.zoomLabel.setBackground(Style.COLOR_SURFACE);
    this.zoomLabel.setOpaque(true);
    this.zoomLabel.setPreferredSize(new Dimension(74, 32));
    this.zoomLabel.setBorder(new RoundedBorder(Style.COLOR_BORDER, 10, 4));

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

  private static void paintToolbarButton(java.awt.Component c, javax.swing.ButtonModel model, Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      boolean enabled = c.isEnabled();
      boolean selected = model.isSelected();
      boolean active = selected || model.isPressed();
      Color fill = !enabled ? Style.COLOR_SURFACE : active ? Style.COLOR_ACCENT_BLUE : model.isRollover() ? Style.COLOR_HOVER : Style.COLOR_SURFACE;
      Color border = !enabled ? Style.COLOR_BORDER.darker() : selected ? Style.COLOR_ACCENT_BLUE : Style.COLOR_BORDER;
      g2.setColor(fill);
      g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 10, 10);
      g2.setColor(border);
      g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 10, 10);
      if (!enabled) {
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 10, 10);
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
    protected void paintComponent(Graphics g) {
      paintToolbarButton(this, getModel(), g);
      super.paintComponent(g);
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
        g2.setColor(c instanceof JToggleButton toggle && toggle.isSelected() ? Color.WHITE : Style.COLOR_TEXT);
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
