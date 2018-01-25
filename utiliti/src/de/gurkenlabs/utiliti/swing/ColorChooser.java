package de.gurkenlabs.utiliti.swing;

import java.awt.Color;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import de.gurkenlabs.litiengine.Game;

public final class ColorChooser {
  public static final Color showRgbDialog(String title, Color color) {
    JColorChooser chooser = new JColorChooser();
    chooser.setColor(color);
    for (AbstractColorChooserPanel accp : chooser.getChooserPanels()) {
      if (accp.getDisplayName().equals("RGB")) {
        int result = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), accp, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
          return chooser.getColor();
        }

        return color;
      }
    }

    return color;
  }
}
