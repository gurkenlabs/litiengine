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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;


public class AsepriteHandlerTests {
  
  
  
  /**
   * Test that just create a json. Select some entries in json file to test.
   */
  @Test
  public void exportAnimationTest() {
    String spritesheetPath = "tests/de/gurkenlabs/litiengine/graphics/animation/aseprite_test_animations/Sprite-0001-sheet.png";
    BufferedImage image = new BufferedImage(96, 32, BufferedImage.TYPE_4BYTE_ABGR);
    Spritesheet spritesheet = new Spritesheet(image, spritesheetPath, 32, 32);
    Animation animation = new Animation(spritesheet, false, false, 2, 2, 2);
    int[] keyFrames = animation.getKeyFrameDurations();
    SpritesheetResource spritesheetResource = new SpritesheetResource(animation.getSpritesheet());
    spritesheetResource.setKeyframes(keyFrames);
    
    AsepriteHandler aseprite = new AsepriteHandler();
    String result = aseprite.exportAnimation(spritesheetResource);

    File asepriteJsonFile = new File(result);
    try {
      JsonElement rootElement = JsonParser.parseReader(new FileReader(asepriteJsonFile));
      JsonElement frames = rootElement.getAsJsonObject().get("frames");
      JsonObject firstFrameObject = frames.getAsJsonObject().entrySet().iterator().next().getValue().getAsJsonObject();
      JsonObject frameDimensions = firstFrameObject.get("sourceSize").getAsJsonObject();

      int frameWidth = frameDimensions.get("w").getAsInt();
      int frameHeight = frameDimensions.get("h").getAsInt();
      assertEquals(32, frameWidth);
      assertEquals(32, frameHeight);

      int duration = firstFrameObject.get("duration").getAsInt();
      assertEquals(100, duration);

      JsonElement meta = rootElement.getAsJsonObject().get("meta");
      JsonObject size = meta.getAsJsonObject().get("size").getAsJsonObject();
      int metaWidth = size.get("w").getAsInt();
      int metaHeight = size.get("h").getAsInt();
      assertEquals(96, metaWidth);
      assertEquals(32, metaHeight);

      JsonElement layers = meta.getAsJsonObject().get("layers");
      int opacity = layers.getAsJsonArray().get(0).getAsJsonObject().get("opacity").getAsInt();
      assertEquals(255, opacity);



    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
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
