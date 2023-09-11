package de.gurkenlabs.litiengine.abilities;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.abilities.effects.EffectTarget;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.EntityPivotType;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

@ExtendWith(GameTestSuite.class)
class AbilityTests {

  @BeforeEach
  public void init() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @Test
  void isOnCooldownNoCurrentExecution() {
    // arrange
    TestAbility ability = setupAbility();

    ability.setCurrentExecution(null);

    // act
    boolean onCooldown = ability.isOnCooldown();

    // assert
    assertFalse(onCooldown);
  }

  @Test
  void isOnCooldownCurrentExecutionOver() {
    // arrange
    TestAbility ability = setupAbility();

    AbilityExecution abExec = mock(AbilityExecution.class);
    when(abExec.getExecutionTicks()).thenReturn(0L);
    ability.setCurrentExecution(abExec);

    // act
    boolean onCooldown = ability.isOnCooldown();

    // assert
    assertFalse(onCooldown);
  }

  @Test
  void isOnCooldownCooldownOver() {
    // arrange
    TestAbility ability = setupAbility();

    AbilityExecution abExec = mock(AbilityExecution.class);
    when(abExec.getExecutionTicks())
      .thenReturn(Game.time().now() - (ability.getAttributes().cooldown().get() + 1));
    ability.setCurrentExecution(abExec);

    // act
    boolean onCooldown = ability.isOnCooldown();

    // assert
    assertFalse(onCooldown);
  }

  @Test
  void isOnCooldownStillOnCooldown() {
    // arrange
    TestAbility ability = setupAbility();

    AbilityExecution abExec = mock(AbilityExecution.class);
    when(abExec.getExecutionTicks()).thenReturn(1L);
    ability.setCurrentExecution(abExec);

    // act
    boolean onCooldown = ability.isOnCooldown();

    // assert
    assertTrue(onCooldown);
  }

  @Test
  void getRemainingCooldownInSecondsNoCast() {
    // arrange
    TestAbility ability = setupAbility();

    // act
    float actual = ability.getRemainingCooldownInSeconds();

    // assert
    assertEquals(0, actual);
  }

  @Test
  void getRemainingCooldownInSecondsCreatureIsDead() {
    // arrange
    Creature creature = mock(Creature.class);
    when(creature.isDead()).thenReturn(true);
    TestAbility ability = new TestAbility(creature);
    ability.cast();

    // act
    float actual = ability.getRemainingCooldownInSeconds();

    // assert
    assertEquals(0, actual);
  }

  @Test
  void testGetRemainingCooldownInSeconds_returnTime() {
    Creature creature = mock(Creature.class);
    TestAbility ability = new TestAbility(creature);
    ability.canCast();

    AbilityExecution ae = mock(AbilityExecution.class);
    when(ae.getExecutionTicks()).thenReturn(10L);
    ability.setCurrentExecution(ae);

    // act
    float actual = ability.getRemainingCooldownInSeconds();

    // assert
    assertEquals(0.499, actual, 0.0001);
  }

  @Test
  void testGetRemainingCooldownInSeconds_returnZero() {
    Creature creature = mock(Creature.class);
    TestAbility ability = new TestAbility(creature);
    ability.canCast();

    AbilityExecution ae = mock(AbilityExecution.class);
    when(ae.getExecutionTicks()).thenReturn(0L);
    ability.setCurrentExecution(ae);

    // act
    float actual = ability.getRemainingCooldownInSeconds();

    // assert
    assertEquals(0, actual);
  }

  private TestAbility setupAbility() {
    Creature creature = mock(Creature.class);
    return new TestAbility(creature);
  }

  @Test
  void testInitialization() {
    TestAbility ability = new TestAbility(new Creature());

    assertEquals("I do somethin", ability.getName());
    assertEquals("does somethin", ability.getDescription());
    assertEquals(CastType.ONCONFIRM, ability.getCastType());
    assertTrue(ability.isMultiTarget());

    assertEquals(333, ability.getAttributes().cooldown().get().intValue());
    assertEquals(222, ability.getAttributes().duration().get().intValue());
    assertEquals(111, ability.getAttributes().impact().get().intValue());
    assertEquals(99, ability.getAttributes().impactAngle().get().intValue());
    assertEquals(444, ability.getAttributes().range().get().intValue());
    assertEquals(999, ability.getAttributes().value().get().intValue());
  }

  @Test
  void testEffectInitialization() {
    Creature entity = new Creature();
    TestAbility ability = new TestAbility(new Creature());

    Effect effect = new TestEffect(ability, EffectTarget.ENEMY);
    assertEquals(ability.getAttributes().duration().get().intValue(), effect.getDuration());
    assertEquals(ability, effect.getAbility());
    assertEquals(0, effect.getFollowUpEffects().size());
    assertFalse(effect.isActive(entity));
    assertArrayEquals(new EffectTarget[]{EffectTarget.ENEMY}, effect.getEffectTargets());
  }

  @AbilityInfo(
    impact = 150,
    origin = EntityPivotType.DIMENSION_CENTER,
    range = 100)
  private static class TestAbility360 extends Ability {

    protected TestAbility360(Creature executor) {
      super(executor);
    }
  }

  /*
   * Impact Angle = 360
   * Range 100
   * Executor Angle = 90 (EAST)
   *
   * Expect Impact Area to be a circle with diameter 150 and origin at (16+range/2, 16) = (66, 16)
   * i.e. the bounding rectangle should have its upper left corner at (-9.0, -59.0) and sides of length 150
   */
  @Test
  void testInternalCalculateImpactArea360() {
    TestAbility360 ability = new TestAbility360(new Creature());
    Shape s = ability.internalCalculateImpactArea(90);
    Ellipse2D e = (Ellipse2D) s;
    assertEquals(-84.0, e.getX(), 0.001);
    assertEquals(-134.0, e.getY(), 0.001);
    assertEquals(300, e.getHeight(), 0.001);
    assertEquals(300, e.getWidth(), 0.001);
  }

  @AbilityInfo(
    impact = 111,
    impactAngle = 180,
    origin = EntityPivotType.DIMENSION_CENTER,
    range = 150)
  private static class TestAbility180 extends Ability {

    protected TestAbility180(Creature executor) {
      super(executor);
    }
  }

  /*
   * Impact Angle = 180
   * Range = 150
   * Executor Angle = 0 (NORTH)
   *
   * Expect Impact Area to be a 180 deg arc with its origin at (16, 16+range/2) = (16, 91)
   * i.e. the bounding rectangle should have its upper left corner at (-39.5, 35.5) and sides of length 111
   */
  @Test
  void testInternalCalculateImpactArea180() {
    TestAbility180 ability = new TestAbility180(new Creature());
    Shape s = ability.internalCalculateImpactArea(0);
    Arc2D a = (Arc2D) s;
    assertEquals(-95.0, a.getX(), 0.001);
    assertEquals(-20, a.getY(), 0.001);
    assertEquals(222, a.getHeight());
    assertEquals(222, a.getWidth());
    assertEquals(-180, a.getAngleStart(), 0.001);
    assertEquals(180, a.getAngleExtent(), 0.001);
  }

  /*
   * Executor angle = 0
   * Expect same shape from the public and the internal method
   */
  @Test
  void testCalculateImpactArea() {
    Creature c = new Creature();
    TestAbility360 ability = new TestAbility360(c);
    Ellipse2D ePub = (Ellipse2D) ability.calculateImpactArea();
    Ellipse2D eInt = (Ellipse2D) ability.internalCalculateImpactArea(0);
    assertEquals(ePub.getX(), eInt.getX(), 0.001);
    assertEquals(ePub.getY(), eInt.getY(), 0.001);
    assertEquals(ePub.getWidth(), eInt.getWidth(), 0.001);
    assertEquals(ePub.getHeight(), eInt.getHeight(), 0.001);
  }

  /*
   * Executor angle = 45
   * Expect same shape from the public and the internal method
   */
  @Test
  void testCalculateImpactArea45() {
    Creature c = new Creature();
    c.setAngle(45);
    TestAbility360 ability = new TestAbility360(c);
    Ellipse2D ePub = (Ellipse2D) ability.calculateImpactArea();
    Ellipse2D eInt = (Ellipse2D) ability.internalCalculateImpactArea(45);
    assertEquals(ePub.getX(), eInt.getX(), 0.001);
    assertEquals(ePub.getY(), eInt.getY(), 0.001);
    assertEquals(ePub.getWidth(), eInt.getWidth(), 0.001);
    assertEquals(ePub.getHeight(), eInt.getHeight(), 0.001);
  }

  @Test
  void testGetOriginLocation() {
    // arrange
    Creature entity = mock(Creature.class);

    // act
    TestOriginLocation abilityLocation = new TestOriginLocation(entity);

    // assert
    assertEquals(entity, abilityLocation.getPivot().getEntity());
    assertEquals(EntityPivotType.LOCATION, abilityLocation.getPivot().getType());
  }

  @Test
  void testGetOriginCustom() {
    // arrange
    Creature entity = mock(Creature.class);

    // act
    TestOriginCustom abilityLocation = new TestOriginCustom(entity);

    // assert
    assertEquals(entity, abilityLocation.getPivot().getEntity());
    assertEquals(EntityPivotType.OFFSET, abilityLocation.getPivot().getType());
  }

  @Test
  void testGetOriginDimension() {
    // arrange
    Creature entity = mock(Creature.class);

    // act
    TestOriginDimension abilityLocation = new TestOriginDimension(entity);

    // assert
    assertEquals(entity, abilityLocation.getPivot().getEntity());
    assertEquals(EntityPivotType.DIMENSION_CENTER, abilityLocation.getPivot().getType());
  }

  @Test
  void testGetOriginCollisionBox() {
    // arrange
    Creature entity = mock(Creature.class);

    // act
    TestOriginCollisionBox abilityLocation = new TestOriginCollisionBox(entity);

    // assert
    assertEquals(entity, abilityLocation.getPivot().getEntity());
    assertEquals(EntityPivotType.COLLISIONBOX_CENTER, abilityLocation.getPivot().getType());
  }

  @Test
  void testRender() {
    // arrange
    TestAbility ability = new TestAbility(new Creature());
    Graphics2D graphics = mock(Graphics2D.class);
    RenderEngine renderEngine = mock(RenderEngine.class);

    try (MockedStatic<Game> gameMockedStatic = mockStatic(Game.class)) {
      gameMockedStatic.when(Game::graphics).thenReturn(renderEngine);

      // act
      ability.render(graphics);

      // assert
      verify(renderEngine, times(1)).renderShape(
        any(Graphics2D.class),
        any(Shape.class),
        eq(true));
      verify(renderEngine, times(1)).renderOutline(
        any(Graphics2D.class),
        any(Shape.class),
        eq(true));
    }
  }

  @AbilityInfo(
    castType = CastType.ONCONFIRM,
    name = "I do somethin",
    description = "does somethin",
    cooldown = 333,
    duration = 222,
    impact = 111,
    impactAngle = 99,
    multiTarget = true,
    origin = EntityPivotType.COLLISIONBOX_CENTER,
    range = 444,
    value = 999)
  private static class TestAbility extends Ability {

    protected TestAbility(Creature executor) {
      super(executor);
    }
  }

  private static class TestAbilityNoImpact extends Ability {

    protected TestAbilityNoImpact(Creature executor) {
      super(executor);
    }
  }

  private static class TestEffect extends Effect {

    protected TestEffect(Ability ability, EffectTarget... targets) {
      super(ability, targets);
    }
  }

  @AbilityInfo(origin = EntityPivotType.LOCATION)
  private static class TestOriginLocation extends Ability {

    protected TestOriginLocation(Creature executor) {
      super(executor);
    }
  }

  @AbilityInfo(origin = EntityPivotType.OFFSET)
  private static class TestOriginCustom extends Ability {

    protected TestOriginCustom(Creature executor) {
      super(executor);
    }
  }

  @AbilityInfo(origin = EntityPivotType.COLLISIONBOX_CENTER)
  private static class TestOriginCollisionBox extends Ability {

    protected TestOriginCollisionBox(Creature executor) {
      super(executor);
    }
  }

  @AbilityInfo(origin = EntityPivotType.DIMENSION_CENTER)
  private static class TestOriginDimension extends Ability {

    protected TestOriginDimension(Creature executor) {
      super(executor);
    }
  }

  /**
   * Test getPotentialCollisionBox when the collision box of the entity is the zero rectangle and
   * the impact of the of the ability is zero.
   *
   * <p>
   * Expected: potentialImpactArea() is an ellipse with a center in the origin and a zero width and
   * height.
   */
  @Test
  void testGetPotentialCollisionZeroBoxZeroImpact() {
    Rectangle2D collisonBox = new Rectangle2D.Double(0, 0, 0, 0);
    Ellipse2D ellipse = new Ellipse2D.Double(0, 0, 0, 0);

    Creature entity = mock(Creature.class);
    when(entity.getCollisionBox()).thenReturn(collisonBox);

    TestAbilityNoImpact ability = new TestAbilityNoImpact(entity);
    assertEquals(ellipse, ability.calculatePotentialImpactArea());
  }

  /**
   * Test getPotentialCollisionBox when the collision box of the entity is non-zero and the impact
   * of the of the ability is zero.
   *
   * <p>
   * Expected: potentialImpactArea() is an ellipse with a center corresponding to the collisionbox
   * and a zero width and height.
   */
  @Test
  void testGetPotentialCollisionBoxNonZeroBoxZeroImpact() {
    Rectangle2D collisonBox = new Rectangle2D.Double(1, 1, 1, 1);
    Ellipse2D ellipse =
      new Ellipse2D.Double(collisonBox.getCenterX(), collisonBox.getCenterY(), 0, 0);

    Creature entity = mock(Creature.class);
    when(entity.getCollisionBox()).thenReturn(collisonBox);

    TestAbilityNoImpact ability = new TestAbilityNoImpact(entity);
    assertEquals(ellipse, ability.calculatePotentialImpactArea());
  }

  /**
   * Test getPotentialCollisionBox when the collision box of the entity is the zero rectangle, and
   * the impact of the of the ability is non-zero.
   *
   * <p>
   * Expected: potentialImpactArea() is an ellipse with a center shifted by half of the negative
   * impact from the origin, and a width and height corresponding to the impact.
   */
  @Test
  void testGetPotentialCollisionBoxZeroBoxNonZeroImpact() {
    Rectangle2D collisonBox = new Rectangle2D.Double(0, 0, 0, 0);
    Ellipse2D ellipse = new Ellipse2D.Double(0 - 111 * 0.5, 0 - 111 * 0.5, 111, 111);

    Creature entity = mock(Creature.class);
    when(entity.getCollisionBox()).thenReturn(collisonBox);

    TestAbility ability = new TestAbility(entity);
    assertEquals(ellipse, ability.calculatePotentialImpactArea());
  }

  /**
   * Test getPotentialCollisionBox when the collision box of the entity is non-zero, and the impact
   * of the of the ability is non-zero.
   *
   * <p>
   * Expected: potentialImpactArea() is an ellipse with a center corresponding to the collisionbox
   * shifted by half of the negative impact, and a width and height corresponding to the impact.
   */
  @Test
  void testGetPotentialCollisionBoxNonZeroBoxNonZeroImpact() {
    Rectangle2D collisonBox = new Rectangle2D.Double(1, 1, 0, 0);
    Ellipse2D ellipse = new Ellipse2D.Double(1 - 111 * 0.5, 1 - 111 * 0.5, 111, 111);

    Creature entity = mock(Creature.class);
    when(entity.getCollisionBox()).thenReturn(collisonBox);

    TestAbility ability = new TestAbility(entity);
    assertEquals(ellipse, ability.calculatePotentialImpactArea());
  }

  /**
   * If the executor is dead it is not possible to cast Expected: canCast() is false.
   */
  @Test
  void testCanCastWhenDead() {
    Creature entity = mock(Creature.class);
    when(entity.isDead()).thenReturn(true);

    TestAbility a = new TestAbility(entity);
    assertEquals(a.getExecutor(), entity);
    assertTrue(a.getExecutor().isDead());
    assertFalse(a.canCast());
  }

  /**
   * If the executor is alive and the ability has no current execution, it is possible to cast.
   * Expected: canCast() is true.
   */
  @Test
  void testCanCastWhenNoExecution() {
    Creature entity = mock(Creature.class);
    when(entity.isDead()).thenReturn(false);

    TestAbility a = new TestAbility(entity);
    a.setCurrentExecution(null);

    assertEquals(a.getExecutor(), entity);
    assertFalse(a.getExecutor().isDead());
    assertNull(a.getCurrentExecution());
    assertTrue(a.canCast());
  }

  /**
   * If the executor is alive and the execution has no execution ticks left, it is possible to cast.
   * Expected: canCast() is true.
   */
  @Test
  void testCanCastWhenNoExecutionticks() {
    Creature entity = mock(Creature.class);
    when(entity.isDead()).thenReturn(false);

    AbilityExecution ae = mock(AbilityExecution.class);
    when(ae.getExecutionTicks()).thenReturn(0L);

    TestAbility a = new TestAbility(entity);
    a.setCurrentExecution(ae);

    assertEquals(a.getExecutor(), entity);
    assertFalse(a.getExecutor().isDead());
    assertNotNull(a.getCurrentExecution());
    assertEquals(0, a.getCurrentExecution().getExecutionTicks());
    assertTrue(a.canCast());
  }

  @Test
  void testOnEffectApplied() {
    Effect.EffectAppliedListener listener;
    Effect effect;
    Ability ability = new TestAbility(new Creature());
    listener = mock(Effect.EffectAppliedListener.class);
    effect = mock(Effect.class);
    ability.addEffect(effect);
    ability.onEffectApplied(listener);

    verify(listener, times(0)).applied(any());
  }

  @Test
  void testOnEffectCeased() {
    Effect.EffectCeasedListener listener;
    Effect effect;
    Ability ability = new TestAbility(new Creature());
    listener = mock(Effect.EffectCeasedListener.class);
    effect = mock(Effect.class);
    ability.addEffect(effect);
    ability.onEffectCeased(listener);

    verify(listener, times(0)).ceased(any());
  }
}
