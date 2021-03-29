package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

public class EntityTests {

  @Test
  public void testEntityAction() {
    TestEntity entity = new TestEntity();

    assertTrue(entity.actions().exists("doSomething"));
    assertFalse(entity.actions().exists("imNotAnAction"));

    assertEquals(2, entity.actions().getActions().size());

    EntityAction action = entity.actions().get("doSomething");

    assertEquals("doSomething", action.getName());
    assertEquals("does something", action.getDescription());

    assertNotNull(action);

    action.perform();

    assertTrue(entity.didSomething);

    entity.actions().unregister(action);

    assertEquals(1, entity.actions().getActions().size());
    assertFalse(entity.actions().exists("doSomething"));

    entity.actions().register(action);

    assertEquals(2, entity.actions().getActions().size());
    assertTrue(entity.actions().exists("doSomething"));
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
    TestEntity entity = new TestEntity();
    entity.register("customAction", () -> {
      entity.customActionPerformed = true;
      return;
    });

    assertTrue(entity.actions().exists("customAction"));

    entity.perform("customAction");
    assertTrue(entity.customActionPerformed);

    entity.actions().unregister("customAction");

    assertFalse(entity.actions().exists("customAction"));

    assertDoesNotThrow(() -> entity.perform("I don't exist!"));
  }

  @ParameterizedTest
  @MethodSource("getDefaultTags")
  public void testDefaultTags(String tag) {
    TestEntity entity = new TestEntity();
    assertTrue(entity.hasTag(tag));
  }

  private static Stream<Arguments> getDefaultTags() {
    return Stream.of(
            Arguments.of("some tag"),
            Arguments.of("another tag")
    );
  }

  @Tag("some tag")
  @Tag("another tag")
  private class TestEntity extends Entity {
    private boolean didSomething;
    private boolean didNamedAction;
    private boolean customActionPerformed;

    @Action(description = "does something")
    public void doSomething() {
      didSomething = true;
    }

    @Action(name = "myName")
    public void namedAction() {
      didNamedAction = true;
    }

    @SuppressWarnings("unused")
    public void imNotAnAction() {
    }
    
    @Action
    public void imNotParameterless(int something) {
    }
    
    @Action
    private void privateAction() {
    }
  }
}
