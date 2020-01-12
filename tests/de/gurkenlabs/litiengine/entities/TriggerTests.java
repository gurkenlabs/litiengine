package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.physics.Collision;

public class TriggerTests {
  private Environment testEnvironment;

  @BeforeAll
  public static void initGame() {

    // necessary because the environment need access to the game loop and other
    // stuff
    Game.init(Game.COMMADLINE_ARG_NOGUI);
  }

  @AfterAll
  public static void terminateGame() {
    GameTest.resetGame();
  }

  @BeforeEach
  public void initEnvironment() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    when(map.getSizeInTiles()).thenReturn(new Dimension(10, 10));
    when(map.getOrientation()).thenReturn(MapOrientations.ORTHOGONAL);
    when(map.getRenderLayers()).thenReturn(new ArrayList<>());

    this.testEnvironment = new Environment(map);
  }

  @Test
  public void testInteractTrigger() {
    Trigger trigger = new Trigger(TriggerActivation.INTERACT, "testrigger", "testmessage");
    this.testEnvironment.add(trigger);

    IEntity target = this.mockEntity(456);
    trigger.addTarget(target.getMapId());

    Game.world().loadEnvironment(this.testEnvironment);

    assertFalse(trigger.isActivated());

    // only entity instances can interact with a trigger
    assertFalse(trigger.interact(null));
    assertFalse(trigger.isActivated());

    assertTrue(trigger.interact(this.mockEntity(123)));
    assertTrue(trigger.isActivated());
    verify(target, times(1)).sendMessage(trigger, trigger.getMessage());
  }

  @Test
  public void onlyActivatorsCanTrigger() {
    Trigger trigger = new Trigger(TriggerActivation.INTERACT, "testrigger", "testmessage");
    trigger.getActivators().add(111);
    this.testEnvironment.add(trigger);

    Game.world().loadEnvironment(this.testEnvironment);

    // only the 111 entity can interact with the trigger
    assertFalse(trigger.interact(mockEntity(222)));
    assertTrue(trigger.interact(mockEntity(111)));
  }

  @Test
  public void testOneTimeTrigger() {
    Trigger trigger = new Trigger(TriggerActivation.INTERACT, "testrigger", "testmessage", true);
    this.testEnvironment.add(trigger);

    Game.world().loadEnvironment(this.testEnvironment);
    assertTrue(trigger.interact(mockEntity(111)));
    assertFalse(trigger.interact(mockEntity(111)));
  }

  @Test
  public void testMultipleInteractions() {
    Trigger trigger = new Trigger(TriggerActivation.INTERACT, "testrigger", "testmessage");
    this.testEnvironment.add(trigger);

    Game.world().loadEnvironment(this.testEnvironment);
    assertTrue(trigger.interact(mockEntity(111)));
    assertTrue(trigger.interact(mockEntity(111)));
    assertTrue(trigger.interact(mockEntity(111)));
  }

  @Test
  public void testInteractPredicate() {
    Trigger trigger = new Trigger(TriggerActivation.INTERACT, "testrigger", "testmessage", true);
    trigger.addActivatingCondition(e -> {
      return "You shall not pass!";
    });
    this.testEnvironment.add(trigger);

    IEntity activator = mockEntity(111);
    Game.world().loadEnvironment(this.testEnvironment);
    assertFalse(trigger.interact(activator));

    // make sure the activator receives the reason why the trigger cannot be executed
    verify(activator, times(1)).sendMessage(trigger, "You shall not pass!");
  }

  @Test
  public void testCollisionTriggerActivates() {
    Trigger trigger = new Trigger(TriggerActivation.COLLISION, "testrigger", "testmessage");
    
    // collision box width == width for triggers
    trigger.setSize(16, 16);
    trigger.setLocation(0, 0);
    this.testEnvironment.add(trigger);
    Game.world().loadEnvironment(this.testEnvironment);
    
    // collisionentity that is colliding with the trigger
    mockCollisionEntity(111, 8, 8);
    
    trigger.update();
    
    assertTrue(trigger.isActivated());
  }
  
  @Test
  public void testCollisionTriggerDoesntActivate() {
    Trigger trigger = new Trigger(TriggerActivation.COLLISION, "testrigger", "testmessage");
    
    // collision box width == width for triggers
    trigger.setSize(16, 16);
    trigger.setLocation(0, 0);
    this.testEnvironment.add(trigger);
    Game.world().loadEnvironment(this.testEnvironment);
    
    // collisionentity that is colliding with the trigger
    mockCollisionEntity(111, 16, 16);
    
    trigger.update();
    
    assertFalse(trigger.isActivated());
  }

  private IEntity mockEntity(int id) {
    IEntity entity = mock(IEntity.class);
    when(entity.getMapId()).thenReturn(id);
    when(entity.getRenderType()).thenReturn(RenderType.NONE);
    when(entity.sendMessage(any(Object.class), any(String.class))).thenReturn("answer");

    this.testEnvironment.add(entity);

    return entity;
  }
  
  private ICollisionEntity mockCollisionEntity(int id, int x, int y) {
    ICollisionEntity entity = mock(ICollisionEntity.class);
    when(entity.getMapId()).thenReturn(id);
    when(entity.getRenderType()).thenReturn(RenderType.NONE);
    when(entity.sendMessage(any(Object.class), any(String.class))).thenReturn("answer");
    
    when(entity.getCollisionBox()).thenReturn(new Rectangle2D.Double(x, y, 8, 8));
    when(entity.hasCollision()).thenReturn(true);
    when(entity.getCollisionType()).thenReturn(Collision.DYNAMIC);

    this.testEnvironment.add(entity);

    return entity;
  }
}
