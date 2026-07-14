package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.tool.PointerTool;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/** Compact status strip scoped to the map viewport. */
public final class StatusBar extends JPanel {
  private final JLabel stateLabel = new JLabel(new StatusIcon());
  private final JLabel toolLabel = new JLabel();
  private final JLabel positionLabel = new JLabel();
  private final JLabel tileLabel = new JLabel();
  private final JLabel gridLabel = new JLabel();
  private final JLabel snapLabel = new JLabel();
  private final JLabel fpsLabel = new JLabel("0 FPS");
  private final Timer updateTimer;
  private final List<JPanel> separatorLines = new ArrayList<>();

  public StatusBar() {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setOpaque(true);
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()),
        BorderFactory.createEmptyBorder(2, 8, 2, 8)));

    Font font = new Font(
        Style.FONTNAME_CONSOLE,
        Font.PLAIN,
        Math.max(10, Math.round(11 * Editor.preferences().getUiScale())));
    for (JLabel label : new JLabel[] {
        this.stateLabel, this.toolLabel, this.positionLabel, this.tileLabel,
        this.gridLabel, this.snapLabel, this.fpsLabel}) {
      label.setFont(font);
    }

    add(this.stateLabel);
    add(separator());
    add(this.toolLabel);
    add(separator());
    add(this.positionLabel);
    add(separator());
    add(this.tileLabel);
    add(separator());
    add(this.gridLabel);
    add(separator());
    add(this.snapLabel);
    add(Box.createHorizontalGlue());
    add(this.fpsLabel);

    this.updateTimer = new Timer(100, event -> updateValues());
    Game.window().getRenderComponent().onFpsChanged(
        fps -> javax.swing.SwingUtilities.invokeLater(() -> this.fpsLabel.setText(fps + " FPS")));
    refreshTheme();
    updateValues();
  }

  @Override public void addNotify() {
    super.addNotify();
    this.updateTimer.start();
  }

  @Override public void removeNotify() {
    this.updateTimer.stop();
    super.removeNotify();
  }

  void refreshTheme() {
    setBackground(Style.background());
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()),
        BorderFactory.createEmptyBorder(2, 8, 2, 8)));
    this.stateLabel.setForeground(Style.text());
    this.toolLabel.setForeground(Style.text());
    this.positionLabel.setForeground(Style.mutedText());
    this.tileLabel.setForeground(Style.mutedText());
    this.gridLabel.setForeground(Style.mutedText());
    this.snapLabel.setForeground(Style.mutedText());
    this.fpsLabel.setForeground(Style.COLOR_GREEN);
    for (JPanel separatorLine : this.separatorLines) {
      separatorLine.setBackground(Style.border());
    }
  }

  void updateValues() {
    String currentStatus = Editor.instance().getCurrentStatus();
    this.stateLabel.setText(currentStatus == null || currentStatus.isBlank() ? "Ready" : currentStatus);
    this.toolLabel.setText(getToolDisplayName());

    IMap map = Game.world() != null && Game.world().environment() != null
        ? Game.world().environment().getMap()
        : null;
    if (map == null) {
      this.positionLabel.setText("X: --  Y: --");
      this.tileLabel.setText("Tile: --, --");
      this.gridLabel.setText("Grid: --");
    } else {
      this.positionLabel.setText(String.format(
          "X: %d  Y: %d",
          (int) Input.mouse().getMapLocation().getX(),
          (int) Input.mouse().getMapLocation().getY()));
      this.tileLabel.setText(String.format("Tile: %d, %d", Input.mouse().getTile().x, Input.mouse().getTile().y));
      this.gridLabel.setText(map.getTileWidth() == map.getTileHeight()
          ? "Grid: " + map.getTileWidth() + " px"
          : "Grid: " + map.getTileWidth() + "x" + map.getTileHeight());
    }
    this.snapLabel.setText(Editor.preferences().snapToGrid()
        ? "Snap: On (1/" + Editor.preferences().getSnapDivision() + ")"
        : "Snap: Off");
  }

  private static String getToolDisplayName() {
    if (ToolManager.instance().getActiveTool() == null) {
      return "Select Tool";
    }
    if (!(ToolManager.instance().getActiveTool() instanceof PointerTool)) {
      var layer = ToolManager.instance().getActiveTileLayer();
      String layerName = layer != null && layer.getName() != null && !layer.getName().isBlank()
          ? layer.getName()
          : "No tile layer";
      return ToolManager.instance().getActiveTool().getName() + "  |  " + layerName;
    }
    return switch (Editor.instance().getMapComponent().getTransformMode()) {
      case NONE -> "Select Tool";
      case MOVE -> "Move Tool";
      case RESIZE -> "Resize Tool";
      case CREATE -> "Create Tool";
    };
  }

  private JPanel separator() {
    JPanel wrapper = new JPanel();
    wrapper.setOpaque(false);
    wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
    wrapper.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
    JPanel line = new JPanel();
    line.setOpaque(true);
    line.setBackground(Style.border());
    Dimension size = new Dimension(1, 12);
    line.setPreferredSize(size);
    line.setMinimumSize(size);
    line.setMaximumSize(size);
    this.separatorLines.add(line);
    wrapper.add(line);
    return wrapper;
  }

  private static final class StatusIcon implements Icon {
    @Override public int getIconWidth() { return 12; }
    @Override public int getIconHeight() { return 12; }

    @Override public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
      Graphics2D g = (Graphics2D) graphics.create();
      try {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(Style.COLOR_GREEN.getRed(), Style.COLOR_GREEN.getGreen(), Style.COLOR_GREEN.getBlue(), 55));
        g.fillOval(x, y, 10, 10);
        g.setColor(Style.COLOR_GREEN);
        g.fillOval(x + 2, y + 2, 6, 6);
      } finally {
        g.dispose();
      }
    }
  }
}
