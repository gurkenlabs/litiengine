package de.gurkenlabs.litiengine.environment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Dimension;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.graphics.RenderType;

public class EnvironmentEventTests {
  private IEnvironment testEnvironment;

  @BeforeEach
  public void initEnvironment() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    when(map.getSizeInTiles()).thenReturn(new Dimension(10, 10));
    this.testEnvironment = new Environment(map);
  }

  @Test
  public void testOnInitialized() {
    EnvironmentListener environmentListener = mock(EnvironmentListener.class);
    this.testEnvironment.addListener(environmentListener);

    this.testEnvironment.init();

    verify(environmentListener, times(1)).environmentInitialized(this.testEnvironment);

  }

  @Test
  public void testOnLoaded() {
    EnvironmentListener environmentListener = mock(EnvironmentListener.class);
    this.testEnvironment.addListener(environmentListener);

    this.testEnvironment.load();

    verify(environmentListener, times(1)).environmentLoaded(this.testEnvironment);
  }

  @Test
  public void testOnAdded() {
    ICombatEntity combatEntity = mock(ICombatEntity.class);
    when(combatEntity.getMapId()).thenReturn(123);
    when(combatEntity.getRenderType()).thenReturn(RenderType.NORMAL);

    Consumer<IEntity> addedConsumer = (Consumer<IEntity>) mock(Consumer.class);
    this.testEnvironment.onEntityAdded(addedConsumer);

    this.testEnvironment.add(combatEntity);

    verify(addedConsumer, times(1)).accept(combatEntity);
  }

  @Test
  public void testOnRemoved() {
    ICombatEntity combatEntity = mock(ICombatEntity.class);
    when(combatEntity.getMapId()).thenReturn(123);
    when(combatEntity.getRenderType()).thenReturn(RenderType.NORMAL);

    Consumer<IEntity> removedConsumer = (Consumer<IEntity>) mock(Consumer.class);
    this.testEnvironment.onEntityRemoved(removedConsumer);

    this.testEnvironment.add(combatEntity);

    this.testEnvironment.remove(combatEntity);

    verify(removedConsumer, times(1)).accept(combatEntity);
  }
}
