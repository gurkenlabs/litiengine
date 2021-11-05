package de.gurkenlabs.litiengine.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameLoop;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class MovementControllerTests {

  private MovementController<IMobileEntity> controller;
  private IMobileEntity mobileEntity;

  @BeforeEach
  public void setUp() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    IMobileEntity actualEntity = new Creature();
    mobileEntity = spy(actualEntity);
    mobileEntity.setDeceleration(20);
    mobileEntity.setAcceleration(42);
    mobileEntity.setVelocity(39);
    controller = new MovementController<>(mobileEntity);
  }

  @Test
  void testHandleMovement_dx() {
    // act
    controller.handleMovement();

    // assert
    assertEquals(0, controller.getDx());
  }

  @Test
  void testHandleMovement_dy() {
    // act
    controller.handleMovement();

    // assert
    assertEquals(0, controller.getDy());
  }

  @Test
  void testHandleMovement_notAllowed() {
    // arrange
    controller.onMovementCheck(entity -> false);

    // act
    controller.handleMovement();

    // assert
    assertEquals(0, controller.getVelocity());
  }

  @Test
  void testHandleMovement_velocity() {
    // arrange
    try (MockedStatic<Game> gameMockedStatic = mockStatic(Game.class)) {
      IGameLoop mockGameLoop = mock(IGameLoop.class);
      PhysicsEngine physicsEngine = new PhysicsEngine();
      when(mockGameLoop.getDeltaTime()).thenReturn(10L);
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

  @Test
  void handleForces_disablesAndResetsTurnOnMove() {
    // arrange
    mobileEntity.setTurnOnMove(true);
    assertTrue(mobileEntity.turnOnMove());
    ArgumentCaptor<Boolean> turnOnMoveCaptor = ArgumentCaptor.forClass(Boolean.class);

    Force activeForce = spy(new Force(new Point2D.Double(0, 0), 1, 1));
    controller.apply(activeForce);
    assertEquals(1, controller.getActiveForces().size());

    // act
    controller.update();

    // assert
    verify(mobileEntity, times(3)).setTurnOnMove(turnOnMoveCaptor.capture());
    List<Boolean> turnOnMoveCallArguments = turnOnMoveCaptor.getAllValues();
    assertEquals(
        Arrays.asList(true, false, true), turnOnMoveCallArguments); // first "true" for setup

    assertTrue(mobileEntity.turnOnMove());
  }

  @Test
  void moveEntityByActiveForces_movesEntityToCorrectTarget() {
    // arrange
    // Game environment
    PhysicsEngine physicsEngineMock = mock(PhysicsEngine.class);
    when(physicsEngineMock.move(any(IMobileEntity.class), any(Point2D.class))).thenReturn(true);

    GameLoop loopMock = mock(GameLoop.class);

    MockedStatic<Game> gameMockedStatic = mockStatic(Game.class);
    gameMockedStatic.when(Game::physics).thenReturn(physicsEngineMock);
    gameMockedStatic.when(Game::loop).thenReturn(loopMock);

    // private method return: combineActiveForces()
    MockedStatic<GeometricUtilities> geomUtilsMockedStatic = mockStatic(GeometricUtilities.class);
    when(GeometricUtilities.getDeltaX(anyDouble(), anyDouble())).thenReturn(4d);
    when(GeometricUtilities.getDeltaY(anyDouble(), anyDouble())).thenReturn(3d);

    // test-entity properties
    Force activeForce = spy(new Force(new Point2D.Double(0, 0), 1, 1));
    controller.apply(activeForce);
    assertEquals(1, controller.getActiveForces().size());

    when(mobileEntity.getX()).thenReturn(1d);
    when(mobileEntity.getY()).thenReturn(2d);

    Point2D targetPoint = new Point2D.Double(5, 5);

    // act
    controller.update();

    // assert
    verify(physicsEngineMock, times(1)).move(mobileEntity, targetPoint);
    verify(activeForce, times(0)).end();

    // cleanup
    gameMockedStatic.close();
    geomUtilsMockedStatic.close();
  }

  @Test
  void moveEntityByActiveForces_endsForcesOnFailure() {
    // arrange
    // Game environment
    PhysicsEngine physicsEngineMock = mock(PhysicsEngine.class);
    when(physicsEngineMock.move(any(IMobileEntity.class), any(Point2D.class))).thenReturn(false);

    GameLoop loopMock = mock(GameLoop.class);

    MockedStatic<Game> gameMockedStatic = mockStatic(Game.class);
    gameMockedStatic.when(Game::physics).thenReturn(physicsEngineMock);
    gameMockedStatic.when(Game::loop).thenReturn(loopMock);

    // private method combineActiveForces()
    MockedStatic<GeometricUtilities> geomUtilsMockedStatic = mockStatic(GeometricUtilities.class);

    // test-entity properties
    Force forcePenetrateOnCollision = spy(new Force(new Point2D.Double(0, 0), 3, 1));
    forcePenetrateOnCollision.setCancelOnCollision(false);
    Force forceCancelOnCollision = spy(new Force(new Point2D.Double(0, 0), 1, 1));
    controller.apply(forcePenetrateOnCollision);
    controller.apply(forceCancelOnCollision);
    assertEquals(2, controller.getActiveForces().size());

    // act
    controller.update();

    // assert
    verify(forcePenetrateOnCollision, times(0)).end();
    verify(forceCancelOnCollision, times(1)).end();

    // cleanup
    gameMockedStatic.close();
    geomUtilsMockedStatic.close();
  }
}
