package de.gurkenlabs.litiengine.graphics;

import org.junit.Assert;
import org.junit.Test;

public class CameraTests {

  @Test
  public void testInit() {
    Camera cam = new Camera();
    Assert.assertEquals(cam.getZoom(), 1, 0.0001);
  }

  @Test
  public void testSimpleZoom() {
    Camera cam = new Camera();
    cam.setZoom(5, 0);

    Assert.assertEquals(cam.getZoom(), 5, 0.0001);
  }
}
