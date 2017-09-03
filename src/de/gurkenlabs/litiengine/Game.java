package de.gurkenlabs.litiengine;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.LogManager;

import de.gurkenlabs.core.DefaultUncaughtExceptionHandler;
import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.entities.ai.EntityControllerManager;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.IRenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.gui.screens.IScreenManager;
import de.gurkenlabs.litiengine.gui.screens.ScreenManager;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.physics.IPhysicsEngine;
import de.gurkenlabs.litiengine.physics.PhysicsEngine;
import de.gurkenlabs.litiengine.sound.ISoundEngine;
import de.gurkenlabs.litiengine.sound.SoundEngine;

public final class Game {
  private final static GameConfiguration configuration;
  private final static EntityControllerManager entityControllerManager;
  private static IEnvironment environment;

  private final static List<Consumer<IEnvironment>> environmentLoadedConsumer;
  private static IGameLoop gameLoop;
  private final static IRenderEngine graphicsEngine;
  private final static GameInfo info;
  private final static List<IMap> maps;
  private final static GameMetrics metrics;
  private final static IPhysicsEngine physicsEngine;
  private static RenderLoop renderLoop;
  private static IScreenManager screenManager;
  private final static ISoundEngine soundEngine;

  private final static List<Consumer<String>> startedConsumer;
  private final static List<Predicate<String>> terminatingConsumer;
  private final static List<Consumer<GameConfiguration>> configLoadedConsumer;

  static {
    startedConsumer = new CopyOnWriteArrayList<>();
    terminatingConsumer = new CopyOnWriteArrayList<>();
    environmentLoadedConsumer = new CopyOnWriteArrayList<>();
    configLoadedConsumer = new CopyOnWriteArrayList<>();
    graphicsEngine = new RenderEngine();
    physicsEngine = new PhysicsEngine();
    soundEngine = new SoundEngine();
    metrics = new GameMetrics();
    entityControllerManager = new EntityControllerManager();
    info = new GameInfo();
    maps = new CopyOnWriteArrayList<>();

    // init configuration before init method in order to use configured values
    // to initialize components
    configuration = new GameConfiguration();
  }

  public static GameConfiguration getConfiguration() {
    return configuration;
  }

  public static EntityControllerManager getEntityControllerManager() {
    return entityControllerManager;
  }

  public static IEnvironment getEnvironment() {
    return environment;
  }

  public static GameInfo getInfo() {
    return info;
  }

  public static IGameLoop getLoop() {
    return gameLoop;
  }

  public static IMap getMap(final String mapName) {
    if (mapName == null || mapName.isEmpty() || maps.size() == 0) {
      return null;
    }

    for (final IMap map : maps) {
      if (map.getFileName().equals(mapName)) {
        return map;
      }
    }

    return null;
  }

  public static List<IMap> getMaps() {
    return maps;
  }

  public static GameMetrics getMetrics() {
    return metrics;
  }

  public static IPhysicsEngine getPhysicsEngine() {
    return physicsEngine;
  }

  public static IRenderEngine getRenderEngine() {
    return graphicsEngine;
  }

  public static RenderLoop getRenderLoop() {
    return renderLoop;
  }

  public static IScreenManager getScreenManager() {
    return screenManager;
  }

  public static ISoundEngine getSoundEngine() {
    return soundEngine;
  }

  public static void init() {
    getConfiguration().load();
    Locale.setDefault(new Locale(getConfiguration().CLIENT.getCountry(), getConfiguration().CLIENT.getLanguage()));
    for (Consumer<GameConfiguration> cons : configLoadedConsumer) {
      cons.accept(getConfiguration());
    }

    final GameLoop updateLoop = new GameLoop(getConfiguration().CLIENT.getUpdaterate());
    updateLoop.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
    gameLoop = updateLoop;

    final ScreenManager scrMgr = new ScreenManager(getInfo().toString());

    // setup default exception handling for render and update loop
    renderLoop = new RenderLoop(scrMgr.getRenderComponent(), scrMgr);
    renderLoop.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());

    getLoop().attach(getPhysicsEngine());
    getLoop().onUpsTracked(updateCount -> getMetrics().setUpdatesPerSecond(updateCount));

    Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());

    // ensures that we terminate the game, when the window is closed
    scrMgr.addWindowListener(new WindowHandler());
    screenManager = scrMgr;

    final String LOGGING_CONFIG_FILE = "logging.properties";
    // init logging
    if (new File(LOGGING_CONFIG_FILE).exists()) {
      System.setProperty("java.util.logging.config.file", LOGGING_CONFIG_FILE);

      try {
        LogManager.getLogManager().readConfiguration();
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }

    if (getConfiguration().CLIENT.showGameMetrics()) {
      getScreenManager().getRenderComponent().onRendered((g) -> getMetrics().render(g));
    }

    if (getConfiguration().DEBUG.isDebugEnabled()) {
      getRenderEngine().onEntityRendered(e -> DebugRenderer.renderEntityDebugInfo(e.getGraphics(), e.getRenderedObject()));
    }

    getRenderEngine().onMapRendered(e -> {
      DebugRenderer.renderMapDebugInfo(e.getGraphics(), e.getRenderedObject());
    });

    // init screens
    getScreenManager().init(getConfiguration().GRAPHICS.getResolutionWidth(), getConfiguration().GRAPHICS.getResolutionHeight(), getConfiguration().GRAPHICS.isFullscreen());
    getScreenManager().getRenderComponent().onFpsChanged(fps -> {
      getMetrics().setFramesPerSecond(fps);
    });

    getScreenManager().setIconImage(RenderEngine.getImage("icon.png"));

    Input.init();
    getScreenManager().getRenderComponent().addMouseListener(Input.MOUSE);
    getScreenManager().getRenderComponent().addMouseMotionListener(Input.MOUSE);
    getScreenManager().getRenderComponent().addMouseWheelListener(Input.MOUSE);

    Input.KEYBOARD.onKeyTyped(KeyEvent.VK_PRINTSCREEN, key -> getScreenManager().getRenderComponent().takeScreenshot());
  }

  public static void load(final String gameResourceFile) {
    final GameFile file = GameFile.load(gameResourceFile);
    if (file == null) {
      return;
    }

    int mapCnt = 0;
    for (final IMap m : file.getMaps()) {
      if (getMaps().stream().anyMatch(x -> x.getFileName().equals(m.getFileName()))) {
        continue;
      }

      getMaps().add(m);
      mapCnt++;
    }

    System.out.println(mapCnt + " maps loaded from '" + gameResourceFile + "'");

    final List<Spritesheet> loadedSprites = new ArrayList<>();
    for (final String spriteFile : file.getSpriteFiles()) {
      final List<Spritesheet> sprites = Spritesheet.load(getInfo().getSpritesDirectory() + spriteFile);
      loadedSprites.addAll(sprites);
    }

    for (final SpriteSheetInfo tileset : file.getTileSets()) {
      final Spritesheet sprite = Spritesheet.load(tileset);
      loadedSprites.add(sprite);
    }

    int spriteload = 0;
    for (final Spritesheet s : loadedSprites) {
      for (int i = 0; i < s.getRows() * s.getColumns(); i++) {
        s.getSprite(i);
        spriteload++;
      }
    }

    System.out.println(spriteload + " sprites loaded to memory");
  }

  public static void loadEnvironment(final IEnvironment env) {
    if (getEnvironment() != null) {
      getEnvironment().unload();
    }

    environment = env;
    if (getEnvironment() != null) {
      getEnvironment().load();
    }

    for (final Consumer<IEnvironment> cons : environmentLoadedConsumer) {
      cons.accept(getEnvironment());
    }
  }

  public static void onEnvironmentLoaded(final Consumer<IEnvironment> cons) {
    environmentLoadedConsumer.add(cons);
  }

  public static void onStarted(final Consumer<String> cons) {
    startedConsumer.add(cons);
  }

  /**
   * Returning false prevents the terminate event to continue.
   *
   * @param cons
   */
  public static void onTerminating(final Predicate<String> cons) {
    terminatingConsumer.add(cons);
  }

  public static void onConfigurationLoaded(final Consumer<GameConfiguration> conf) {

  }

  public static void start() {
    gameLoop.start();
    soundEngine.start();
    renderLoop.start();

    for (final Consumer<String> cons : startedConsumer) {
      cons.accept(Game.getInfo().getName());
    }
  }

  public static void terminate() {
    for (final Predicate<String> cons : terminatingConsumer) {
      if (!cons.test(Game.getInfo().getName())) {
        return;
      }
    }

    getConfiguration().save();
    gameLoop.terminate();

    soundEngine.terminate();
    renderLoop.terminate();

    System.exit(0);
  }
}
