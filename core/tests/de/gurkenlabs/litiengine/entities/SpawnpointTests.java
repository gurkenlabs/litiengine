package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import java.awt.Dimension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpawnpointTests {
  private Environment testEnvironment;
  private boolean eventCalled;

  @BeforeAll
  public static void initGame() {
    // necessary because the environment need access to the game loop and other stuff
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @AfterAll
  public static void terminateGame() {
    GameTest.terminateGame();
  }

  @BeforeEach
  public void initEnvironment() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    when(map.getSizeInTiles()).thenReturn(new Dimension(10, 10));
    this.testEnvironment = new Environment(map);
    this.testEnvironment.init();
    eventCalled = false;
  }

  @Test
  void testSpawn() {
    Creature creature = new Creature();

    Spawnpoint spawn1 = new Spawnpoint();
    Spawnpoint spawn2 = new Spawnpoint(20, 20);
    Spawnpoint spawn3 = new Spawnpoint(123, 30, 30);
    Spawnpoint spawn4 = new Spawnpoint(124, 40, 40, Direction.RIGHT);
    Spawnpoint spawn5 = new Spawnpoint(Direction.LEFT);
    Spawnpoint spawn6 = new Spawnpoint(Direction.LEFT, "some type");
    spawn6.setLocation(60, 60);

    // spawning is not possible if the environment is not loaded yet
    assertFalse(spawn1.spawn(creature));

    this.testEnvironment.add(spawn1);
    this.testEnvironment.add(spawn2);
    this.testEnvironment.add(spawn3);
    this.testEnvironment.add(spawn4);
    this.testEnvironment.add(spawn5);
    this.testEnvironment.add(spawn6);

    Game.world().loadEnvironment(this.testEnvironment);

    assertTrue(spawn1.spawn(creature));
    assertTrue(this.testEnvironment.contains(creature));

    assertTrue(spawn2.spawn(creature));
    assertTrue(spawn3.spawn(creature));

    assertEquals(30, creature.getX());
    assertEquals(30, creature.getY());

    assertTrue(spawn4.spawn(creature));

    assertEquals(Direction.RIGHT, Direction.fromAngle(creature.getAngle()));

    assertTrue(spawn5.spawn(creature));

    assertEquals(Direction.LEFT, Direction.fromAngle(creature.getAngle()));

    assertTrue(spawn6.spawn(creature));

    assertEquals(60, creature.getX());
    assertEquals(60, creature.getY());
  }

  @Test
  void testSpawnEvent() {
    // arrange
    Creature creature = new Creature();
    Spawnpoint spawn = new Spawnpoint(20, 20);

    this.testEnvironment.add(spawn);
    Game.world().loadEnvironment(this.testEnvironment);

    spawn.onSpawned(e -> eventCalled = true);

    // act, assert
    assertTrue(spawn.spawn(creature));
    assertTrue(eventCalled);
  }
}
