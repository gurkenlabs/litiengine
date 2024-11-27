package de.gurkenlabs.utiliti;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

/**
 * A utility class that provides helper methods for Swing components.
 */
public class SwingHelpers {
  private SwingHelpers() {
  }

  /**
   * Updates the text field with the encoded color value and sets the background and foreground colors. If the color is null, the method returns
   * without making any changes.
   *
   * @param textField the JTextComponent to be updated
   * @param color     the Color to be encoded and set on the text field
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
   * Updates the label with the encoded color value and sets an icon representing the color.
   *
   * @param label the JLabel to be updated
   * @param color the Color to be encoded and set on the label
   */
  public static void updateColorLabel(final JLabel label, Color color) {
    if (color == null) {
      return;
    }
    label.setText(ColorHelper.encode(color));

    final String cacheKey = ColorHelper.encode(color);

    BufferedImage newIconImage =
      Resources.images()
        .get(
          cacheKey,
          () -> {
            BufferedImage img = Imaging.getCompatibleImage(10, 10);
            if (img == null) {
              return null;
            }
            Graphics2D g = (Graphics2D) img.getGraphics();
            g.setColor(color);
            g.fillRect(0, 0, PropertyPanel.CONTROL_HEIGHT, PropertyPanel.CONTROL_HEIGHT);
            g.dispose();
            return img;
          });

    label.setIcon(new ImageIcon(newIconImage));
  }
}
