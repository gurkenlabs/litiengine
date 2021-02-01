package com.litiengine.environment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.litiengine.Game;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.litiengine.GameTest;
import com.litiengine.environment.tilemap.IMap;
import com.litiengine.resources.Resources;

public class GameWorldTests {

  @BeforeAll
  public static void initGame() {

    // necessary because the environment need access to the game loop and other
    // stuff
    Game.init(Game.COMMADLINE_ARG_NOGUI);
  }

  @AfterAll
  public static void terminateGame() {
    GameTest.resetGame();
  }

  @Test
  public void testListeners() {
    Status mapLoaded = new Status();
    Status mapUnloaded = new Status();
    Status mapInitialized = new Status();
    Status mapCleared = new Status();
    Status map2Loaded = new Status();
    Status map2Initialized = new Status();

    Game.world().addListener("test-map", new EnvironmentListener() {
      @Override
      public void initialized(Environment environment) {
        mapInitialized.wasCalled = true;
      }

      @Override
      public void loaded(Environment environment) {
        mapLoaded.wasCalled = true;
      }

      @Override
      public void unloaded(Environment environment) {
        mapUnloaded.wasCalled = true;
      }

      @Override
      public void cleared(Environment environment) {
        mapCleared.wasCalled = true;
      }
    });

    Game.world().addListener("test-mapobject", new EnvironmentListener() {

      @Override
      public void initialized(Environment environment) {
        map2Initialized.wasCalled = true;
      }

      @Override
      public void loaded(Environment environment) {
        map2Loaded.wasCalled = true;
      }
    });

    IMap map = Resources.maps().get("tests/com/litiengine/environment/tilemap/xml/test-map.tmx");

    Environment env = Game.world().loadEnvironment(map);

    assertTrue(mapLoaded.wasCalled);
    assertTrue(mapInitialized.wasCalled);

    env.clear();

    assertTrue(mapCleared.wasCalled);

    IMap map2 = Resources.maps().get("tests/com/litiengine/environment/tilemap/xml/test-mapobject.tmx");

    Game.world().loadEnvironment(map2);

    assertTrue(mapUnloaded.wasCalled);
    assertTrue(map2Loaded.wasCalled);
    assertTrue(map2Initialized.wasCalled);
  }

  @Test
  public void testMapSpecificLoadedListeners() {
    Status mapLoaded = new Status();

    Game.world().onLoaded("test-map", e -> mapLoaded.wasCalled = true);

    IMap map = Resources.maps().get("tests/com/litiengine/environment/tilemap/xml/test-map.tmx");

    Game.world().loadEnvironment(map);

    assertTrue(mapLoaded.wasCalled);
  }

  @Test
  public void testMapSpecificUnloadedListeners() {
    Status mapUnloaded = new Status();

    Game.world().onUnloaded("test-map", e -> mapUnloaded.wasCalled = true);

    IMap map = Resources.maps().get("tests/com/litiengine/environment/tilemap/xml/test-map.tmx");

    Game.world().loadEnvironment(map);

    Game.world().unloadEnvironment();

    assertTrue(mapUnloaded.wasCalled);
  }

  private class Status {
    boolean wasCalled = false;
  }
}