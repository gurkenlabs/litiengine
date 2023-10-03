package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTime;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.attributes.RangeAttribute;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import de.gurkenlabs.litiengine.tweening.TweenType;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
class CombatEntityTests {

  private CombatEntity combatEntitySpy;
  private CombatEntityListener entityListener;
  private Ability ability;

  @BeforeEach
  public void setUp() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);

    combatEntitySpy = spy(new CombatEntity());

    entityListener = mock(CombatEntityListener.class);
    combatEntitySpy.addCombatEntityListener(entityListener);

    ability = mock(Ability.class);
  }

  @Test
  void testDieIsDead() {
    // arrange
    when(combatEntitySpy.isDead()).thenReturn(true);
    verify(combatEntitySpy, times(1)).isDead(); // apparently when() counts as interaction

    // act
    combatEntitySpy.die();

    // assert
    verify(combatEntitySpy, times(2)).isDead();
    verify(combatEntitySpy, times(0)).getHitPoints();
  }

  @Test
  void testDieIsAlive() {
    // arrange
    when(combatEntitySpy.isDead()).thenReturn(false);
    verify(combatEntitySpy, times(1)).isDead(); // apparently when() counts as interaction

    // act
    combatEntitySpy.die();

    // assert
    verify(combatEntitySpy, times(2)).isDead();
    verify(combatEntitySpy, times(1)).getHitPoints();
    verify(combatEntitySpy, times(1)).setCollision(false);
  }

  @Test
  void testGetHitBox() {
    // arrange
    when(combatEntitySpy.getX()).thenReturn(5d);
    when(combatEntitySpy.getY()).thenReturn(10d);
    when(combatEntitySpy.getWidth()).thenReturn(70d);
    when(combatEntitySpy.getHeight()).thenReturn(60d);

    // act
    Shape hitBox = combatEntitySpy.getHitBox();

    // assert
    assertEquals(new Ellipse2D.Double(5d, 10d, 70d, 60d), hitBox);
  }

  @Test
  void testGetTweenValuesHitPoints() {
    // act
    float[] tweenValues = combatEntitySpy.getTweenValues(TweenType.HITPOINTS);

    // assert
    assertEquals(1, tweenValues.length);
    assertEquals(100f, tweenValues[0]); // default
  }

  @Test
  void testGetTweenValuesOtherType() {
    // arrange
    combatEntitySpy.setCollisionBoxHeight(42d);
    combatEntitySpy.setCollisionBoxWidth(42d);

    // act
    float[] tweenValues = combatEntitySpy.getTweenValues(TweenType.COLLISION_BOTH);

    // assert
    assertEquals(2, tweenValues.length);
    assertEquals(42f, tweenValues[0]);
    assertEquals(42f, tweenValues[1]);
  }

  @Test
  void testSetTweenValuesHitPoints() {
    // arrange
    float[] newValues = {42.3f, 1f, 99f};
    RangeAttribute<Integer> hitPointsMock = mock(RangeAttribute.class);
    when(combatEntitySpy.getHitPoints()).thenReturn(hitPointsMock);

    // act
    combatEntitySpy.setTweenValues(TweenType.HITPOINTS, newValues);

    // assert
    verify(combatEntitySpy, times(1)).getHitPoints();
    verify(hitPointsMock, times(1)).setBaseValue(42);
  }

  @Test
  void testSetTweenValuesOtherType() {
    // arrange
    float[] newValues = {42.3f, 1f, 99f};

    // act
    combatEntitySpy.setTweenValues(TweenType.COLLISION_BOTH, newValues);

    // assert
    verify(combatEntitySpy, times(0)).getHitPoints();
    assertEquals(42.3f, combatEntitySpy.getCollisionBoxWidth());
    assertEquals(1f, combatEntitySpy.getCollisionBoxHeight());
  }

  @Test
  void testHitWithoutAbilityDelegates() {
    // act
    combatEntitySpy.hit(9000);

    // assert
    verify(combatEntitySpy, times(1)).hit(9000, null);
  }

  @Test
  void testHitNotDead() {
    // arrange
    int hitPoints = 100;

    // act
    combatEntitySpy.hit(hitPoints, ability);

    // assert
    verify(entityListener, times(2)).hit(any());
  }

  @Test
  void testHitDead() {
    // arrange
    int hitPoints = 100;
    combatEntitySpy.die();

    // act
    combatEntitySpy.hit(hitPoints, ability);

    // assert
    verify(entityListener, times(0)).hit(any());
  }

  @Test
  void testHitIndestructible() {
    // arrange
    int hitPoints = 100;
    combatEntitySpy.setIndestructible(true);

    // act
    combatEntitySpy.hit(hitPoints, ability);

    // assert
    assertEquals(100, combatEntitySpy.getHitPoints().get());
  }

  @Test
  void testHitGetsKilledWithThisHit() {
    // arrange
    int hitPoints = 250;

    // act
    combatEntitySpy.hit(hitPoints, ability);

    // assert
    verify(entityListener, times(2)).death(combatEntitySpy);
  }

  @Test
  void testIsFriendlySameTeam() {
    // arrange
    combatEntitySpy.setTeam(5);
    ICombatEntity friendlyEntity = mock(ICombatEntity.class);
    when(friendlyEntity.getTeam()).thenReturn(5);

    // act, assert
    assertTrue(combatEntitySpy.isFriendly(friendlyEntity));
  }

  @Test
  void testIsFriendlyOtherTeam() {
    // arrange
    combatEntitySpy.setTeam(5);
    ICombatEntity enemyEntity = mock(ICombatEntity.class);
    when(enemyEntity.getTeam()).thenReturn(10);

    // act, assert
    assertFalse(combatEntitySpy.isFriendly(enemyEntity));
  }

  @Test
  void testIsNeutralZeroTeam() {
    // arrange
    combatEntitySpy.setTeam(0); // should be default, making sure

    // act, assert
    assertTrue(combatEntitySpy.isNeutral());
  }

  @Test
  void testIsNeutralOtherTeam() {
    // arrange
    combatEntitySpy.setTeam(5);

    // act, assert
    assertFalse(combatEntitySpy.isNeutral());
  }

  @Test
  void testResurrectDead() {
    // arrange
    CombatEntityListener entityListenerMock = mock(CombatEntityListener.class);
    CombatEntityResurrectListener resurrectListenerMock = mock(CombatEntityResurrectListener.class);
    combatEntitySpy.addCombatEntityListener(entityListenerMock);
    combatEntitySpy.onResurrect(resurrectListenerMock);

    when(combatEntitySpy.isDead()).thenReturn(true);
    verify(combatEntitySpy, times(1)).isDead(); // apparently when() counts as interaction

    // act
    combatEntitySpy.resurrect();

    // assert
    verify(combatEntitySpy, times(2)).isDead();
    verify(combatEntitySpy, times(2)).getHitPoints();
    verify(entityListenerMock, times(1)).resurrect(combatEntitySpy);
    verify(resurrectListenerMock, times(1)).resurrect(combatEntitySpy);
    verify(combatEntitySpy, times(1)).setCollision(true);
  }

  @Test
  void testResurrectAlive() {
    // arrange
    when(combatEntitySpy.isDead()).thenReturn(false);
    verify(combatEntitySpy, times(1)).isDead(); // apparently when() counts as interaction

    // act
    combatEntitySpy.resurrect();

    // assert
    verify(combatEntitySpy, times(2)).isDead();
    verify(combatEntitySpy, times(0)).getHitPoints();
  }

  @Test
  void testGetAndSetTarget() {
    // arrange
    ICombatEntity entityMock = mock(CombatEntity.class);
    assertNull(combatEntitySpy.getTarget());

    // act, assert
    combatEntitySpy.setTarget(entityMock);
    assertEquals(entityMock, combatEntitySpy.getTarget());
  }

  @Test
  void testWasHitInTimespan() {
    // arrange
    GameTime timeMock = mock(GameTime.class);
    when(timeMock.since(anyLong())).thenReturn(500L);
    try (var gameMockedStatic = mockStatic(Game.class)) {
      gameMockedStatic.when(Game::time).thenReturn(timeMock);

      // act, assert
      assertTrue(combatEntitySpy.wasHit(800));
    }
  }

  @Test
  void testWasHitOnTimespan() {
    // arrange
    GameTime timeMock = mock(GameTime.class);
    when(timeMock.since(anyLong())).thenReturn(500L);
    try (var gameMockedStatic = mockStatic(Game.class)) {
      gameMockedStatic.when(Game::time).thenReturn(timeMock);

      // act, assert
      assertFalse(combatEntitySpy.wasHit(500));
    }
  }

  @Test
  void testWasHitOutsideTimespan() {
    // arrange
    GameTime timeMock = mock(GameTime.class);
    when(timeMock.since(anyLong())).thenReturn(500L);
    try (var gameMockedStatic = mockStatic(Game.class)) {
      gameMockedStatic.when(Game::time).thenReturn(timeMock);

      // act, assert
      assertFalse(combatEntitySpy.wasHit(300));
    }
  }
}
