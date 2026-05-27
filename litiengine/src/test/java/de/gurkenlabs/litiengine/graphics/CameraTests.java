package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameWindow;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.tweening.TweenType;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class CameraTests {

  @Test
  void testInit() {
    Camera cam = new Camera();
    assertEquals(1, cam.getZoom(), 0.0001);
  }

  @Test
  void testSimpleZoom() {
    Camera cam = new Camera();
    cam.setZoom(5, 0);

    assertEquals(5, cam.getZoom(), 0.0001);
  }

  @Test
  void testClampToMap_NoEnvironmentOrMap() {
    Camera cam = new Camera();
    cam.setClampToMap(true);

    try (MockedStatic<Game> gameMock = mockStatic(Game.class)) {
      GameWorld world = mock(GameWorld.class);
      when(world.environment()).thenReturn(null);
      gameMock.when(Game::world).thenReturn(world);

      Point2D input = new Point2D.Double(50, 50);
      Point2D result = cam.clampToMap(input);
      assertEquals(input.getX(), result.getX(), 0.0001);
      assertEquals(input.getY(), result.getY(), 0.0001);
    }
  }

  @Test
  void testClampToMap_ClampToMapDisabled() {
    Camera cam = new Camera();
    cam.setClampToMap(false);

    Point2D input = new Point2D.Double(-100, 9999);
    Point2D result = cam.clampToMap(input);
    assertEquals(input.getX(), result.getX(), 0.0001);
    assertEquals(input.getY(), result.getY(), 0.0001);
  }

  @Test
  void testClampToMap_ClampingWithinBounds() {
    Camera cam = spy(new Camera() {
      @Override
      protected double getViewportWidth() {
        return 40;
      }

      @Override
      protected double getViewportHeight() {
        return 40;
      }
    });
    cam.setClampToMap(true);

    try (MockedStatic<Game> gameMock = mockStatic(Game.class)) {
      GameWorld world = mock(GameWorld.class);
      Environment env = mock(Environment.class);
      IMap map = mock(IMap.class);
      GameWindow window = mock(GameWindow.class);

      when(world.environment()).thenReturn(env);
      when(env.getMap()).thenReturn(map);
      when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
      gameMock.when(Game::world).thenReturn(world);
      gameMock.when(Game::window).thenReturn(window);
      when(window.getResolution()).thenReturn(new Dimension(100, 100));

      // Point inside bounds
      Point2D input = new Point2D.Double(50, 50);
      Point2D result = cam.clampToMap(input);
      assertEquals(50, result.getX(), 0.0001);
      assertEquals(50, result.getY(), 0.0001);

      // Point outside left/top
      input = new Point2D.Double(0, 0);
      result = cam.clampToMap(input);
      assertEquals(20, result.getX(), 0.0001); // minX = viewportWidth/2
      assertEquals(20, result.getY(), 0.0001);

      // Point outside right/bottom
      input = new Point2D.Double(100, 100);
      result = cam.clampToMap(input);
      assertEquals(80, result.getX(), 0.0001); // maxX = mapWidth - viewportWidth/2
      assertEquals(80, result.getY(), 0.0001);
    }
  }

  @Test
  void testCameraTweenValuesForZoom() {
    Camera cam = new Camera();
    cam.setZoom(2.5f, 0);
    float[] values = cam.getTweenValues(TweenType.ZOOM);
    assertEquals(1, values.length);
    assertEquals(2.5f, values[0], 0.0001f);

    cam.setTweenValues(TweenType.ZOOM, new float[] {4.0f});
    assertEquals(4.0f, cam.getZoom(), 0.0001f);
  }

  @Test
  void testCameraTweenValuesForLocation() {
    Camera cam = new Camera();
    cam.setFocus(new Point2D.Double(10, 20));
    float[] xy = cam.getTweenValues(TweenType.LOCATION_XY);
    assertEquals(2, xy.length);
    assertEquals(10f, xy[0], 0.05f);
    // setFocus may apply a 0.01 offset to certain Y values; only assert closeness
    assertEquals(20f, xy[1], 0.05f);

    cam.setTweenValues(TweenType.LOCATION_XY, new float[] {30f, 40f});
    assertEquals(30f, cam.getFocus().getX(), 0.05f);
    assertEquals(40f, cam.getFocus().getY(), 0.05f);
  }
}
