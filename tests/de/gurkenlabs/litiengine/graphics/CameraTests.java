package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
}
