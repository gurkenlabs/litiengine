package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class IconThemeTest {

  @Test
  void existingIconUpdatesWhenThemeChanges() {
    Style.Theme original = Editor.preferences().getTheme();
    Icon icon = Icons.API_16;
    try {
      UI.setTheme(Style.Theme.DARK);
      assertEquals(new Color(0xE5E7EB), foregroundColor(icon));

      UI.setTheme(Style.Theme.LIGHT);
      assertEquals(new Color(0x4B5563), foregroundColor(icon));
    } finally {
      UI.setTheme(original);
    }
  }

  private static Color foregroundColor(Icon icon) {
    BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    icon.paintIcon(null, graphics, 0, 0);
    graphics.dispose();
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        Color color = new Color(image.getRGB(x, y), true);
        if (color.getAlpha() == 255) {
          return new Color(color.getRGB());
        }
      }
    }
    throw new AssertionError("Icon has no opaque foreground pixel");
  }
}
