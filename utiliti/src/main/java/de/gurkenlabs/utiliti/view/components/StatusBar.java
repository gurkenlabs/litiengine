package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public final class StatusBar {
  private static JLabel statusLabel;
  private static JLabel toolLabel;

  private StatusBar() {}

  public static Container create() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setOpaque(true);
    panel.setBackground(Style.COLOR_BG);
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, Style.COLOR_BORDER),
        new EmptyBorder(2, 6, 2, 6)));

    int fs = (int) (11 * Editor.preferences().getUiScale());

    statusLabel = new JLabel("");
    statusLabel.setFont(new Font(Style.FONTNAME_CONSOLE, Font.PLAIN, fs));
    statusLabel.setForeground(Style.COLOR_SUBTEXT);

    toolLabel = new JLabel("");
    toolLabel.setFont(new Font(Style.FONTNAME_CONSOLE, Font.PLAIN, fs));
    toolLabel.setForeground(Style.COLOR_ACCENT_BLUE);

    panel.add(statusLabel);
    panel.add(Box.createHorizontalGlue());
    panel.add(createSeparator());
    panel.add(toolLabel);
    return panel;
  }

  public static void update() {
    String position = String.format("%d, %d",
        (int) Input.mouse().getMapLocation().getX(),
        (int) Input.mouse().getMapLocation().getY());
    String tile = String.format("%d, %d", Input.mouse().getTile().x, Input.mouse().getTile().y);
    StringBuilder sb = new StringBuilder();
    sb.append(position);
    sb.append("  \u00B7  ").append(tile);

    int selectionSize = Editor.instance().getMapComponent().getSelectedMapObjects().size();
    if (selectionSize > 0) {
      sb.append("  \u00B7  ").append(Resources.strings().get("status_selected_objects", selectionSize));
    }

    if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
      ILayer currentLayer = getActiveLayer();
      if (currentLayer != null) {
        sb.append("  \u00B7  ").append(currentLayer.getName());
      }
    }

    statusLabel.setText(sb.toString());

    String toolName = getToolDisplayName();
    toolLabel.setText(toolName);

  }

  private static ILayer getActiveLayer() {
    var map = Game.world().environment().getMap();
    if (map.getTileLayers().size() > 0) {
      return map.getTileLayers().get(0);
    }
    if (map.getMapObjectLayers().size() > 0) {
      return map.getMapObjectLayers().get(0);
    }
    return null;
  }

  private static JPanel createSeparator() {
    JPanel sep = new JPanel();
    sep.setOpaque(false);
    sep.setLayout(new BoxLayout(sep, BoxLayout.X_AXIS));
    sep.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
    JPanel line = new JPanel();
    line.setPreferredSize(new Dimension(1, 12));
    line.setMaximumSize(new Dimension(1, 12));
    line.setMinimumSize(new Dimension(1, 12));
    line.setBackground(Style.COLOR_BORDER);
    line.setOpaque(true);
    sep.add(line);
    return sep;
  }

  private static String getToolDisplayName() {
    var mode = Editor.instance().getMapComponent().getTransformMode();
    return switch (mode) {
      case NONE -> "Select";
      case MOVE -> "Move";
      case RESIZE -> "Resize";
      case CREATE -> "Create";
    };
  }
}
