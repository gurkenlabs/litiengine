package de.gurkenlabs.litiengine.environment;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.resources.Resources;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameWorldTests {

  @BeforeEach
  public void initGame() {
    // necessary because the environment needs access to the game loop and other stuff
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @AfterEach
  public void terminateGame() {
    GameTest.terminateGame();
  }

  @Test
  void testListeners_loadEnvironment() {
    // arrange
    Status mapInitialized = new Status();
    Status mapLoaded = new Status();

    Game.world()
        .addListener(
            "test-map",
            new EnvironmentListener() {
              @Override
              public void initialized(Environment environment) {
                mapInitialized.wasCalled = true;
              }

              @Override
              public void loaded(Environment environment) {
                mapLoaded.wasCalled = true;
              }
            });
    IMap map =
        Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx");

    // act
    Environment env = Game.world().loadEnvironment(map);

    // assert
    assertNotNull(env);
    assertTrue(mapInitialized.wasCalled);
    assertTrue(mapLoaded.wasCalled);
  }

  @Test
  void testListeners_clearEnvironment() {
    Status mapUnloaded = new Status();
    Status mapCleared = new Status();

    Game.world()
        .addListener(
            "test-map",
            new EnvironmentListener() {
              @Override
              public void unloaded(Environment environment) {
                mapUnloaded.wasCalled = true;
              }

              @Override
              public void cleared(Environment environment) {
                mapCleared.wasCalled = true;
              }
            });
    IMap map =
        Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx");
    Environment env = Game.world().loadEnvironment(map);

    // act
    env.clear();

    // assert
    assertTrue(mapCleared.wasCalled);
  }

  @Test
  void testListeners_loadDifferentEnvironment() {
    Status mapUnloaded = new Status();
    Status map2Initialized = new Status();
    Status map2Loaded = new Status();

    Game.world()
        .addListener(
            "test-map",
            new EnvironmentListener() {
              @Override
              public void unloaded(Environment environment) {
                mapUnloaded.wasCalled = true;
              }
            });

    Game.world()
        .addListener(
            "test-mapobject",
            new EnvironmentListener() {
              @Override
              public void initialized(Environment environment) {
                map2Initialized.wasCalled = true;
              }

              @Override
              public void loaded(Environment environment) {
                map2Loaded.wasCalled = true;
              }
            });
    IMap map =
        Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx");
    Game.world().loadEnvironment(map);
    IMap map2 =
        Resources.maps()
            .get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-mapobject.tmx");

    // act
    Game.world().loadEnvironment(map2);

    // assert
    assertTrue(mapUnloaded.wasCalled);
    assertTrue(map2Loaded.wasCalled);
    assertTrue(map2Initialized.wasCalled);
  }

  @Test
  void testMapSpecificLoadedListeners() {
    // arrange
    Status mapLoaded = new Status();
    Game.world().onLoaded("test-map", e -> mapLoaded.wasCalled = true);
    IMap map =
        Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx");

    // act
    Game.world().loadEnvironment(map);

    // assert
    assertTrue(mapLoaded.wasCalled);
  }

  @Test
  void testMapSpecificUnloadedListeners() {
    // arrange
    Status mapUnloaded = new Status();
    Game.world().onUnloaded("test-map", e -> mapUnloaded.wasCalled = true);
    IMap map =
        Resources.maps().get("de/gurkenlabs/litiengine/environment/tilemap/xml/test-map.tmx");
    Game.world().loadEnvironment(map);

    // act
    Game.world().unloadEnvironment();

    // assert
    assertTrue(mapUnloaded.wasCalled);
  }

  private class Status {
    boolean wasCalled = false;
  }
}
