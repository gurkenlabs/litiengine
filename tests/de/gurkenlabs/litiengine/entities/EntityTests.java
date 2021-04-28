package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.entities.behavior.IBehaviorController;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.animation.IEntityAnimationController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.geom.Point2D;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class EntityTests {

    @Test
    public void testOnMessageDelegates() {
        // arrange
        TestEntity entitySpy = spy(new TestEntity());
        EntityMessageListener listenerMock = mock(EntityMessageListener.class);

        // act
        entitySpy.onMessage(listenerMock);

        // assert
        verify(entitySpy, times(1)).onMessage(Entity.ANY_MESSAGE, listenerMock);
    }

    @Test
    public void testGetAngle() {
        // arrange
        TestEntity entity = new TestEntity();

        // act
        double angle = entity.getAngle();

        // assert
        assertEquals(0, angle); // default
    }

    @Test
    public void testSetAngle() {
        // arrange
        TestEntity entity = new TestEntity();
        assertEquals(0, entity.getAngle()); // default

        // act
        entity.setAngle(5);

        // assert
        assertEquals(5, entity.getAngle());
    }

    @Test
    public void testBehavior() {
        // arrange
        TestEntity entitySpy = spy(new TestEntity());

        // act
        entitySpy.behavior();

        // assert
        verify(entitySpy, times(1)).getController(IBehaviorController.class);
    }

    @Test
    public void testGetCenter() {
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
    public void testGetRenderType() {
        // arrange
        TestEntity entity = new TestEntity();

        // act
        RenderType type = entity.getRenderType();

        // assert
        assertEquals(RenderType.NORMAL, type); // default
    }

    @Test
    public void testSetRenderType() {
        // arrange
        TestEntity entity = new TestEntity();
        assertEquals(RenderType.NORMAL, entity.getRenderType()); // default

        // act
        entity.setRenderType(RenderType.SURFACE);

        // assert
        assertEquals(RenderType.SURFACE, entity.getRenderType());
    }

    @Test
    public void testIsVisibleNull() {
        // arrange
        TestEntity entitySpy = spy(new TestEntity());
        when(entitySpy.animations()).thenReturn(null);

        // act, assert
        assertFalse(entitySpy.isVisible());
    }

    @Test
    public void testIsVisibleNotEnabled() {
        // arrange
        TestEntity entitySpy = spy(new TestEntity());
        IEntityAnimationController animationControllerMock = mock(IEntityAnimationController.class);
        when(animationControllerMock.isEnabled()).thenReturn(false);
        when(entitySpy.animations()).thenReturn(animationControllerMock);

        // act, assert
        assertFalse(entitySpy.isVisible());
    }

    @Test
    public void testIsVisibleEnabled() {
        // arrange
        TestEntity entitySpy = spy(new TestEntity());
        IEntityAnimationController animationControllerMock = mock(IEntityAnimationController.class);
        when(animationControllerMock.isEnabled()).thenReturn(true);
        when(entitySpy.animations()).thenReturn(animationControllerMock);

        // act, assert
        assertTrue(entitySpy.isVisible());
    }

    @Test
    public void testSetVisibleAnimationsNull() {
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
    public void testSetVisibleDelegates() {
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
    public void testSendMessage() {
        // arrange
        TestEntity entitySpy = spy(new TestEntity());
        Creature sender = mock(Creature.class); // arbitrary object

        // act
        String response = entitySpy.sendMessage(sender, "cool test message");

        // assert
        assertNull(response);
    }

    @Test
     void testEntityAction() {
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
        entity.register("customAction", () ->
            entity.customActionPerformed = true);

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
