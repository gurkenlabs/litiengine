package de.gurkenlabs.litiengine;

import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.LogManager;

import de.gurkenlabs.core.DefaultUncaughtExceptionHandler;
import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.entities.Collider;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.ai.EntityManager;
import de.gurkenlabs.litiengine.environment.IEnvironment;
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
import de.gurkenlabs.litiengine.sound.PaulsSoundEngine;
import de.gurkenlabs.tilemap.IMap;

public abstract class Game {
  private final static List<Consumer<String>> startedConsumer;
  private final static List<Predicate<String>> terminatingConsumer;
  private final static List<Consumer<IEnvironment>> environmentLoadedConsumer;

  private final static GameConfiguration configuration;
  private final static IRenderEngine graphicsEngine;
  private final static IPhysicsEngine physicsEngine;
  private final static ISoundEngine soundEngine;
  private static IGameLoop gameLoop;
  private final static GameMetrics metrics;
  private final static EntityManager entityManager;
  private static RenderLoop renderLoop;
  private final static GameInfo info;
  private final static List<IMap> maps;

  private static IScreenManager screenManager;
  private static IEnvironment environment;

  static {
    startedConsumer = new CopyOnWriteArrayList<>();
    terminatingConsumer = new CopyOnWriteArrayList<>();
    environmentLoadedConsumer = new CopyOnWriteArrayList<>();
    graphicsEngine = new RenderEngine();
    physicsEngine = new PhysicsEngine();
    soundEngine = new PaulsSoundEngine();
    metrics = new GameMetrics();
    entityManager = new EntityManager();
    info = new GameInfo();
    maps = new CopyOnWriteArrayList<>();

    // init configuration before init method in order to use configured values
    // to initialize components
    configuration = new GameConfiguration();
  }

  public static GameConfiguration getConfiguration() {
    return configuration;
  }

  public static EntityManager getEntityManager() {
    return entityManager;
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

  public static RenderLoop getRenderLoop() {
    return renderLoop;
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

  public static IScreenManager getScreenManager() {
    return screenManager;
  }

  public static ISoundEngine getSoundEngine() {
    return soundEngine;
  }

  public static List<IMap> getMaps() {
    return maps;
  }

  public static IMap getMap(String mapName) {
    if (mapName == null || mapName.isEmpty() || maps.size() == 0) {
      return null;
    }

    for (IMap map : maps) {
      if (map.getFileName().equals(mapName)) {
        return map;
      }
    }

    return null;
  }

  public static void loadEnvironment(final IEnvironment env) {
    environment = env;
    Game.getPhysicsEngine().clear();
    if (getEnvironment() != null) {
      getPhysicsEngine().setBounds(new Rectangle2D.Double(0, 0, getEnvironment().getMap().getSizeInPixels().getWidth(), getEnvironment().getMap().getSizeInPixels().getHeight()));
      for (IEntity entity : getEnvironment().getEntities()) {
        if (entity instanceof Collider) {
          Collider coll = (Collider) entity;
          if (coll.isObstacle()) {
            Game.getPhysicsEngine().add(coll.getBoundingBox());
          } else {
            Game.getPhysicsEngine().add(coll);
          }
        } else if (entity instanceof ICollisionEntity) {
          final ICollisionEntity coll = (ICollisionEntity) entity;
          if (coll.hasCollision()) {
            Game.getPhysicsEngine().add(coll);
          }
        }
      }
    }

    for (Consumer<IEnvironment> cons : environmentLoadedConsumer) {
      cons.accept(getEnvironment());
    }
  }

  public static void init() {
    getConfiguration().load();

    final GameLoop updateLoop = new GameLoop(getConfiguration().CLIENT.getUpdaterate());
    updateLoop.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
    gameLoop = updateLoop;

    final ScreenManager scrMgr = new ScreenManager(getInfo().toString());

    // setup default exception handling for render and update loop
    renderLoop = new RenderLoop(scrMgr.getRenderComponent(), scrMgr);
    renderLoop.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());

    getLoop().registerForUpdate(getPhysicsEngine());
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

    // init sounds
    soundEngine.init(getConfiguration().SOUND.getSoundVolume());

    getScreenManager().getRenderComponent().addMouseListener(Input.MOUSE);
    getScreenManager().getRenderComponent().addMouseMotionListener(Input.MOUSE);
    getScreenManager().getRenderComponent().addMouseWheelListener(Input.MOUSE);

    Input.KEYBOARD.onKeyTyped(KeyEvent.VK_PRINTSCREEN, key -> getScreenManager().getRenderComponent().takeScreenshot());
  }

  public static void start() {
    gameLoop.start();
    soundEngine.start();
    renderLoop.start();

    for (Consumer<String> cons : startedConsumer) {
      cons.accept(Game.getInfo().getName());
    }
  }

  public static void load(String gameResourceFile) {
    GameFile file = GameFile.load(gameResourceFile);
    if (file == null) {
      return;
    }

    int mapCnt = 0;
    for (IMap m : file.getMaps()) {
      if (getMaps().stream().anyMatch(x -> x.getFileName().equals(m.getFileName()))) {
        continue;
      }

      getMaps().add(m);
      mapCnt++;
    }

    List<Spritesheet> loadedSprites = new ArrayList<>();
    for (String spriteFile : file.getSpriteFiles()) {
      List<Spritesheet> sprites = Spritesheet.load(getInfo().getSpritesDirectory() + spriteFile);
      loadedSprites.addAll(sprites);
    }

    for (SpriteSheetInfo tileset : file.getTileSets()) {
      Spritesheet sprite = Spritesheet.load(tileset);
      loadedSprites.add(sprite);
    }

    int spriteload = 0;
    for (Spritesheet s : loadedSprites) {
      for (int i = 0; i < s.getRows() * s.getColumns(); i++) {
        s.getSprite(i);
        spriteload++;
      }
    }

    System.out.println(spriteload + " sprites loaded to memory");
    System.out.println(mapCnt + " maps loaded from '" + gameResourceFile + "'");
  }

  public static void terminate() {
    for (Predicate<String> cons : terminatingConsumer) {
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

  public static void onStarted(Consumer<String> cons) {
    startedConsumer.add(cons);
  }

  /**
   * Returning false prevents the terminate event to continue.
   * 
   * @param cons
   */
  public static void onTerminating(Predicate<String> cons) {
    terminatingConsumer.add(cons);
  }

  public static void onEnvironmentLoaded(Consumer<IEnvironment> cons) {
    environmentLoadedConsumer.add(cons);
  }
}
