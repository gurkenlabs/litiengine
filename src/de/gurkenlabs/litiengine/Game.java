package de.gurkenlabs.litiengine;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.annotation.AnnotationFormatError;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.annotation.GameInfo;
import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.graphics.DefaultCamera;
import de.gurkenlabs.litiengine.graphics.GraphicsEngine;
import de.gurkenlabs.litiengine.graphics.IGraphicsEngine;
import de.gurkenlabs.litiengine.gui.screens.IScreenManager;
import de.gurkenlabs.litiengine.gui.screens.ScreenManager;
import de.gurkenlabs.litiengine.input.Input;

public abstract class Game implements IGame {
  private static final List<IUpdateable> updatables = new CopyOnWriteArrayList<>();
  private static final List<Consumer<Integer>> upsChangedConsumer = new CopyOnWriteArrayList<>();

  private static int updateRate;
  private static int updateCount;
  private static long lastUpsTime;
  private static long lastUpdateTime;

  private static long gameTicks;
  private final GameInfo info;

  private final GameConfiguration configuration;
  private final IScreenManager screenManager;
  private final IGraphicsEngine graphicsEngine;
  private final RenderLoop renderLoop;

  private final GameLoop gameLoop;

  protected Game() {
    final GameInfo info = this.getClass().getAnnotation(GameInfo.class);
    if (info == null) {
      throw new AnnotationFormatError("No GameInfo annotation found on game implementation " + this.getClass());
    }

    this.info = info;
    final ScreenManager screenManager = this.createScreenManager(this.getInfo().name() + " " + this.getInfo().version());

    // ensures that we terminate the game, when the window is closed
    screenManager.addWindowListener(new WindowHandler());
    this.screenManager = screenManager;

    // init configuration before init method in order to use configured values
    // to initialize components
    this.configuration = this.createConfiguration();
    this.getConfiguration().load();
    updateRate = this.getConfiguration().CLIENT.getUpdaterate();

    this.renderLoop = new RenderLoop();
    this.gameLoop = new GameLoop();

    this.graphicsEngine = new GraphicsEngine(this.getConfiguration().GRAPHICS, new DefaultCamera(this.getScreenManager()), this.info.orientation());
  }

  public static long convertToMs(final long ticks) {
    return (long) (ticks / (updateRate / 1000.0));
  }

  public static long getDeltaTime() {
    return System.currentTimeMillis() - lastUpdateTime;
  }

  /**
   * Calculates the deltatime between the current game time and the specified
   * ticks in ms.
   * 
   * @param ticks
   * @return The delta time in ms.
   */
  public static long getDeltaTime(final long ticks) {
    return convertToMs(gameTicks - ticks);
  }

  public static long getTicks() {
    return gameTicks;
  }

  /**
   * Update.
   */
  private static void update() {
    ++gameTicks;
    updatables.forEach(updatable -> updatable.update());

    updateCount++;

    final long currentMillis = System.currentTimeMillis();
    if (currentMillis - lastUpsTime >= 1000) {
      lastUpsTime = currentMillis;
      upsChangedConsumer.forEach(consumer -> consumer.accept(updateCount));
      updateCount = 0;
    }

    lastUpdateTime = currentMillis;
  }

  @Override
  public GameConfiguration getConfiguration() {
    return this.configuration;
  }

  @Override
  public IGraphicsEngine getGraphicsEngine() {
    return this.graphicsEngine;
  }

  @Override
  public GameInfo getInfo() {
    return this.info;
  }

  @Override
  public IScreenManager getScreenManager() {
    return this.screenManager;
  }

  @Override
  public void init() {
    // init screens
    this.getScreenManager().init(this.getConfiguration().GRAPHICS.getResolutionWidth(), this.getConfiguration().GRAPHICS.getResolutionHeight(),
        this.getConfiguration().GRAPHICS.isFullscreen());

    // TODO: init sounds

    // init inputs
    Input.init(this);
    this.getScreenManager().getRenderComponent().addMouseListener(Input.MOUSE);
    this.getScreenManager().getRenderComponent().addMouseMotionListener(Input.MOUSE);
    this.getScreenManager().getRenderComponent().addMouseWheelListener(Input.MOUSE);
  }

  @Override
  public void onUpsChanged(final Consumer<Integer> upsConsumer) {
    if (!upsChangedConsumer.contains(upsConsumer)) {
      upsChangedConsumer.add(upsConsumer);
    }
  }

  @Override
  public void registerForUpdate(final IUpdateable updatable) {
    if (!updatables.contains(updatable)) {
      updatables.add(updatable);
    }
  }

  @Override
  public void start() {
    this.gameLoop.start();
    this.renderLoop.start();
  }

  @Override
  public void terminate() {
    this.gameLoop.terminate();
    this.renderLoop.terminate();
  }

  @Override
  public void unregisterFromUpdate(final IUpdateable updatable) {
    if (updatables.contains(updatable)) {
      updatables.remove(updatable);
    }
  }

  protected GameConfiguration createConfiguration() {
    return new GameConfiguration();
  }

  protected ScreenManager createScreenManager(final String gameTitle) {
    return new ScreenManager(gameTitle);
  }

  /**
   * The Class GameLoop.
   */
  private class GameLoop extends Thread {

    /** The game is running. */
    private boolean gameIsRunning = true;

    /** The next game tick. */
    private long nextGameTick = System.currentTimeMillis();

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      while (this.gameIsRunning) {
        final int SKIP_TICKS = 1000 / Game.updateRate;

        if (System.currentTimeMillis() > this.nextGameTick) {
          Game.update();
          this.nextGameTick += SKIP_TICKS;
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
        final int SKIP_FRAMES = 1000 / Game.this.getConfiguration().CLIENT.getMaxFps();

        if (System.currentTimeMillis() > this.nextRenderTick) {
          Game.this.getGraphicsEngine().getCamera().updateFocus();
          Game.this.getScreenManager().renderCurrentScreen();
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
