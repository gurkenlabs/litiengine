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
   *                    the text field
   */
  public static void updateColorTextField(final JTextComponent textField) {
    final Color fromText = ColorHelper.decode(textField.getText(), true);
    if (fromText == null) {
      return;
    }
    final float[] hsb = Color.RGBtoHSB(fromText.getRed(), fromText.getGreen(), fromText.getBlue(), null);
    final Color contrastColor = hsb[2] > 0.7 ? Color.black : Color.white;
    textField.setBackground(fromText);
    textField.setForeground(contrastColor);
  }

}
