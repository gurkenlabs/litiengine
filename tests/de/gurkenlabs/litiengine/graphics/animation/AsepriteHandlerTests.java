package de.gurkenlabs.litiengine.graphics.animation;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.resources.ImageFormat;

public class AsepriteHandlerTests {

  /**
   * Tests that Aseprite animation import works as expected when given valid input.
   * */
  @Test
  public void importAsepriteAnimationTest() {
    try {
      Animation animation = AsepriteHandler.importAnimation("tests/de/gurkenlabs/litiengine/graphics/animation/aseprite_test_animations/Sprite-0001.json");
      assertEquals("Sprite-0001-sheet", animation.getName());
      assertEquals(300, animation.getTotalDuration());
      for(int keyFrameDuration : animation.getKeyFrameDurations())
        assertEquals(100, keyFrameDuration);

      Spritesheet spriteSheet = animation.getSpritesheet();
      assertEquals(32, spriteSheet.getSpriteHeight());
      assertEquals(32, spriteSheet.getSpriteWidth());
      assertEquals(3, spriteSheet.getTotalNumberOfSprites());
      assertEquals(1, spriteSheet.getRows());
      assertEquals(3, spriteSheet.getColumns());
      assertEquals(ImageFormat.PNG, spriteSheet.getImageFormat());

      BufferedImage image = spriteSheet.getImage();
      assertEquals(96, image.getWidth());
      assertEquals(32, image.getHeight());
    }
    catch(FileNotFoundException e) {
      fail(e.getMessage());
    }
    catch(IOException e) {
      fail(e.getMessage());
    }
    catch(AsepriteHandler.ImportAnimationException e) {
      fail(e.getMessage());
    }
  }
}
