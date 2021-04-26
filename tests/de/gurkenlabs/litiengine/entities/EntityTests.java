package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
  public void testActions() {
    // arrange
    TestEntity entity = new TestEntity();

    // act
    EntityActionMap actions = entity.actions();

    // assert
    assertTrue(actions.exists("doSomething"));
  }

  @Test
  void testNamedAction() {
    TestEntity entity = new TestEntity();

    assertTrue(entity.actions().exists("myName"));

    EntityAction action = entity.actions().get("myName");

    assertNotNull(action);

    action.perform();

    assertTrue(entity.didNamedAction);
  }

  @Test
  void testCustomAction() {
    TestEntity entity = new TestEntity();
    entity.register("customAction", () -> entity.customActionPerformed = true);

    assertTrue(entity.actions().exists("customAction"));

    entity.perform("customAction");
    assertTrue(entity.customActionPerformed);

    entity.actions().unregister("customAction");

    assertFalse(entity.actions().exists("customAction"));

    assertDoesNotThrow(() -> entity.perform("I don't exist!"));
  }

  @ParameterizedTest
  @MethodSource("getDefaultTags")
  void testDefaultTags(String tag) {
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
  private static class TestEntity extends Entity {
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
