package de.gurkenlabs.litiengine;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.lang.annotation.AnnotationFormatError;
import java.util.logging.LogManager;

import de.gurkenlabs.core.DefaultUncaughtExceptionHandler;
import de.gurkenlabs.core.IInitializable;
import de.gurkenlabs.core.ILaunchable;
import de.gurkenlabs.litiengine.annotation.GameInfo;
import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.graphics.IRenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.gui.screens.IScreenManager;
import de.gurkenlabs.litiengine.gui.screens.ScreenManager;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.physics.IPhysicsEngine;
import de.gurkenlabs.litiengine.physics.PhysicsEngine;
import de.gurkenlabs.litiengine.sound.ISoundEngine;
import de.gurkenlabs.litiengine.sound.PaulsSoundEngine;
import de.gurkenlabs.util.console.CommandManager;
import de.gurkenlabs.util.console.ICommandManager;

public abstract class Game implements IInitializable, ILaunchable {
  private static GameInfo info;

  private static final GameConfiguration configuration = new GameConfiguration();
  private static IScreenManager screenManager;
  private static IRenderEngine graphicsEngine;
  private static IPhysicsEngine physicsEngine;
  private static ISoundEngine soundEngine;
  private static IGameLoop gameLoop;
  private static GameMetrics metrics;
  private static ICommandManager commandManager;

  private final RenderLoop renderLoop;

  protected Game() {
    final GameInfo inf = this.getClass().getAnnotation(GameInfo.class);
    if (inf == null) {
      throw new AnnotationFormatError("No GameInfo annotation found on game implementation " + this.getClass());
    }
    info = inf;

    Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
    final String gameTitle = !getInfo().subTitle().isEmpty() ? getInfo().name() + " - " + getInfo().subTitle() + " " + getInfo().version() : getInfo().name() + " - " + getInfo().version();
    final ScreenManager scrMgr = new ScreenManager(gameTitle);

    // ensures that we terminate the game, when the window is closed
    scrMgr.addWindowListener(new WindowHandler());
    screenManager = scrMgr;
    graphicsEngine = new RenderEngine();
    physicsEngine = new PhysicsEngine();
    soundEngine = new PaulsSoundEngine();

    metrics = new GameMetrics();
    commandManager = new CommandManager();

    // init configuration before init method in order to use configured values
    // to initialize components
    getConfiguration().load();

    // setup default exception handling for render and update loop
    this.renderLoop = new RenderLoop();
    this.renderLoop.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
    final GameLoop updateLoop = new GameLoop(getConfiguration().CLIENT.getUpdaterate());
    updateLoop.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
    gameLoop = updateLoop;

    getLoop().onUpsTracked(updateCount -> getMetrics().setUpdatesPerSecond(updateCount));
  }

  public static GameConfiguration getConfiguration() {
    return configuration;
  }

  public static GameInfo getInfo() {
    return info;
  }

  public static GameMetrics getMetrics() {
    return metrics;
  }

  public static IGameLoop getLoop() {
    return gameLoop;
  }

  public static IPhysicsEngine getPhysicsEngine() {
    return physicsEngine;
  }

  public static IRenderEngine getRenderEngine() {
    return graphicsEngine;
  }

  public static ISoundEngine getSoundEngine() {
    return soundEngine;
  }

  public static IScreenManager getScreenManager() {
    return screenManager;
  }

  public static ICommandManager getCommandManager() {
    return commandManager;
  }

  @Override
  public void init() {
    // init logging
    if (new File("logging.properties").exists()) {
      System.setProperty("java.util.logging.config.file", "logging.properties");

      try {
        LogManager.getLogManager().readConfiguration();
      }

      catch (final Exception e) {
        e.printStackTrace();
      }
    }

    // init screens
    getScreenManager().init(getConfiguration().GRAPHICS.getResolutionWidth(), getConfiguration().GRAPHICS.getResolutionHeight(), getConfiguration().GRAPHICS.isFullscreen());
    getScreenManager().onFpsChanged(fps -> {
      getMetrics().setFramesPerSecond(fps);
    });

    // init sounds
    soundEngine.init(getConfiguration().SOUND.getSoundVolume());

    // init inputs
    Input.init();
    getScreenManager().getRenderComponent().addMouseListener(Input.MOUSE);
    getScreenManager().getRenderComponent().addMouseMotionListener(Input.MOUSE);
    getScreenManager().getRenderComponent().addMouseWheelListener(Input.MOUSE);
  }

  @Override
  public void start() {
    commandManager.start();
    gameLoop.start();
    soundEngine.start();
    this.renderLoop.start();
  }

  @Override
  public void terminate() {
    commandManager.terminate();
    gameLoop.terminate();
    soundEngine.terminate();
    this.renderLoop.terminate();
  }

  /**
   * The Class RenderLoop.
   */
  private class RenderLoop extends Thread {

    /** The game is running. */
    private boolean gameIsRunning = true;

    /** The next render tick. */
    private long nextRenderTick = System.currentTimeMillis();

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      while (this.gameIsRunning) {
        final int SKIP_FRAMES = 1000 / Game.getConfiguration().CLIENT.getMaxFps();

        if (System.currentTimeMillis() > this.nextRenderTick) {
          Game.getScreenManager().renderCurrentScreen();
          this.nextRenderTick += SKIP_FRAMES;
        }
      }
    }

    /**
     * Terminate.
     */
    public void terminate() {
      this.gameIsRunning = false;
    }
  }

  /**
   * The Class WindowHandler.
   */
  private class WindowHandler implements WindowListener {

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
     */
    @Override
    public void windowActivated(final WindowEvent event) {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosed(final WindowEvent event) {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosing(final WindowEvent event) {
      Game.this.terminate();
      System.exit(0);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.
     * WindowEvent)
     */
    @Override
    public void windowDeactivated(final WindowEvent event) {
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.
     * WindowEvent)
     */
    @Override
    public void windowDeiconified(final WindowEvent event) {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
     */
    @Override
    public void windowIconified(final WindowEvent event) {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
     */
    @Override
    public void windowOpened(final WindowEvent event) {
    }
  }
}
