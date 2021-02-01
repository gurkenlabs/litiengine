package com.litiengine.utiliti.swing;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.litiengine.input.Input;
import com.litiengine.resources.Resources;
import com.litiengine.utiliti.components.Editor;
import com.litiengine.utiliti.handlers.Zoom;
import com.litiengine.utiliti.Style;

public final class StatusBar {
  private static JPanel panel;
  private static JLabel statusLabel;
  private static JComboBox<Zoom> zoomComboBox;

  private static boolean settingZoom;

  private StatusBar() {
  }

  public static Container create() {
    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    statusLabel = new JLabel("");
    statusLabel.setFont(new Font(Style.FONTNAME_CONSOLE, Font.PLAIN, (int) (12 * Editor.preferences().getUiScale())));

    zoomComboBox = new JComboBox<>(Zoom.getAll());
    zoomComboBox.addItemListener(e -> {
      if (settingZoom || e.getStateChange() != ItemEvent.SELECTED) {
        return;
      }

      Zoom.set(((Zoom) zoomComboBox.getSelectedItem()).getValue());
    });

    panel.add(zoomComboBox);
    panel.add(statusLabel);
    return panel;

  }

  public static void update() {
    String position = String.format("x/y: %d,%d", (int) Input.mouse().getMapLocation().getX(), (int) Input.mouse().getMapLocation().getY());
    String tile = String.format("Tile: %d,%d", Input.mouse().getTile().x, Input.mouse().getTile().y);
    String status = String.format("%-14s %-10s", position, tile);

    int size = Editor.instance().getMapComponent().getSelectedMapObjects().size();
    if (size <= 0) {
      statusLabel.setText("");
    } else {
      status += "  " + Resources.strings().get("status_selected_objects", size);
    }

    statusLabel.setText(status);

    settingZoom = true;
    zoomComboBox.setSelectedItem(Zoom.getZoom());
    settingZoom = false;
  }
}
