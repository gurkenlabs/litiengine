package com.litiengine.utiliti;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import com.litiengine.resources.Resources;
import com.litiengine.util.ColorHelper;
import com.litiengine.util.Imaging;
import com.litiengine.utiliti.swing.panels.PropertyPanel;

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

  /**
   * Update color text field background to the decoded color value.
   *
   * @param label
   *          the text field
   */
  public static void updateColorLabel(final JLabel label, Color color) {
    if (color == null) {
      return;
    }
    label.setText(ColorHelper.encode(color));

    final String cacheKey = ColorHelper.encode(color);

    BufferedImage newIconImage = Resources.images().get(cacheKey, () -> {
      BufferedImage img = Imaging.getCompatibleImage(10, 10);
      Graphics2D g = (Graphics2D) img.getGraphics();
      g.setColor(color);
      g.fillRect(0, 0, PropertyPanel.CONTROL_HEIGHT, PropertyPanel.CONTROL_HEIGHT);
      g.dispose();
      return img;
    });

    label.setIcon(new ImageIcon(newIconImage));

  }
}
