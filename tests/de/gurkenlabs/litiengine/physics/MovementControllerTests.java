package de.gurkenlabs.litiengine.physics;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MovementControllerTests {

    private MovementController controller;
    private IMobileEntity mobileEntity;

    @BeforeEach
    public void setUp(){
        // arrange
        Game.init(Game.COMMADLINE_ARG_NOGUI);
        mobileEntity = new Creature();
        mobileEntity.setDeceleration(20);
        mobileEntity.setAcceleration(42);
        mobileEntity.setVelocity(39);
        controller = new MovementController(mobileEntity);
    }

    @Test
    public void testHandleMovement_dx(){
        // act
        controller.handleMovement();

        // assert
        assertEquals(0, controller.getDx());
    }

    @Test
    public void testHandleMovement_dy(){
        // act
        controller.handleMovement();

        // assert
        assertEquals(0, controller.getDy());
    }

    @Test
    public void testHandleMovement_notAllowed(){
        // arrange
        controller.onMovementCheck(entity -> false);

        // act
        controller.handleMovement();

        // assert
        assertEquals(0, controller.getVelocity());
    }

    @Test
    public void testHandleMovement_velocity(){
        // arrange
        try(MockedStatic<Game> gameMockedStatic = mockStatic(Game.class)){
            IGameLoop mockGameLoop = mock(IGameLoop.class);
            PhysicsEngine physicsEngine = new PhysicsEngine();
            when(mockGameLoop.getDeltaTime()).thenReturn(10l);
            when(mockGameLoop.getTimeScale()).thenReturn(2f);

            gameMockedStatic.when(Game::loop).thenReturn(mockGameLoop);
            gameMockedStatic.when(Game::physics).thenReturn(physicsEngine);

            mobileEntity.setAcceleration(-1000);
            controller.setVelocity(300);

            // act
            controller.handleMovement();

            // assert
            assertEquals(0.764400030374527, controller.getVelocity());
        }
    }
}
