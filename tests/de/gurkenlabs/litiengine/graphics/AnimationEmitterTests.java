package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Point2D;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.emitters.AnimationEmitter;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;

public class AnimationEmitterTests {

  @Test
  public void testInitialization() {
    Spritesheet sheet = Resources.spritesheets().load(Imaging.getCompatibleImage(16, 16), "some-sprite", 16, 16);
    AnimationEmitter emitter = new AnimationEmitter(sheet, new Point2D.Double(0, 0));

    assertEquals(16, emitter.getWidth(), 0.0001);
    assertEquals(16, emitter.getHeight(), 0.0001);
    assertEquals(emitter.getTimeToLive(), Animation.DEFAULT_FRAME_DURATION);
  }
}
