package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.Zoom;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.Objects;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class StatusBar {
  private static JLabel statusLabel;
  private static JLabel toolLabel;
  private static JComboBox<Zoom> zoomComboBox;

  private static boolean settingZoom;

  private StatusBar() {}

  public static Container create() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    statusLabel = new JLabel("");
    statusLabel.setFont(
        new Font(
            Style.FONTNAME_CONSOLE, Font.PLAIN, (int) (12 * Editor.preferences().getUiScale())));

    toolLabel = new JLabel("");
    toolLabel.setFont(
        new Font(
            Style.FONTNAME_CONSOLE, Font.PLAIN, (int) (12 * Editor.preferences().getUiScale())));
    toolLabel.setForeground(Style.COLOR_ACCENT_BLUE);

    zoomComboBox = new JComboBox<>(Zoom.getAll());
    zoomComboBox.addItemListener(
        e -> {
          if (settingZoom || e.getStateChange() != ItemEvent.SELECTED) {
            return;
          }

          Zoom.set(((Zoom) Objects.requireNonNull(zoomComboBox.getSelectedItem())).getValue());
        });

    panel.add(zoomComboBox);
    panel.add(statusLabel);
    panel.add(Box.createHorizontalGlue());
    panel.add(toolLabel);
    return panel;
  }

  public static void update() {
    String position =
        String.format(
            "x/y: %d,%d",
            (int) Input.mouse().getMapLocation().getX(),
            (int) Input.mouse().getMapLocation().getY());
    String tile =
        String.format("Tile: %d,%d", Input.mouse().getTile().x, Input.mouse().getTile().y);
    StringBuilder status = new StringBuilder();
    status.append(String.format("%-14s %-10s", position, tile));

    int selectionSize = Editor.instance().getMapComponent().getSelectedMapObjects().size();
    if (selectionSize > 0) {
      status.append("  ").append(Resources.strings().get("status_selected_objects", selectionSize));
    }

    // Show current layer info
    if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
      ILayer currentLayer = getActiveLayer();
      if (currentLayer != null) {
        String layerType = currentLayer instanceof ITileLayer ? "Tile" : "Objects";
        status.append("  |  Layer: ").append(currentLayer.getName()).append(" (").append(layerType).append(")");
      }
    }

    statusLabel.setText(status.toString());

    // Tool name — updates from transform mode
    String toolName = getToolDisplayName();
    toolLabel.setText(toolName);

    settingZoom = true;
    zoomComboBox.setSelectedItem(Zoom.getZoom());
    settingZoom = false;
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
