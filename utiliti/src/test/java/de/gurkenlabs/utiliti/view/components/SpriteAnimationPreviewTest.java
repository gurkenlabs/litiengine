package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class SpriteAnimationPreviewTest {

  @Test
  void previewPreservesFrameAspectRatio() {
    BufferedImage image = new BufferedImage(20, 40, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, 0xffffffff);
    Spritesheet spritesheet = new Spritesheet(image, "ratio-preview.png", 20, 40);
    SpriteAnimationPreview preview = new SpriteAnimationPreview();

    preview.setSpritesheet(spritesheet);

    assertNotNull(preview.getIconForTest());
    assertEquals(48, preview.getIconForTest().getIconWidth());
    assertEquals(96, preview.getIconForTest().getIconHeight());
  }

  @Test
  void previewTimerCanStopWhilePreviewIsHidden() {
    SpriteAnimationPreview preview = new SpriteAnimationPreview();

    preview.start();
    assertTrue(preview.isRunningForTest());
    preview.stop();

    assertFalse(preview.isRunningForTest());
  }
}
