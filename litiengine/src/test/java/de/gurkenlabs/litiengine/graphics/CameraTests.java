package de.gurkenlabs.litiengine.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CameraTests {

  @Test
  public void testInit() {
    Camera cam = new Camera();
    assertEquals(cam.getZoom(), 1, 0.0001);
  }

  @Test
  public void testSimpleZoom() {
    Camera cam = new Camera();
    cam.setZoom(5, 0);

    assertEquals(cam.getZoom(), 5, 0.0001);
  }
}
