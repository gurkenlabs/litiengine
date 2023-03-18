package de.gurkenlabs.litiengine.entities;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EntityControllersTests {
  private EntityControllers controllers;
  private IEntityController controllerA;
  private IEntityController controllerB;

  @BeforeEach
  public void setupWithTwoMockedControllers() {
    controllers = new EntityControllers();
    controllerA = mock(EntityControllerDummyA.class);
    controllerB = mock(EntityControllerDummyB.class);

    final IEntity mockedEntity = mock(IEntity.class);
    when(mockedEntity.isLoaded()).thenReturn(false); // avoid prematurely calling attach()
    when(controllerA.getEntity()).thenReturn(mockedEntity);
    when(controllerB.getEntity()).thenReturn(mockedEntity);

    controllers.addController(controllerA);
    controllers.addController(controllerB);
  }

  @Test
  void detachAll_delegatesCorrectly() {
    // act
    controllers.detachAll();

    // assert
    verify(controllerA, times(1)).detach();
    verify(controllerB, times(1)).detach();
  }

  @Test
  void attachAll_delegatesCorrectly() {
    // act
    controllers.attachAll();

    // assert
    verify(controllerA, times(1)).attach();
    verify(controllerB, times(1)).attach();
  }

  private abstract static class EntityControllerDummyA implements IEntityController {}

  private abstract static class EntityControllerDummyB implements IEntityController {}
}
