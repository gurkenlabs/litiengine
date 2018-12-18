package de.gurkenlabs.litiengine.environment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.resources.Resources;

public class GameWorldTests {

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

  @Test
  public void testListeners() {
    Status mapLoaded = new Status();
    Status mapUnloaded = new Status();
    Status mapInitialized = new Status();
    Status mapCleared = new Status();
    Status map2Loaded = new Status();
    Status map2Initialized = new Status();

    Game.world().addListener("test-map", new EnvironmentAdapter() {
      @Override
      public void initialized(IEnvironment environment) {
        mapInitialized.wasCalled = true;
      }

      @Override
      public void loaded(IEnvironment environment) {
        mapLoaded.wasCalled = true;
      }

      @Override
      public void unloaded(IEnvironment environment) {
        mapUnloaded.wasCalled = true;
      }

      @Override
      public void cleared(IEnvironment environment) {
        mapCleared.wasCalled = true;
      }
    });

    Game.world().addListener("test-mapobject", new EnvironmentAdapter() {

      @Override
      public void initialized(IEnvironment environment) {
        map2Initialized.wasCalled = true;
      }

      @Override
      public void loaded(IEnvironment environment) {
        map2Loaded.wasCalled = true;
      }
    });

    IMap map = Resources.maps().get("tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx");

    IEnvironment env = Game.world().loadEnvironment(map);

    assertTrue(mapLoaded.wasCalled);
    assertTrue(mapInitialized.wasCalled);

    env.clear();

    assertTrue(mapCleared.wasCalled);

    IMap map2 = Resources.maps().get("tests/de/gurkenlabs/litiengine/environment/tilemap/xml/test-mapobject.tmx");

    Game.world().loadEnvironment(map2);

    assertTrue(mapUnloaded.wasCalled);
    assertTrue(map2Loaded.wasCalled);
    assertTrue(map2Initialized.wasCalled);
  }

  private class Status {
    boolean wasCalled = false;
  }
}