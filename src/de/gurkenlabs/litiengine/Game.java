package de.gurkenlabs.litiengine;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import de.gurkenlabs.core.DefaultUncaughtExceptionHandler;
import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.entities.ai.EntityControllerManager;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.graphics.Camera;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.ICamera;
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
  protected static long environmentLoadTick;
  private static final Logger log = Logger.getLogger(Game.class.getName());
  private static final String LOGGING_CONFIG_FILE = "logging.properties";
  private static final GameConfiguration configuration;
  private static final EntityControllerManager entityControllerManager;

  private static final List<Consumer<IEnvironment>> environmentLoadedConsumer;

  private static final IRenderEngine graphicsEngine;
  private static final GameInfo info;
  private static final List<IMap> maps;
  private static final GameMetrics metrics;
  private static final IPhysicsEngine physicsEngine;
  private static final ISoundEngine soundEngine;
  private static final GameTime gameTime;

  private static IEnvironment environment;
  private static ICamera camera;
  private static IGameLoop gameLoop;
  private static RenderLoop renderLoop;
  private static IScreenManager screenManager;

  private static final List<Consumer<String>> startedConsumer;
  private static final List<Predicate<String>> terminatingConsumer;
  private static final List<Consumer<GameConfiguration>> configLoadedConsumer;

  private static boolean hasStarted;

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
    gameTime = new GameTime();

    // init configuration before init method in order to use configured values
    // to initialize components
    configuration = new GameConfiguration();
  }

  private Game() {
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
    if (mapName == null || mapName.isEmpty() || maps.isEmpty()) {
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

  public static ICamera getCamera() {
    return camera;
  }

  public static GameTime getTime() {
    return gameTime;
  }

  public static void init() {
    getConfiguration().load();
    Locale.setDefault(new Locale(getConfiguration().client().getCountry(), getConfiguration().client().getLanguage()));
    for (Consumer<GameConfiguration> cons : configLoadedConsumer) {
      cons.accept(getConfiguration());
    }

    final GameLoop updateLoop = new GameLoop(getConfiguration().client().getUpdaterate());
    updateLoop.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
    gameLoop = updateLoop;
    getLoop().attach(getPhysicsEngine());

    final ScreenManager scrMgr = new ScreenManager(getInfo().toString());

    // setup default exception handling for render and update loop
    renderLoop = new RenderLoop(scrMgr.getRenderComponent());
    renderLoop.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());

    getLoop().onUpsTracked(updateCount -> getMetrics().setUpdatesPerSecond(updateCount));

    Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());

    screenManager = scrMgr;
    setCamera(new Camera());

    // init logging
    if (new File(LOGGING_CONFIG_FILE).exists()) {
      System.setProperty("java.util.logging.config.file", LOGGING_CONFIG_FILE);

      try {
        LogManager.getLogManager().readConfiguration();
      } catch (final Exception e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    if (getConfiguration().client().showGameMetrics()) {
      getScreenManager().getRenderComponent().onRendered(g -> getMetrics().render(g));
    }

    if (getConfiguration().debug().isDebugEnabled()) {
      getRenderEngine().onEntityRendered(e -> DebugRenderer.renderEntityDebugInfo(e.getGraphics(), e.getRenderedObject()));
    }

    getRenderEngine().onMapRendered(e -> DebugRenderer.renderMapDebugInfo(e.getGraphics(), e.getRenderedObject()));

    // init screens
    getScreenManager().init(getConfiguration().graphics().getResolutionWidth(), getConfiguration().graphics().getResolutionHeight(), getConfiguration().graphics().isFullscreen());
    getScreenManager().getRenderComponent().onFpsChanged(fps -> getMetrics().setFramesPerSecond(fps));

    getScreenManager().setIconImage(RenderEngine.getImage("litiengine-icon.png"));

    // init inputs
    Input.init();
    getScreenManager().getRenderComponent().addMouseListener(Input.mouse());
    getScreenManager().getRenderComponent().addMouseMotionListener(Input.mouse());
    getScreenManager().getRenderComponent().addMouseWheelListener(Input.mouse());

    Input.keyboard().onKeyTyped(KeyEvent.VK_PRINTSCREEN, key -> getScreenManager().getRenderComponent().takeScreenshot());
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

    log.log(Level.INFO, "{0} maps loaded from {1}", new Object[] { mapCnt, gameResourceFile });

    final List<Spritesheet> loadedSprites = new ArrayList<>();
    for (final String spriteFile : file.getSpriteFiles()) {
      final List<Spritesheet> sprites = Spritesheet.load(GameDirectories.SPRITES + spriteFile);
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

    log.log(Level.INFO, "{0} sprites loaded to memory", new Object[] { spriteload });
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

    environmentLoadTick = getLoop().getTicks();
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

  public static void onConfigurationLoaded(final Consumer<GameConfiguration> cons) {
    configLoadedConsumer.add(cons);
  }

  public static void start() {
    gameLoop.start();
    Input.start();

    soundEngine.start();
    renderLoop.start();

    for (final Consumer<String> cons : startedConsumer) {
      cons.accept(Game.getInfo().getName());
    }

    hasStarted = true;
  }

  public static void terminate() {
    for (final Predicate<String> cons : terminatingConsumer) {
      if (!cons.test(Game.getInfo().getName())) {
        return;
      }
    }

    getConfiguration().save();
    Input.terminate();
    gameLoop.terminate();

    soundEngine.terminate();
    renderLoop.terminate();

    System.exit(0);
  }

  public static boolean hasStarted() {
    return hasStarted;
  }

  public static void setCamera(final ICamera cam) {
    if (getCamera() != null) {
      Game.getLoop().detach(camera);
    }

    Game.getLoop().attach(cam);
    camera = cam;

    getCamera().updateFocus();
  }

}
