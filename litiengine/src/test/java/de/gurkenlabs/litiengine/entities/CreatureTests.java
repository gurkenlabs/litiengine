package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(GameTestSuite.class)
public class CreatureTests {

  private Creature creature;

  @BeforeAll
  public static void init() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @BeforeEach
  public void setUp() {
    creature = new TestCreature();
  }

  @Test
  void testInitializationByAnnotation_velocity() {
    // act
    int actualVelocity = creature.getVelocity().getModifiedValue().intValue();

    // assert
    assertEquals(111, actualVelocity);
  }

  @Test
  void testInitializationByAnnotation_acceleration() {
    // act
    int actualAcceleration = creature.getAcceleration();

    // assert
    assertEquals(222, actualAcceleration);
  }

  @Test
  void testInitializationByAnnotation_deceleration() {
    // act
    int actualDeceleration = creature.getDeceleration();

    // assert
    assertEquals(333, actualDeceleration);
  }

  @Test
  void testInitializationByAnnotation_turnOnMove() {
    // act
    boolean canTurnOnMove = creature.turnOnMove();

    // assert
    assertFalse(canTurnOnMove);
  }

  @ParameterizedTest(name = "testAcceleration initialAcceleration={0}, expectedAcceleration={1}")
  @CsvSource({"0, 50d", "100, 125d"})
  void testAcceleration(int initialAcceleration, double expectedAcceleration) {
    // arrange
    IMobileEntity mobileEntitySpy = spy(creature);
    when(mobileEntitySpy.getTickVelocity()).thenReturn(50f);
    mobileEntitySpy.setAcceleration(initialAcceleration);

    // act
    double actualAcceleration = mobileEntitySpy.getAcceleration(250);

    // assert
    assertEquals(expectedAcceleration, actualAcceleration);
  }

  @ParameterizedTest(name = "testDeceleration initialDeceleration={0}, expectedDeceleration={1}")
  @CsvSource({"0, 120d", "100, 125d"})
  void testDeceleration(int initialDeceleration, double expectedDeceleration) {
    // arrange
    IMobileEntity mobileEntitySpy = spy(creature);
    when(mobileEntitySpy.getTickVelocity()).thenReturn(50f);
    mobileEntitySpy.setDeceleration(initialDeceleration);

    // act
    double actualDeceleration = mobileEntitySpy.getDeceleration(250, 120);

    // assert
    assertEquals(expectedDeceleration, actualDeceleration);
  }

  @MovementInfo(velocity = 111, acceleration = 222, deceleration = 333, turnOnMove = false)
  private static class TestCreature extends Creature {
  }
}
