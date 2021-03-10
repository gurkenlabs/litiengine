package de.gurkenlabs.litiengine.graphics.animation;

import java.io.File;
import java.io.FileReader;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.AsepriteHandler.ImportAnimationException;
import de.gurkenlabs.litiengine.graphics.animation.AsepriteHandler.ExportAnimationException;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;

public class AsepriteHandlerTests { 

  /**
   * Tests that Aseprite animation import works as expected when given valid input.
   */
  @Test
  public void importAsepriteAnimationTest() {
    try {
      Animation animation = AsepriteHandler.importAnimation("tests/de/gurkenlabs/litiengine/graphics/animation/aseprite_test_animations/Sprite-0001.json");
      Animation animation2 = AsepriteHandler.importAnimation("tests/de/gurkenlabs/litiengine/graphics/animation/aseprite_test_animations/Sprite-0005.json");
      assertEquals("Sprite-0005", animation2.getName());
      assertEquals("Sprite-0001-sheet", animation.getName());
      assertEquals(300, animation.getTotalDuration());
      for (int keyFrameDuration : animation.getKeyFrameDurations())
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
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    } catch (IOException e) {
      fail(e.getMessage());
    } catch (AsepriteHandler.ImportAnimationException e) {
      fail(e.getMessage());
    }
  } 

  /**
   * Test that if AsepriteHandler.ImportAnimationException will be throwed if different frame dimensions are provided.
   */
  @Test
  public void ImportAnimationExceptionTest() {   
    Throwable exception = assertThrows(ImportAnimationException.class, () -> AsepriteHandler.importAnimation("tests/de/gurkenlabs/litiengine/graphics/animation/aseprite_test_animations/Sprite-0002.json"));
    assertEquals("AsepriteHandler.ImportAnimationException: animation key frames require same dimensions.", exception.getMessage());
  }

  /**
   * Tests thrown FileNotFoundException when importing an Aseprite animation.
   * <p>
   * 1.first, we test if FileNotFoundException would be throwed if .json file cannot be found.
   * 2.then we test if FileNotFoundException would be throwed if spritesheet file cannot be found.
   */
  @Test
  public void FileNotFoundExceptionTest() {
    Throwable exception_withoutJsonFile = assertThrows(FileNotFoundException.class, () -> AsepriteHandler.importAnimation("tests/de/gurkenlabs/litiengine/graphics/animation/aseprite_test_animations/Sprite-0003.json"));
    assertEquals("FileNotFoundException: Could not find .json file tests/de/gurkenlabs/litiengine/graphics/animation/aseprite_test_animations/Sprite-0003.json", exception_withoutJsonFile.getMessage());
    Throwable exception_withoutSpriteSheet = assertThrows(FileNotFoundException.class, () -> AsepriteHandler.importAnimation("tests/de/gurkenlabs/litiengine/graphics/animation/aseprite_test_animations/Sprite-0004.json"));
    assertEquals("FileNotFoundException: Could not find sprite sheet file. Expected location is 'image' in .json metadata, or same folder as .json file.", exception_withoutSpriteSheet.getMessage());
  }

  /**
   * Test if exportAnimationException would be thrown when keyframes and spritesheet file have different dimensions.
   */
  @Test
  public void ExportAnimationExceptionTest(){
    String spritesheetPath = "tests/de/gurkenlabs/litiengine/graphics/animation/aseprite_test_animations/Sprite-0001-sheet.png";
    BufferedImage image = new BufferedImage(96, 32, BufferedImage.TYPE_4BYTE_ABGR);
    Spritesheet spritesheet = new Spritesheet(image, spritesheetPath, 32, 32);
    Animation animation = new Animation(spritesheet, false, false, 2, 2);
    int[] keyFrames = animation.getKeyFrameDurations();
    SpritesheetResource spritesheetResource = new SpritesheetResource(animation.getSpritesheet());
    spritesheetResource.setKeyframes(keyFrames);
    Throwable exception = assertThrows(ExportAnimationException.class, () -> AsepriteHandler.exportAnimation(spritesheetResource));
    assertEquals("Different dimensions of keyframes and sprites in spritesheet", exception.getMessage());    
  }
}
