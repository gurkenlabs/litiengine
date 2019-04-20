package de.gurkenlabs.utiliti.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.components.EditorScreen;

public final class StatusBar {
  private static JLabel statusLabel;

  private StatusBar() {
  }

  public static JLabel create() {
    statusLabel = new JLabel("");
    statusLabel.setPreferredSize(new Dimension(0, 16));
    statusLabel.setFont(new Font(Style.FONTNAME_CONSOLE, Font.PLAIN, 11));
    statusLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
    return statusLabel;
  }

  public static void update() {
    Point tile = Input.mouse().getTile();
    String positionX = "x: " + (int) Input.mouse().getMapLocation().getX() + "[" + tile.x + "]";
    String positionY = "y: " + (int) Input.mouse().getMapLocation().getY() + "[" + tile.y + "]";
    String status = String.format("%-14s %-14s", positionX, positionY) + String.format(" %-10s", (int) (Game.world().camera().getRenderScale() * 100) + "%");

    int size = EditorScreen.instance().getMainComponent().getSelectedMapObjects().size();
    if (size <= 0) {
      statusLabel.setText("");
    } else {

      status += Resources.strings().get("status_selected_objects", size);
    }

    statusLabel.setText(status);
  }
}
