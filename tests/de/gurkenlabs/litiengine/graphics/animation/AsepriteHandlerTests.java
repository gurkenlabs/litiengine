package de.gurkenlabs.litiengine.graphics.animation;

import java.io.FileNotFoundException;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;

public class AsepriteHandlerTests {


  @Test
  public void exportAnimationTest() {
    String spritesheetPath = "tests/de/gurkenlabs/litiengine/graphics/animation/aseprite_test_animation/Sprite-0001.json";
    BufferedImage image = new BufferedImage(96, 32, BufferedImage.TYPE_4BYTE_ABGR);
    Spritesheet spritesheet = new Spritesheet(image, spritesheetPath, 32, 32);
    Animation animation = new Animation(spritesheet, false, false, 2,2,2);

    AsepriteHandler aseprite = new AsepriteHandler();
    aseprite.exportAnimation(animation);

  }
}