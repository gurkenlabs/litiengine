package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.annotation.Action;

public class EntityTests {

  @Test
  public void testEntityAction() {
    TestEntity entity = new TestEntity();
    
    assertTrue(entity.actions().exists("doSomething"));
    
    EntityAction action = entity.actions().get("doSomething");
    
    assertNotNull(action);
    
    action.perform();
    
    assertTrue(entity.didSomething);
  }

  @Test
  public void testNamedAction() {
    TestEntity entity = new TestEntity();

    assertTrue(entity.actions().exists("myName"));
    
    EntityAction action = entity.actions().get("myName");
    
    assertNotNull(action);
    
    action.perform();
    
    assertTrue(entity.didNamedAction);
  }

  @Test
  public void testCustomAction() {
    Entity entity = new TestEntity();
    entity.register("customAction", () -> {
      return;
    });

    assertTrue(entity.actions().exists("customAction"));

    entity.actions().unregister("customAction");

    assertFalse(entity.actions().exists("customAction"));
  }

  private class TestEntity extends Entity {
    private boolean didSomething;
    private boolean didNamedAction;

    @Action(description = "does something")
    public void doSomething() {
      didSomething = true;
    }

    @Action(name = "myName")
    public void namedAction() {
      didNamedAction = true;
    }
  }
}
