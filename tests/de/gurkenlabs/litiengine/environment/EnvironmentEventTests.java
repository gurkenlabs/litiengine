package de.gurkenlabs.litiengine.environment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientation;
import de.gurkenlabs.litiengine.graphics.RenderType;

public class EnvironmentEventTests {
  private IEnvironment testEnvironment;
  
  @BeforeAll
  public static void initGame() {

    // necessary because the environment need access to the game loop and other
    // stuff
    Game.init(Game.COMMADLINE_ARG_NOGUI);
  }

  @AfterAll
  public static void terminateGame() {
    Game.terminate();
  }
  
  @BeforeEach
  public void initEnvironment() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    when(map.getSizeInTiles()).thenReturn(new Dimension(10, 10));
    when(map.getOrientation()).thenReturn(MapOrientation.ORTHOGONAL);
    when(map.getRenderLayers()).thenReturn(new ArrayList<>());
    
    this.testEnvironment = new Environment(map);
  }

  @Test
  public void testOnInitialized() {
    EnvironmentListener environmentListener = mock(EnvironmentListener.class);
    this.testEnvironment.addListener(environmentListener);

    this.testEnvironment.init();

    verify(environmentListener, times(1)).initialized(this.testEnvironment);

  }

  @Test
  public void testOnLoaded() {
    EnvironmentListener environmentListener = mock(EnvironmentListener.class);
    this.testEnvironment.addListener(environmentListener);

    this.testEnvironment.load();

    verify(environmentListener, times(1)).loaded(this.testEnvironment);
  }

  @Test
  public void testOnAdded() {
    ICombatEntity combatEntity = mock(ICombatEntity.class);
    when(combatEntity.getMapId()).thenReturn(123);
    when(combatEntity.getRenderType()).thenReturn(RenderType.NORMAL);

    EnvironmentEntityListener listener = mock(EnvironmentEntityListener.class);
    this.testEnvironment.addEntityListener(listener);

    this.testEnvironment.add(combatEntity);

    verify(listener, times(1)).entityAdded(combatEntity);
  }

  @Test
  public void testOnRemoved() {
    ICombatEntity combatEntity = mock(ICombatEntity.class);
    when(combatEntity.getMapId()).thenReturn(123);
    when(combatEntity.getRenderType()).thenReturn(RenderType.NORMAL);

    EnvironmentEntityListener listener = mock(EnvironmentEntityListener.class);
    this.testEnvironment.addEntityListener(listener);

    this.testEnvironment.add(combatEntity);

    this.testEnvironment.remove(combatEntity);

    verify(listener, times(1)).entityRemoved(combatEntity);
  }

  @ParameterizedTest
  @EnumSource(value = RenderType.class, mode = Mode.EXCLUDE, names = "NONE")
  public void testRenderListener(RenderType renderType) {
    EnvironmentRenderListener listener = mock(EnvironmentRenderListener.class);
    Graphics2D g = mock(Graphics2D.class);
    this.testEnvironment.addRenderListener(renderType, listener);
    this.testEnvironment.render(g);
    verify(listener, times(1)).rendered(g, renderType);
  }
}
