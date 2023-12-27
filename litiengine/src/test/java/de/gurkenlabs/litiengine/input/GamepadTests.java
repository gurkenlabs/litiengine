package de.gurkenlabs.litiengine.input;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GamepadTests {

  @Test
  void testButtons() {
    String a = Gamepad.Xbox.A;
    float off = Gamepad.DPad.OFF;

    assertEquals("0", a);
    assertEquals(0.0f, off);
  }
}
