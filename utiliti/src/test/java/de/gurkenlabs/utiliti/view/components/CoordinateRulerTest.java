package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CoordinateRulerTest {
  @Test
  void majorTicksUsePowerOfTwoWorldSteps() {
    assertEquals(128, CoordinateRuler.majorStep(1), 0.001);
    assertEquals(64, CoordinateRuler.majorStep(2), 0.001);
    assertEquals(256, CoordinateRuler.majorStep(0.5), 0.001);
  }
}
