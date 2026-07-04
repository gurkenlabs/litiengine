package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.Zoom;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.Objects;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public final class StatusBar {
  private static JLabel statusLabel;
  private static JLabel toolLabel;
  private static JComboBox<Zoom> zoomComboBox;

  private static boolean settingZoom;

  private StatusBar() {}

  public static Container create() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setBorder(new EmptyBorder(2, 6, 2, 6));

    int fs = (int) (11 * Editor.preferences().getUiScale());

    zoomComboBox = new JComboBox<>(Zoom.getAll());
    zoomComboBox.setFont(new Font(Style.FONTNAME_CONSOLE, Font.PLAIN, fs));
    zoomComboBox.setMaximumSize(new Dimension(80, 24));

    statusLabel = new JLabel("");
    statusLabel.setFont(new Font(Style.FONTNAME_CONSOLE, Font.PLAIN, fs));
    statusLabel.setForeground(Style.COLOR_SUBTEXT);

    toolLabel = new JLabel("");
    toolLabel.setFont(new Font(Style.FONTNAME_CONSOLE, Font.PLAIN, fs));
    toolLabel.setForeground(Style.COLOR_ACCENT_BLUE);

    zoomComboBox.addItemListener(
        e -> {
          if (settingZoom || e.getStateChange() != ItemEvent.SELECTED) {
            return;
          }
          Zoom.set(((Zoom) Objects.requireNonNull(zoomComboBox.getSelectedItem())).getValue());
        });

    panel.add(zoomComboBox);
    panel.add(Box.createRigidArea(new Dimension(8, 0)));
    panel.add(statusLabel);
    panel.add(Box.createHorizontalGlue());
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
