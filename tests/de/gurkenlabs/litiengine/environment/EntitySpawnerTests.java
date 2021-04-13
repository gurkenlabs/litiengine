package de.gurkenlabs.litiengine.environment;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;

public class EntitySpawnerTests {
    private Environment testEnvironment;
    private Spawnpoint spawnPoint;
    private Creature spawnedCreature;
    private EntitySpawner<Creature> spawner;
    final int spawnAmount = 10;

    @BeforeAll
    public static void initGame() {
        // necessary because the environment need access to the game loop and other stuff
        Game.init(Game.COMMADLINE_ARG_NOGUI);
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
    public void testSpawn_noEnvironment() {
        // arrange
        when(spawnPoint.getEnvironment()).thenReturn(null);

        // act
        spawner.spawnNewEntities();

        // assert
        verify(spawnPoint, times(0)).spawn(any(Creature.class));
    }

    @Test
    public void testSpawn_environmentNotLoaded() {
        // arrange
        when(testEnvironment.isLoaded()).thenReturn(false);
        when(spawnPoint.getEnvironment()).thenReturn(testEnvironment);

        // act
        spawner.spawnNewEntities();

        // assert
        verify(spawnPoint, times(0)).spawn(any(Creature.class));
    }

    @Test
    public void testSpawn_spawnConfiguredAmount() {
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
            super(List.of(spawnPoint), amount, SpawnMode.ONERANDOMSPAWNPOINT);
        }

        @Override
        public Creature createNew() {
            return spawnedCreature;
        }
    }
}
