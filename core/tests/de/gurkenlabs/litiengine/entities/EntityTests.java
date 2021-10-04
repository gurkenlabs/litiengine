package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.behavior.IBehaviorController;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

class EntityTests {

  @Test
  void testSetX() {
    // arrange
    TestEntity entity = new TestEntity();
    assertEquals(0, entity.getX());

    // act
    entity.setX(5);

    // assert
    assertEquals(5, entity.getX());
  }

  @Test
  void testSetY() {
    // arrange
    TestEntity entity = new TestEntity();
    assertEquals(0, entity.getY());

    // act
    entity.setY(5);

    // assert
    assertEquals(5, entity.getY());
  }

  @Test
  void testHasTag() {
    // arrange
    String tag = "test tag";
    TestEntity entity = new TestEntity();
    entity.addTag(tag);

    // act, assert
    assertTrue(entity.hasTag(tag));
  }

  @Test
  void testAddTagNotContained() {
    // arrange
    String tag = "test tag";
    TestEntity entity = new TestEntity();
    assertFalse(entity.hasTag(tag));

    // act
    entity.addTag(tag);

    // assert
    assertTrue(entity.hasTag(tag));
  }

  @Test
  void testAddTagContained() {
    // arrange
    String tag = "test tag";
    List<String> tagListSpy = spy(Collections.singletonList(tag));
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.getTags()).thenReturn(tagListSpy);

    // act
    entitySpy.addTag(tag);

    // assert
    verify(tagListSpy, times(1)).contains(tag);
    verify(tagListSpy, times(0)).add(tag);
  }

  @Test
  void testAddTagEnvironmentNull() {
    // arrange
    String tag = "test tag";
    TestEntity entitySpy = spy(new TestEntity());
    Environment envMock = mock(Environment.class);
    entitySpy.loaded(envMock);
    when(entitySpy.getEnvironment()).thenReturn(null);

    // act
    entitySpy.addTag(tag);

    // assert
    verify(envMock, times(0)).getEntitiesByTag();
  }

  @Test
  void testAddTagEnvironmentValid() {
    // arrange
    String tag = "test tag";
    TestEntity entitySpy = spy(new TestEntity());
    Environment envMock = mock(Environment.class);
    when(entitySpy.getEnvironment()).thenReturn(envMock);

    // act
    entitySpy.addTag(tag);

    // assert
    verify(envMock, times(1)).getEntitiesByTag();
  }

  @Test
  void testRemoveTagGameEnvironmentNull() {
    // arrange
    String tag = "test tag";
    List<String> tagListSpy = spy(new ArrayList<>());
    tagListSpy.add(tag);
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.getTags()).thenReturn(tagListSpy);

    MockedStatic<Game> gameMockedStatic = mockStatic(Game.class);
    GameWorld gameWorldMock = mock(GameWorld.class);
    when(gameWorldMock.environment()).thenReturn(null);
    gameMockedStatic.when(Game::world).thenReturn(gameWorldMock);

    // act
    entitySpy.removeTag(tag);

    // assert
    verify(tagListSpy, times(1)).remove(tag);
    verify(entitySpy, times(0)).getEnvironment();

    // cleanup
    gameMockedStatic.close();
  }

  @Test
  void testRemoveTagEmpty() {
    // arrange
    String tag = "test tag";
    List<String> tagListSpy = spy(new ArrayList<>());
    tagListSpy.add(tag);
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.getTags()).thenReturn(tagListSpy);

    Environment environmentMock = mock(Environment.class);
    Map<String, Collection<IEntity>> entities = new HashMap<>();
    List<IEntity> entitiesContent = new ArrayList<>();
    entitiesContent.add(entitySpy);
    entities.put(tag, entitiesContent);
    when(environmentMock.getEntitiesByTag()).thenReturn(entities);
    when(entitySpy.getEnvironment()).thenReturn(environmentMock);

    MockedStatic<Game> gameMockedStatic = mockStatic(Game.class);
    GameWorld gameWorldMock = mock(GameWorld.class);
    when(gameWorldMock.environment()).thenReturn(mock(Environment.class));
    gameMockedStatic.when(Game::world).thenReturn(gameWorldMock);

    assertEquals(1, entities.size());

    // act
    entitySpy.removeTag(tag);

    // assert
    verify(tagListSpy, times(1)).remove(tag);
    assertEquals(0, entities.size());

    // cleanup
    gameMockedStatic.close();
  }

  @ParameterizedTest(name = "testToString name is {0}")
  @MethodSource("getToStringParameters")
  void testToString(
      String testName, int mapId, String name, int getNameInvocations, String expectedName) {
    // arrange
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.getMapId()).thenReturn(mapId);
    when(entitySpy.getName()).thenReturn(name);

    // act
    String result = entitySpy.toString();

    // assert
    verify(entitySpy, times(getNameInvocations)).getName();
    assertEquals(expectedName, result);
  }

  @Test
  void testLoaded() {
    // arrange
    TestEntity entity = new TestEntity();

    EntityListener listenerMock = mock(EntityListener.class);
    entity.addListener(listenerMock);

    Environment envMock = mock(Environment.class);

    assertNull(entity.getEnvironment());
    assertFalse(entity.isLoaded());

    // act
    entity.loaded(envMock);

    // act, assert
    assertEquals(envMock, entity.getEnvironment());
    assertTrue(entity.isLoaded());
    verify(listenerMock, times(1)).loaded(entity, envMock);
  }

  @Test
  void testRemoved() {
    // arrange
    TestEntity entity = new TestEntity();

    Environment envMock = mock(Environment.class);
    entity.loaded(envMock);

    EntityListener listenerMock = mock(EntityListener.class);
    entity.addListener(listenerMock);

    assertEquals(envMock, entity.getEnvironment());
    assertTrue(entity.isLoaded());

    // act
    entity.removed(envMock);

    // act, assert
    assertNull(entity.getEnvironment());
    assertFalse(entity.isLoaded());
    verify(listenerMock, times(1)).removed(entity, envMock);
  }

  @Test
  void testOnMessageDelegates() {
    // arrange
    TestEntity entitySpy = spy(new TestEntity());
    EntityMessageListener listenerMock = mock(EntityMessageListener.class);

    // act
    entitySpy.onMessage(listenerMock);

    // assert
    verify(entitySpy, times(1)).onMessage(Entity.ANY_MESSAGE, listenerMock);
  }

  @Test
  void testGetAngle() {
    // arrange
    TestEntity entity = new TestEntity();

    // act
    double angle = entity.getAngle();

    // assert
    assertEquals(0, angle); // default
  }

  @Test
  void testSetAngle() {
    // arrange
    TestEntity entity = new TestEntity();
    assertEquals(0, entity.getAngle()); // default

    // act
    entity.setAngle(5);

    // assert
    assertEquals(5, entity.getAngle());
  }

  @Test
  void testBehavior() {
    // arrange
    TestEntity entitySpy = spy(new TestEntity());

    // act
    entitySpy.behavior();

    // assert
    verify(entitySpy, times(1)).getController(IBehaviorController.class);
  }

  @Test
  void testGetCenter() {
    // arrange
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.getX()).thenReturn(5d);
    when(entitySpy.getWidth()).thenReturn(10d); // 5 + 10*0.5 = 10
    when(entitySpy.getY()).thenReturn(15d);
    when(entitySpy.getHeight()).thenReturn(20d); // 15 + 20*0.5 = 25

    // act
    Point2D center = entitySpy.getCenter();

    // assert
    assertEquals(new Point2D.Double(10d, 25d), center);
  }

  @Test
  void testGetRenderType() {
    // arrange
    TestEntity entity = new TestEntity();

    // act
    RenderType type = entity.getRenderType();

    // assert
    assertEquals(RenderType.NORMAL, type); // default
  }

  @Test
  void testSetRenderType() {
    // arrange
    TestEntity entity = new TestEntity();
    assertEquals(RenderType.NORMAL, entity.getRenderType()); // default

    // act
    entity.setRenderType(RenderType.SURFACE);

    // assert
    assertEquals(RenderType.SURFACE, entity.getRenderType());
  }

  @Test
  void testIsVisibleNull() {
    // arrange
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.animations()).thenReturn(null);

    // act, assert
    assertFalse(entitySpy.isVisible());
  }

  @Test
  void testIsVisibleNotEnabled() {
    // arrange
    TestEntity entitySpy = spy(new TestEntity());
    IEntityAnimationController animationControllerMock = mock(IEntityAnimationController.class);
    when(animationControllerMock.isEnabled()).thenReturn(false);
    when(entitySpy.animations()).thenReturn(animationControllerMock);

    // act, assert
    assertFalse(entitySpy.isVisible());
  }

  @Test
  void testIsVisibleEnabled() {
    // arrange
    TestEntity entitySpy = spy(new TestEntity());
    IEntityAnimationController animationControllerMock = mock(IEntityAnimationController.class);
    when(animationControllerMock.isEnabled()).thenReturn(true);
    when(entitySpy.animations()).thenReturn(animationControllerMock);

    // act, assert
    assertTrue(entitySpy.isVisible());
  }

  @Test
  void testSetVisibleAnimationsNull() {
    // arrange
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.animations()).thenReturn(null);

    // act
    entitySpy.setVisible(true);

    // assert
    verify(entitySpy, times(1)).setVisible(true); // obvious, but would trigger noMoreInteractions
    verify(entitySpy, times(1)).animations();
    verifyNoMoreInteractions(entitySpy);
  }

  @Test
  void testSetVisibleDelegates() {
    // arrange
    TestEntity entitySpy = spy(new TestEntity());
    IEntityAnimationController animationControllerMock = mock(IEntityAnimationController.class);
    when(entitySpy.animations()).thenReturn(animationControllerMock);

    // act
    entitySpy.setVisible(true);

    // assert
    verify(animationControllerMock, times(1)).setEnabled(true);
  }

  @Test
  void testSendMessage() {
    // arrange
    TestEntity entitySpy = new TestEntity();
    Creature sender = mock(Creature.class); // arbitrary object

    // act
    String response = entitySpy.sendMessage(sender, "cool test message");

    // assert
    assertNull(response);
  }

  @Test
  void testActions() {
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
    return Stream.of(Arguments.of("some tag"), Arguments.of("another tag"));
  }

  private static Stream<Arguments> getToStringParameters() {
    return Stream.of(
        Arguments.of(
            "null", 5, null, 1, "#5: TestEntity"), // second in if is dropped when first is false
        Arguments.of("empty", 5, "", 2, "#5: TestEntity"),
        Arguments.of("valid", 5, "test name", 3, "#5: test name"));
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
    public void imNotAnAction() {}

    @Action
    public void imNotParameterless(int something) {}

    @Action
    private void privateAction() {}
  }
}
