package de.gurkenlabs.utiliti;

import java.awt.Color;

import javax.swing.text.JTextComponent;

import de.gurkenlabs.litiengine.util.ColorHelper;

public class SwingHelpers {
  private SwingHelpers() {
  }

  /**
   * Update color text field background to the decoded color value.
   *
   * @param textField
   *          the text field
   */
  public static void updateColorTextField(final JTextComponent textField, Color color) {
    if (color == null) {
      return;
    }
    final float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    final Color contrastColor = hsb[2] > 0.7 ? Color.black : Color.white;
    textField.setText(ColorHelper.encode(color));
    textField.setBackground(color);
    textField.setForeground(contrastColor);
  }

}
