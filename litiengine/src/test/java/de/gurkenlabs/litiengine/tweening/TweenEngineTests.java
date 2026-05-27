package de.gurkenlabs.litiengine.tweening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class TweenEngineTests {

  /**
   * A simple, lightweight {@link Tweenable} used to drive Tween updates without depending on any other game system.
   */
  private static class TestTweenable implements Tweenable {
    private float value;

    TestTweenable(float value) {
      this.value = value;
    }

    @Override
    public float[] getTweenValues(final TweenType tweenType) {
      return new float[] {this.value};
    }

    @Override
    public void setTweenValues(final TweenType tweenType, final float[] newValues) {
      this.value = newValues[0];
    }
  }

  private static GameTime mockTime(final AtomicLong currentTick) {
    final GameTime time = mock(GameTime.class);
    when(time.now()).thenAnswer(invocation -> currentTick.get());
    when(time.since(org.mockito.ArgumentMatchers.anyLong()))
        .thenAnswer(invocation -> currentTick.get() - (long) invocation.getArgument(0));
    return time;
  }

  @Test
  void testTweenStopsAfterDuration() {
    final AtomicLong tick = new AtomicLong(0);
    final GameTime time = mockTime(tick);
    try (MockedStatic<Game> gameMock = mockStatic(Game.class)) {
      gameMock.when(Game::time).thenReturn(time);

      final TestTweenable target = new TestTweenable(0f);
      final TweenEngine engine = new TweenEngine();
      final Tween tween = engine.start(target, TweenType.UNDEFINED, 10).ease(TweenFunction.LINEAR).target(100f);

      tick.set(5);
      engine.update();
      assertTrue(tween.isRunning());
      assertEquals(50f, target.value, 0.0001f);

      tick.set(10);
      engine.update();
      assertTrue(tween.hasStopped());
      assertEquals(100f, target.value, 0.0001f);
    }
  }

  @Test
  void testLoopableTweenRestarts() {
    final AtomicLong tick = new AtomicLong(0);
    final GameTime time = mockTime(tick);
    try (MockedStatic<Game> gameMock = mockStatic(Game.class)) {
      gameMock.when(Game::time).thenReturn(time);

      final TestTweenable target = new TestTweenable(0f);
      final TweenEngine engine = new TweenEngine();
      final Tween tween =
          engine.start(target, TweenType.UNDEFINED, 10)
              .ease(TweenFunction.LINEAR)
              .target(100f)
              .loop(TweenLoop.LOOP);

      tick.set(10);
      engine.update(); // completes the first cycle, should restart
      assertFalse(tween.hasStopped());
      // The tween just completed a cycle (target value applied) and immediately re-began.
      // The next update will resume interpolating from the start values.

      tick.set(15);
      engine.update();
      assertEquals(50f, target.value, 0.0001f);
    }
  }

  @Test
  void testPingPongTweenReversesDirection() {
    final AtomicLong tick = new AtomicLong(0);
    final GameTime time = mockTime(tick);
    try (MockedStatic<Game> gameMock = mockStatic(Game.class)) {
      gameMock.when(Game::time).thenReturn(time);

      final TestTweenable target = new TestTweenable(0f);
      final TweenEngine engine = new TweenEngine();
      final Tween tween =
          engine.start(target, TweenType.UNDEFINED, 10)
              .ease(TweenFunction.LINEAR)
              .target(100f)
              .loop(TweenLoop.PINGPONG);

      tick.set(10);
      engine.update(); // completes the first cycle going forward, should reverse direction
      assertFalse(tween.hasStopped());
      assertTrue(tween.isReversed());
      assertEquals(100f, target.value, 0.0001f);

      tick.set(15);
      engine.update();
      assertEquals(50f, target.value, 0.0001f); // now interpolating backwards

      tick.set(20);
      engine.update(); // second cycle complete, should swing back forward again
      assertFalse(tween.isReversed());
      assertEquals(0f, target.value, 0.0001f);
    }
  }

  @Test
  void testTweenListenerEvents() {
    final AtomicLong tick = new AtomicLong(0);
    final GameTime time = mockTime(tick);
    try (MockedStatic<Game> gameMock = mockStatic(Game.class)) {
      gameMock.when(Game::time).thenReturn(time);

      final TestTweenable target = new TestTweenable(0f);
      final TweenEngine engine = new TweenEngine();
      final Tween tween = engine.start(target, TweenType.UNDEFINED, 10).ease(TweenFunction.LINEAR).target(100f);

      final AtomicReference<Tween> started = new AtomicReference<>();
      final AtomicReference<Tween> stopped = new AtomicReference<>();
      final AtomicReference<Tween> completed = new AtomicReference<>();
      tween.addListener(new TweenListener() {
        @Override
        public void started(Tween t) {
          started.set(t);
        }

        @Override
        public void stopped(Tween t) {
          stopped.set(t);
        }

        @Override
        public void completed(Tween t) {
          completed.set(t);
        }
      });

      // re-trigger start so listeners are notified
      tween.start();
      assertNotNull(started.get());
      assertNull(stopped.get());
      assertNull(completed.get());

      tick.set(10);
      engine.update();
      assertNotNull(completed.get());
      assertNotNull(stopped.get());
    }
  }

  @Test
  void testListenerStopFiresOnlyOnce() {
    final TestTweenable target = new TestTweenable(0f);
    final Tween tween = new Tween(target, TweenType.UNDEFINED, 10);
    final AtomicLong stopCount = new AtomicLong();
    tween.addListener(new TweenListener() {
      @Override
      public void stopped(Tween t) {
        stopCount.incrementAndGet();
      }
    });
    tween.stop();
    tween.stop();
    assertEquals(1L, stopCount.get());
  }

  @Test
  void testRemoveListener() {
    final TestTweenable target = new TestTweenable(0f);
    final Tween tween = new Tween(target, TweenType.UNDEFINED, 10);
    final AtomicLong stopCount = new AtomicLong();
    final TweenListener listener = new TweenListener() {
      @Override
      public void stopped(Tween t) {
        stopCount.incrementAndGet();
      }
    };
    tween.addListener(listener);
    tween.removeListener(listener);
    tween.stop();
    assertEquals(0L, stopCount.get());
  }

  @Test
  void testLoopDefaultsToNone() {
    final Tween tween = new Tween(new TestTweenable(0f), TweenType.UNDEFINED, 10);
    assertEquals(TweenLoop.NONE, tween.getLoop());
    tween.loop(null);
    assertEquals(TweenLoop.NONE, tween.getLoop());
    tween.loop(TweenLoop.PINGPONG);
    assertEquals(TweenLoop.PINGPONG, tween.getLoop());
  }
}
