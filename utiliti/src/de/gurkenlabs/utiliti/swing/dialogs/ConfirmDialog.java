package de.gurkenlabs.utiliti.swing.dialogs;

import de.gurkenlabs.litiengine.Game;
import javax.swing.JOptionPane;

public class ConfirmDialog {

  private ConfirmDialog() {}

  public static boolean show(String title, String message) {
    int n =
        JOptionPane.showConfirmDialog(
            Game.window().getRenderComponent(), message, title, JOptionPane.YES_NO_OPTION);
    return n == JOptionPane.YES_OPTION;
  }
}
