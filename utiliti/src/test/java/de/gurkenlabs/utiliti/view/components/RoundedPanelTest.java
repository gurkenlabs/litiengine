package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.utiliti.model.Style;
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

  @Test
  void childRepaintsCannotOverwriteTheOutline() {
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
      int[] before = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());

      Graphics2D childGraphics = (Graphics2D) graphics.create(
          content.getX(), content.getY(), content.getWidth(), content.getHeight());
      try {
        content.paint(childGraphics);
      } finally {
        childGraphics.dispose();
      }

      int outline = Style.border().getRGB();
      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
          int previousPixel = before[y * image.getWidth() + x];
          if (previousPixel == outline) {
            assertEquals(previousPixel, image.getRGB(x, y));
          }
        }
      }
    } finally {
      graphics.dispose();
    }
  }

  @Test
  void dockPanelsHaveAUniformMargin() {
    JPanel content = new JPanel();

    JPanel container = UI.createDockPanel(content);

    assertEquals(4, container.getInsets().top);
    assertEquals(4, container.getInsets().left);
    assertEquals(4, container.getInsets().bottom);
    assertEquals(4, container.getInsets().right);
    assertSame(content, ((BorderLayout) container.getLayout()).getLayoutComponent(BorderLayout.CENTER));
  }

  @Test
  void workspaceDockPanelAlignsWithCanvasTop() {
    JPanel container = UI.createDockPanel(new JPanel(), 0);

    assertEquals(0, container.getInsets().top);
    assertEquals(4, container.getInsets().left);
    assertEquals(4, container.getInsets().bottom);
    assertEquals(4, container.getInsets().right);
  }
}
