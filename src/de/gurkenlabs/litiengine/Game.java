package de.gurkenlabs.litiengine;

import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.LogManager;

import de.gurkenlabs.core.DefaultUncaughtExceptionHandler;
import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.entities.ai.EntityManager;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.IRenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderComponent;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.gui.screens.IScreenManager;
import de.gurkenlabs.litiengine.gui.screens.ScreenManager;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.physics.IPhysicsEngine;
import de.gurkenlabs.litiengine.physics.PhysicsEngine;
import de.gurkenlabs.litiengine.sound.ISoundEngine;
import de.gurkenlabs.litiengine.sound.PaulsSoundEngine;
import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;
import de.gurkenlabs.util.io.FileUtilities;
import de.gurkenlabs.util.io.StreamUtilities;

public abstract class Game {
  private final static List<Consumer<String>> startedConsumer;
  private final static List<Consumer<String>> terminatedConsumer;

  private final static GameConfiguration configuration;
  private final static IRenderEngine graphicsEngine;
  private final static IPhysicsEngine physicsEngine;
  private final static ISoundEngine soundEngine;
  private final static IGameLoop gameLoop;
  private final static GameMetrics metrics;
  private final static EntityManager entityManager;
  private static RenderLoop renderLoop;
  private final static GameInfo info;

  private static IScreenManager screenManager;
  private static IEnvironment environment;

  static {
    startedConsumer = new CopyOnWriteArrayList<>();
    terminatedConsumer = new CopyOnWriteArrayList<>();
    graphicsEngine = new RenderEngine();
    physicsEngine = new PhysicsEngine();
    soundEngine = new PaulsSoundEngine();
    metrics = new GameMetrics();
    entityManager = new EntityManager();
    info = new GameInfo();

    // init configuration before init method in order to use configured values
    // to initialize components
    configuration = new GameConfiguration();
    getConfiguration().load();
    
    final GameLoop updateLoop = new GameLoop(getConfiguration().CLIENT.getUpdaterate());
    updateLoop.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
    gameLoop = updateLoop;
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
  
  public static RenderLoop getRenderLoop(){
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

  public static void loadEnvironment(final IEnvironment env) {
    environment = env;
    environment.init();
    getPhysicsEngine().setBounds(new Rectangle2D.Double(0, 0, environment.getMap().getSizeInPixels().getWidth(), environment.getMap().getSizeInPixels().getHeight()));
  }

  public static void init() {
    final String gameTitle = !getInfo().getSubTitle().isEmpty() ? getInfo().getName() + " - " + getInfo().getSubTitle() + " " + getInfo().getVersion() : getInfo().getName() + " - " + getInfo().getVersion();
    final ScreenManager scrMgr = new ScreenManager(gameTitle);

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
    final InputStream defaultLoggingConfig = FileUtilities.getGameFile(LOGGING_CONFIG_FILE);

    // if a specific file exists, load it
    // otherwise try to find a default logging configuration in any resource
    // folder.
    if (!new File(LOGGING_CONFIG_FILE).exists() && defaultLoggingConfig != null) {
      try {
        StreamUtilities.copy(defaultLoggingConfig, new File(LOGGING_CONFIG_FILE));
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }

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
    
    for(Consumer<String> cons : startedConsumer){
      cons.accept(Game.getInfo().getName());
    }
  }

  public static void terminate() {
    gameLoop.terminate();

    soundEngine.terminate();
    renderLoop.terminate();
    
    for(Consumer<String> cons : terminatedConsumer){
      cons.accept(Game.getInfo().getName());
    }
    
    System.exit(0);
  }
  
  public static void onStarted(Consumer<String> cons){
    startedConsumer.add(cons);
  }
  
  public static void onTerminated(Consumer<String> cons){
    terminatedConsumer.add(cons);
  }
}
