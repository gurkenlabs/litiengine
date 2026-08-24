package de.gurkenlabs.utiliti.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class IconsTest {

  @Test
  void debugIconsAreLoadedWithCorrectDimensions() {
    assertNotNull(Icons.GREEN_DEBUG_16);
    assertNotNull(Icons.DISABLED_DEBUG_16);
    assertEquals(16, Icons.GREEN_DEBUG_16.getIconWidth());
    assertEquals(16, Icons.GREEN_DEBUG_16.getIconHeight());
    assertEquals(16, Icons.DISABLED_DEBUG_16.getIconWidth());
    assertEquals(16, Icons.DISABLED_DEBUG_16.getIconHeight());

    BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    try {
      Icons.GREEN_DEBUG_16.paintIcon(new JLabel(), graphics, 0, 0);
      Icons.DISABLED_DEBUG_16.paintIcon(new JLabel(), graphics, 0, 0);
    } finally {
      graphics.dispose();
    }
  }

  @Test
  void scriptIconsAreLoadedWithCorrectDimensionsAndRender() {
    assertNotNull(Icons.SCRIPT_16);
    assertNotNull(Icons.SCRIPT_ENTITY_16);
    assertNotNull(Icons.SCRIPT_ENVIRONMENT_16);
    assertNotNull(Icons.SCRIPT_GAME_16);
    assertNotNull(Icons.SCRIPT_INHERITED_16);

    assertEquals(16, Icons.SCRIPT_16.getIconWidth());
    assertEquals(16, Icons.SCRIPT_16.getIconHeight());
    assertEquals(16, Icons.SCRIPT_ENTITY_16.getIconWidth());
    assertEquals(16, Icons.SCRIPT_ENVIRONMENT_16.getIconWidth());
    assertEquals(16, Icons.SCRIPT_GAME_16.getIconWidth());
    assertEquals(16, Icons.SCRIPT_INHERITED_16.getIconWidth());

    BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    try {
      Icons.SCRIPT_16.paintIcon(new JLabel(), graphics, 0, 0);
      Icons.SCRIPT_ENTITY_16.paintIcon(new JLabel(), graphics, 0, 0);
      Icons.SCRIPT_ENVIRONMENT_16.paintIcon(new JLabel(), graphics, 0, 0);
      Icons.SCRIPT_GAME_16.paintIcon(new JLabel(), graphics, 0, 0);
      Icons.SCRIPT_INHERITED_16.paintIcon(new JLabel(), graphics, 0, 0);
    } finally {
      graphics.dispose();
    }

    assertNotNull(Icons.getScriptIcon(de.gurkenlabs.litiengine.scripting.ScriptHostType.ENTITY));
    assertNotNull(Icons.getScriptIcon(de.gurkenlabs.litiengine.scripting.ScriptHostType.ENVIRONMENT));
    assertNotNull(Icons.getScriptIcon(de.gurkenlabs.litiengine.scripting.ScriptHostType.GAME));
    assertNotNull(Icons.getScriptIcon((de.gurkenlabs.litiengine.scripting.ScriptHostType) null));
    assertNotNull(Icons.getScriptIcon((de.gurkenlabs.litiengine.scripting.ScriptDefinition) null));
  }
}
