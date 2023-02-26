package de.gurkenlabs.litiengine.environment;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.test.GameTestSuite;

import java.util.Collections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
class EntitySpawnerTests {
  private Environment testEnvironment;
  private Spawnpoint spawnPoint;
  private Creature spawnedCreature;
  private EntitySpawner<Creature> spawner;
  final int spawnAmount = 10;

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
  public void setup() {
    testEnvironment = mock(Environment.class);
    spawnPoint = mock(Spawnpoint.class);
    spawner = new TestCreatureSpawner(spawnPoint, spawnAmount);
    spawnedCreature = new Creature();
  }

  @Test
  void testSpawnNoEnvironment() {
    // arrange
    when(spawnPoint.getEnvironment()).thenReturn(null);

    // act
    spawner.spawnNewEntities();

    // assert
    verify(spawnPoint, times(0)).spawn(any(Creature.class));
  }

  @Test
  void testSpawnEnvironmentNotLoaded() {
    // arrange
    when(testEnvironment.isLoaded()).thenReturn(false);
    when(spawnPoint.getEnvironment()).thenReturn(testEnvironment);

    // act
    spawner.spawnNewEntities();

    // assert
    verify(spawnPoint, times(0)).spawn(any(Creature.class));
  }

  @Test
  void testSpawnSpawnConfiguredAmount() {
    // arrange
    when(testEnvironment.isLoaded()).thenReturn(true);
    when(spawnPoint.getEnvironment()).thenReturn(testEnvironment);

    // act
    spawner.spawnNewEntities();

    // assert
    verify(spawnPoint, times(spawnAmount)).spawn(spawnedCreature);
  }

  private class TestCreatureSpawner extends EntitySpawner<Creature> {
    TestCreatureSpawner(Spawnpoint spawnPoint, int amount) {
      super(Collections.singletonList(spawnPoint), amount, SpawnMode.ONERANDOMSPAWNPOINT);
    }

    @Override
    public Creature createNew() {
      return spawnedCreature;
    }
  }
}
