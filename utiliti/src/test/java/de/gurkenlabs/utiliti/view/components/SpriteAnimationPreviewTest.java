package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SpriteAnimationPreviewTest {

  @AfterEach
  void cleanup() {
    Resources.spritesheets().remove("ratio-preview");
    Resources.spritesheets().remove("timer-preview");
    Resources.spritesheets().remove("timed-preview");
  }

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
    BufferedImage image = new BufferedImage(4, 2, BufferedImage.TYPE_INT_ARGB);
    Spritesheet spritesheet = new Spritesheet(image, "timer-preview.png", 2, 2);
    SpriteAnimationPreview preview = new SpriteAnimationPreview();
    preview.setSpritesheet(spritesheet);

    preview.start();
    assertTrue(preview.isRunningForTest());
    preview.stop();

    assertFalse(preview.isRunningForTest());
  }

  @Test
  void previewUsesConfiguredFrameDurations() {
    BufferedImage image = new BufferedImage(4, 2, BufferedImage.TYPE_INT_ARGB);
    SpritesheetResource resource = new SpritesheetResource(image, "timed-preview", 2, 2);
    resource.setKeyframes(new int[] {50, 240});
    Spritesheet spritesheet = Resources.spritesheets().load(resource);
    SpriteAnimationPreview preview = new SpriteAnimationPreview();

    preview.setSpritesheet(spritesheet);

    assertEquals(0, preview.getCurrentFrameForTest());
    assertEquals(50, preview.getTimerDelayForTest());
    preview.advanceFrameForTest();
    assertEquals(1, preview.getCurrentFrameForTest());
    assertEquals(240, preview.getTimerDelayForTest());
  }
}
