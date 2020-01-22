package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class MobileEntityTests {
  @Test
  public void testInitializationByAnnotation() {
    IMobileEntity entity = new TestMobileEntity();

    assertEquals(111, entity.getVelocity().get().intValue());
    assertEquals(222, entity.getAcceleration());
    assertEquals(333, entity.getDeceleration());
    assertFalse(entity.turnOnMove());
  }

  @MovementInfo(velocity = 111, acceleration = 222, deceleration = 333, turnOnMove = false)
  private class TestMobileEntity extends MobileEntity {

  }
}
