package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScrollTest {
  @Test
  void fittedContentHidesScrollbar() {
    Scroll.AxisModel model = Scroll.AxisModel.create(936, 1000, 32, 32, 0);

    assertFalse(model.visible());
    assertEquals(468, model.minimumFocus());
    assertEquals(model.minimumFocus(), model.maximumFocus());
  }

  @Test
  void overflowingContentHasProportionalExtent() {
    Scroll.AxisModel model = Scroll.AxisModel.create(2000, 1000, 32, 32, 1000);

    assertTrue(model.visible());
    assertTrue(model.extent() > 400_000 && model.extent() < 500_000);
  }

  @Test
  void scrollbarEndpointsMapToFocusRange() {
    Scroll.AxisModel model = Scroll.AxisModel.create(2000, 1000, 32, 32, 1000);

    assertEquals(model.minimumFocus(), model.focusForValue(0), 0.001);
    assertEquals(model.maximumFocus(), model.focusForValue(1_000_000 - model.extent()), 0.001);
  }
}
