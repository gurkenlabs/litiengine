package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class RoundedPanelTest {

  @Test
  void clipsOpaqueChildrenAtCorners() {
    RoundedPanel panel = new RoundedPanel(new BorderLayout(), 12);
    JPanel content = new JPanel();
    content.setBackground(Color.RED);
    panel.add(content);
    panel.setSize(40, 40);
    panel.doLayout();

    BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    try {
      panel.paint(graphics);
    } finally {
      graphics.dispose();
    }

    assertEquals(0, image.getRGB(0, 0) >>> 24);
    assertNotEquals(0, image.getRGB(20, 20) >>> 24);
  }
}
