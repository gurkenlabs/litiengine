package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreatureTests {

  private Creature creature;

  @BeforeEach
  public void setUp(){
    creature = new TestCreature();
  }

  @Test
  public void testInitializationByAnnotation_velocity(){
    // act
    int actualVelocity = creature.getVelocity().get().intValue();

    // assert
    assertEquals(111, actualVelocity);
  }

  @Test
  public void testInitializationByAnnotation_acceleration(){
    // act
    int actualAcceleration = creature.getAcceleration();

    // assert
    assertEquals(222, actualAcceleration);
  }

  @Test
  public void testInitializationByAnnotation_deceleration(){
    // act
    int actualDeceleration = creature.getDeceleration();

    // assert
    assertEquals(333, actualDeceleration);
  }

  @Test
  public void testInitializationByAnnotation_turnOnMove(){
    // act
    boolean canTurnOnMove = creature.turnOnMove();

    // assert
    assertFalse(canTurnOnMove);
  }

  @MovementInfo(velocity = 111, acceleration = 222, deceleration = 333, turnOnMove = false)
  private class TestCreature extends Creature {
  }
}
