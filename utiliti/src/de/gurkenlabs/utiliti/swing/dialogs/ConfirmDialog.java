package de.gurkenlabs.utiliti.swing.dialogs;

import javax.swing.JOptionPane;

import de.gurkenlabs.litiengine.Game;

public class ConfirmDialog {

  private ConfirmDialog() {
  }

  public static boolean show(String title, String message) {
    int n = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), message, title, JOptionPane.YES_NO_OPTION);
    return n == JOptionPane.YES_OPTION;
  }
}
