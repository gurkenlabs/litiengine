package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.animation.KeyFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AnimationTests {
  private static final String SPRITE_SHEET_NAME = "spritesheet.png";

  private Animation animation;

  @BeforeEach
  public void setUp() {
    Game.init();
    Spritesheet spritesheet = Mockito.mock(Spritesheet.class);
    Mockito.when(spritesheet.getName()).thenReturn(SPRITE_SHEET_NAME);
    Mockito.when(spritesheet.getTotalNumberOfSprites()).thenReturn(3);
    Mockito.when(spritesheet.isLoaded()).thenReturn(true);

    animation = new Animation(spritesheet, true, false, 100, 150, 200);
  }


  @Test
  void testStart() {
    animation.start();
    assertTrue(animation.isPlaying());
  }

  @Test
  void testRestart() {
    animation.restart();
    assertNotNull(animation.getCurrentKeyFrame());
  }

  @Test
  void testTerminate() {
    animation.terminate();
    assertFalse(animation.isPlaying());
  }

  @Test
  void testPauseAndUnpause() {
    animation.pause();
    assertTrue(animation.isPaused());

    animation.unpause();
    assertFalse(animation.isPaused());
  }

  @Test
  void testSetLooping() {
    animation.setLooping(false);
    assertFalse(animation.isLooping());

    animation.setLooping(true);
    assertTrue(animation.isLooping());
  }

  @Test
  void testSetDurationForAllKeyFrames() {
    animation.setDurationForAllKeyFrames(250);
    for (int keyFrameDuration : animation.getKeyFrameDurations()) {
      assertEquals(250, keyFrameDuration);
    }
  }

  @Test
  void testSetKeyFrameDurations() {
    animation.setKeyFrameDurations(300, 350, 400);
    int[] expectedDurations = {300, 350, 400};
    assertArrayEquals(expectedDurations, animation.getKeyFrameDurations());
  }

  @Test
  void testInitKeyFrames() {
    assertEquals(3, animation.getKeyFrameDurations().length);
    assertEquals(100, animation.getKeyFrameDurations()[0]);
    assertEquals(150, animation.getKeyFrameDurations()[1]);
    assertEquals(200, animation.getKeyFrameDurations()[2]);
  }

  @Test
  void testIsLastKeyFrame() {
    animation.setLooping(false);
    animation.restart();
    assertFalse(animation.isLastKeyFrame());
    KeyFrame kf = animation.getCurrentKeyFrame();
    animation.getKeyframes().clear();
    animation.getKeyframes().add(kf);
    assertTrue(animation.isLastKeyFrame());
  }
}
