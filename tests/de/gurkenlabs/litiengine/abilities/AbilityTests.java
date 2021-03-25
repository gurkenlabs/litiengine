package de.gurkenlabs.litiengine.abilities;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.abilities.effects.EffectTarget;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.EntityPivotType;

class AbilityTests {
  @BeforeEach
  public void init() {
    Game.init(Game.COMMADLINE_ARG_NOGUI);
  }

  @Test
  public void isOnCooldown_noCurrentExecution() {
    // arrange
    TestAbility ability = setupAbility();

    ability.setCurrentExecution(null);

    // act
    boolean onCooldown = ability.isOnCooldown();

    // assert
    assertFalse(onCooldown);
  }

  @Test
  public void isOnCooldown_currentExecutionOver() {
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
  public void isOnCooldown_cooldownOver() {
    // arrange
    TestAbility ability = setupAbility();

    AbilityExecution abExec = mock(AbilityExecution.class);
    when(abExec.getExecutionTicks()).thenReturn(Game.time().now() - (ability.getAttributes().cooldown().get() + 1));
    ability.setCurrentExecution(abExec);

    // act
    boolean onCooldown = ability.isOnCooldown();

    // assert
    assertFalse(onCooldown);
  }

  @Test
  public void isOnCooldown_stillOnCooldown() {
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
  void getRemainingCooldownInSeconds_NoCast() {
    // arrange
    TestAbility ability = setupAbility();

    // act
    float actual = ability.getRemainingCooldownInSeconds();

    // assert
    assertEquals(0, actual);
  }

  @Test
  void getRemainingCooldownInSeconds_CreatureIsDead() {
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
    assertEquals(true, ability.isMultiTarget());

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
    assertArrayEquals(new EffectTarget[] { EffectTarget.ENEMY }, effect.getEffectTargets());
  }

  @AbilityInfo(impact = 150, impactAngle = 360, origin = EntityPivotType.DIMENSION_CENTER, range = 100)
  private class TestAbility360 extends Ability {

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
    assertTrue(e instanceof Ellipse2D);
    assertEquals(-9.0, e.getX(), 0.001);
    assertEquals(-59.0, e.getY(), 0.001);
    assertEquals(150, e.getHeight(), 0.001);
    assertEquals(150, e.getWidth(), 0.001);

  }

  @AbilityInfo(impact = 111, impactAngle = 180, origin = EntityPivotType.DIMENSION_CENTER, range = 150)
  private class TestAbility180 extends Ability {

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
    assertTrue(a instanceof Arc2D);
    assertEquals(-39.5, a.getX(), 0.001);
    assertEquals(35.5, a.getY(), 0.001);
    assertEquals(111, a.getHeight(), 0.001);
    assertEquals(111, a.getWidth(), 0.001);
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

  /*
   * Test the function getOrigin() with valid inputs when AbilityOrigin = LOCATION.
   */
  @Test
  void testGetOriginLocation() {
    Point2D point1 = new Point2D.Double(0, 0);
    Point2D point2 = new Point2D.Double(1, 1);

    /*
     * If AbilityOrigin = LOCATION; (default) mapLocation = Point2D.Double(0,0)
     * --> return Point2D.Double(0,0)
     */
    Creature entity = mock(Creature.class);
    when(entity.getLocation()).thenReturn(point1);

    TestOriginLocation abilityLocation = new TestOriginLocation(entity);
    assertEquals(point1, abilityLocation.getPivot().getPoint());

    /*
     * If AbilityOrigin = LOCATION; mapLocation = Point2D.Double(1,1)
     * --> return Point2D.Double(1,1)
     */
    when(entity.getLocation()).thenReturn(point2);
    when(entity.getX()).thenReturn((double) 1);
    when(entity.getY()).thenReturn((double) 1);
    assertEquals(point2, abilityLocation.getPivot().getPoint());
  }

  /*
   * Test the function getOrigin() with valid inputs when AbilityOrigin = CUSTOM.
   */
  @Test
  void testGetOriginCustom() {
    Point2D point2 = new Point2D.Double(1, 1);
    Point2D point3 = new Point2D.Double(2, 2);

    /*
     * If AbilityOrigin = CUSTOM; origin = null; (default) mapLocation = Point2D.Double(0,0)
     * --> return Point2D.Double(0,0)
     */
    Creature entity = mock(Creature.class);
    when(entity.getLocation()).thenReturn(point2);
    when(entity.getX()).thenReturn((double) 1);
    when(entity.getY()).thenReturn((double) 1);

    TestOriginCustom abilityCustom = new TestOriginCustom(entity);
    assertEquals(point2, abilityCustom.getPivot().getPoint());

    /*
     * If AbilityOrigin = CUSTOM; origin = Point2D.Double(1,1); mapLocation = Point2D.Double(1,1)
     * --> return Point2D.Double(2,2).
     */
    abilityCustom.getPivot().setOffset(point2);
    assertEquals(point3, abilityCustom.getPivot().getPoint());
  }

  /*
   * Test the function getOrigin() with valid inputs when AbilityOrigin = DIMENSION_CENTER.
   */
  @Test
  void testGetOriginDimension() {
    Point2D point1 = new Point2D.Double(16, 16);

    /*
     * If AbilityOrigin = DIMENSION_CENTER; (default) mapLocation = Point2D.Double(0,0); (default) height = 32; (default) width = 32
     * --> return Point2D.Double(16,16)
     */

    Creature entity = mock(Creature.class);
    when(entity.getCenter()).thenReturn(point1);

    TestOriginDimension abilityDimension = new TestOriginDimension(entity);
    assertEquals(point1, abilityDimension.getPivot().getPoint());
  }

  /*
   * Test the function getOrigin() with valid inputs when AbilityOrigin = COLLISIONBOX_CENTER.
   */

  @Test
  void testGetOriginCollisionBox() {
    Point2D point1 = new Point2D.Double(16, 25.6);
    Rectangle2D shape1 = new Rectangle2D.Double(9.6, 19.2, 12.8, 12.8);

    /*
     * If AbilityOrigin = COLLISIONBOX_CENTER; (default) mapLocation = Point2D.Double(0,0); (default) height = 32; (default) width = 32;
     * (default) Valign = DOWN; (default) Align = CENTER; (default) collisionBoxHeight = -1; (default) collisionBoxwidth = -1
     * --> return Point2D(16, 25.6)
     */

    Creature entity = mock(Creature.class);
    when(entity.getCollisionBox()).thenReturn(shape1);

    TestOriginCollisionBox abilityCollision = new TestOriginCollisionBox(entity);
    assertEquals(point1, abilityCollision.getPivot().getPoint());

  }

  @AbilityInfo(castType = CastType.ONCONFIRM, name = "I do somethin", description = "does somethin", cooldown = 333, duration = 222, impact = 111, impactAngle = 99, multiTarget = true, origin = EntityPivotType.COLLISIONBOX_CENTER, range = 444, value = 999)
  private class TestAbility extends Ability {

    protected TestAbility(Creature executor) {
      super(executor);
    }
  }

  @AbilityInfo(impact = 0)
  private class TestAbilityNoImpact extends Ability {

    protected TestAbilityNoImpact(Creature executor) {
      super(executor);
    }
  }

  private class TestEffect extends Effect {
    protected TestEffect(Ability ability, EffectTarget... targets) {
      super(ability, targets);
    }
  }

  @AbilityInfo(origin = EntityPivotType.LOCATION)
  private class TestOriginLocation extends Ability {
    protected TestOriginLocation(Creature executor) {
      super(executor);
    }
  }

  @AbilityInfo(origin = EntityPivotType.OFFSET)
  private class TestOriginCustom extends Ability {
    protected TestOriginCustom(Creature executor) {
      super(executor);
    }
  }

  @AbilityInfo(origin = EntityPivotType.COLLISIONBOX_CENTER)
  private class TestOriginCollisionBox extends Ability {
    protected TestOriginCollisionBox(Creature executor) {
      super(executor);
    }
  }

  @AbilityInfo(origin = EntityPivotType.DIMENSION_CENTER)
  private class TestOriginDimension extends Ability {
    protected TestOriginDimension(Creature executor) {
      super(executor);
    }
  }

  /**
   * Test getPotentialCollisionBox when the collision box of the entity is the zero rectangle
   * and the impact of the of the ability is zero.
   * 
   * Expected: potentialImpactArea() is an ellipse with a center in the origin
   * and a zero width and height.
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
   * Test getPotentialCollisionBox when the collision box of the entity is non-zero
   * and the impact of the of the ability is zero.
   * 
   * Expected: potentialImpactArea() is an ellipse with a center
   * corresponding to the collisionbox and a zero width and height.
   */
  @Test
  void testGetPotentialCollisionBoxNonZeroBoxZeroImpact() {
    Rectangle2D collisonBox = new Rectangle2D.Double(1, 1, 1, 1);
    Ellipse2D ellipse = new Ellipse2D.Double(collisonBox.getCenterX(), collisonBox.getCenterY(), 0, 0);

    Creature entity = mock(Creature.class);
    when(entity.getCollisionBox()).thenReturn(collisonBox);

    TestAbilityNoImpact ability = new TestAbilityNoImpact(entity);
    assertEquals(ellipse, ability.calculatePotentialImpactArea());
  }

  /**
   * Test getPotentialCollisionBox when the collision box of the entity is the zero rectangle,
   * and the impact of the of the ability is non-zero.
   * 
   * Expected: potentialImpactArea() is an ellipse with a center
   * shifted by half of the negative impact from the origin,
   * and a width and height corresponding to the impact.
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
   * Test getPotentialCollisionBox when the collision box of the entity is non-zero,
   * and the impact of the of the ability is non-zero.
   * 
   * Expected: potentialImpactArea() is an ellipse with a center
   * corresponding to the collisionbox shifted by half of the negative impact,
   * and a width and height corresponding to the impact.
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
   * If the executor is dead it is not possible to cast
   * Expected: canCast() is false.
   */
  @Test
  void testCanCastWhenDead() {
    Creature entity = mock(Creature.class);
    when(entity.isDead()).thenReturn(true);

    TestAbility a = new TestAbility(entity);
    assertEquals(a.getExecutor(), entity);
    assertFalse(!a.getExecutor().isDead());
    assertFalse(a.canCast());
  }

  /**
   * If the executor is alive and the ability has no current execution,
   * it is possible to cast.
   * Expected: canCast() is true.
   */
  @Test
  void testCanCastWhenNoExecution() {
    Creature entity = mock(Creature.class);
    when(entity.isDead()).thenReturn(false);

    TestAbility a = new TestAbility(entity);
    a.setCurrentExecution(null);

    assertEquals(a.getExecutor(), entity);
    assertTrue(!a.getExecutor().isDead());
    assertNull(a.getCurrentExecution());
    assertTrue(a.canCast());
  }

  /**
   * If the executor is alive and the execution has no execution ticks left,
   * it is possible to cast.
   * Expected: canCast() is true.
   */
  @Test
  void testCanCastWhenNoExecutionticks() {
    Creature entity = mock(Creature.class);
    when(entity.isDead()).thenReturn(false);

    AbilityExecution ae = mock(AbilityExecution.class);
    when(ae.getExecutionTicks()).thenReturn(0l);

    TestAbility a = new TestAbility(entity);
    a.setCurrentExecution(ae);

    assertEquals(a.getExecutor(), entity);
    assertTrue(!a.getExecutor().isDead());
    assertNotNull(a.getCurrentExecution());
    assertEquals(0, a.getCurrentExecution().getExecutionTicks());
    assertTrue(a.canCast());
  }
}
