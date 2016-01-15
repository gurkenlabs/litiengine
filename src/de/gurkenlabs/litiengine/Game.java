package de.gurkenlabs.litiengine;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.annotation.AnnotationFormatError;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.annotation.GameInfo;
import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.core.IUpdateable;
import de.gurkenlabs.litiengine.gui.screens.IScreenManager;
import de.gurkenlabs.litiengine.gui.screens.ScreenManager;
import de.gurkenlabs.litiengine.input.Input;

public abstract class Game implements IGame {
  private final List<IUpdateable> updatables;
  private final List<Consumer<Integer>> upsChangedConsumer;

  private final GameInfo info;
  private GameConfiguration configuration;
  private final IScreenManager screenManager;

  private final RenderLoop renderLoop;
  private final GameLoop gameLoop;

  private int updateCount;
  private long lastUpsTime;

  private long gameTicks;

  protected Game() {
    GameInfo info = this.getClass().getAnnotation(GameInfo.class);
    if (info == null) {
      throw new AnnotationFormatError("No GameInfo annotation found on game implementation " + this.getClass());
    }
    this.updatables = new CopyOnWriteArrayList<>();
    this.upsChangedConsumer = new CopyOnWriteArrayList<>();

    this.info = info;
    ScreenManager screenManager = this.createScreenManager(this.getInfo().name() + " " +this.getInfo().version());

    // ensures that we terminate the game, when the window is closed
    screenManager.addWindowListener(new WindowHandler());
    this.screenManager = screenManager;

    this.renderLoop = new RenderLoop();
    this.gameLoop = new GameLoop();
    
    // init configuration before init method in order to use configured values to initialize components
    this.configuration = this.createConfiguration();
    this.getConfiguration().load();
  }

  protected GameConfiguration createConfiguration() {
    return new GameConfiguration();
  }

  protected ScreenManager createScreenManager(String gameTitle) {
    return new ScreenManager(gameTitle);
  }

  @Override
  public GameInfo getInfo() {
    return this.info;
  }

  @Override
  public long getTicks() {
    return this.gameTicks;
  }

  @Override
  public void registerForUpdate(IUpdateable updatable) {
    if (!this.updatables.contains(updatable)) {
      this.updatables.add(updatable);
    }
  }

  @Override
  public void unregisterFromUpdate(IUpdateable updatable) {
    if (this.updatables.contains(updatable)) {
      this.updatables.remove(updatable);
    }
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
  public IScreenManager getScreenManager() {
    return this.screenManager;
  }

  @Override
  public GameConfiguration getConfiguration() {
    return this.configuration;
  }

  @Override
  public void onUpsChanged(Consumer<Integer> upsConsumer) {
    if (!this.upsChangedConsumer.contains(upsConsumer)) {
      this.upsChangedConsumer.add(upsConsumer);
    }
  }

  /**
   * Update.
   */
  private void update() {
    ++this.gameTicks;
    this.updatables.forEach(updatable -> updatable.update());

    this.updateCount++;
    final long currentMillis = System.currentTimeMillis();
    if (currentMillis - this.lastUpsTime >= 1000) {
      this.lastUpsTime = currentMillis;
      this.upsChangedConsumer.forEach(consumer -> consumer.accept(this.updateCount));
      this.updateCount = 0;
    }
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
        final int SKIP_TICKS = 1000 / Game.this.getConfiguration().CLIENT.getUpdaterate();

        if (System.currentTimeMillis() > this.nextGameTick) {
          Game.this.update();
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
