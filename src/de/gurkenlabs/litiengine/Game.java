package de.gurkenlabs.litiengine;

import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.environment.EnvironmentLoadedListener;
import de.gurkenlabs.litiengine.environment.EnvironmentUnloadedListener;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;
import de.gurkenlabs.litiengine.graphics.Camera;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.gui.screens.IScreenManager;
import de.gurkenlabs.litiengine.gui.screens.ScreenManager;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.input.Input.InputGameAdapter;
import de.gurkenlabs.litiengine.physics.IPhysicsEngine;
import de.gurkenlabs.litiengine.physics.PhysicsEngine;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.ISoundEngine;
import de.gurkenlabs.litiengine.sound.SoundEngine;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

/***
 * <p>
 * The <code>Game</code> class is without any doubt one of the classes that you will call a lot when creating a game with the LITIengine.
 * It is designed to be the static container that provides access to all important aspects of the engine, e.g. it holds the <code>GameInfo</code>,
 * the <code>RenderEngine</code>, the <code>SoundEngine</code> and many other major components.
 * </p>
 * <p>
 * We designed the API such that all important parts that make up the game are directly accessible via the <code>Game</code> class in a static manner.
 * To be a little bit more technical, it is essentially a collection of core Singleton instances.
 * </p>
 * <p>
 * This class will also be your starting point when setting up a new LITIengine project. In order to launch your game,
 * you need to at least call {@link Game#init(String...)} and {@link Game#start()} from your programs <code>main(String[])</code> method.
 * </p>
 * <p>
 * Additionally, it provides an interface to hook up event listeners (e.g. <code>GameListener</code> or <code>EnvironmentLoadedListener</code>) for
 * the most basic operations of a Game life cycle. 
 * </p>
 * 
 * @see GameListener
 * @see GameTerminatedListener
 * @see EnvironmentLoadedListener
 */
public final class Game {
  public static final int EXIT_GAME_CLOSED = 0;
  public static final int EXIT_GAME_CRASHED = -1;

  public static final String COMMADLINE_ARG_RELEASE = "-release";
  public static final String COMMADLINE_ARG_NOGUI = "-nogui";

  protected static long environmentLoadTick;
  private static final Logger log = Logger.getLogger(Game.class.getName());
  private static final String LOGGING_CONFIG_FILE = "logging.properties";

  private static boolean debug = true;
  private static boolean noGUIMode = false;
  private static final List<EnvironmentLoadedListener> environmentLoadedListeners;
  private static final List<EnvironmentUnloadedListener> environmentUnloadedListeners;
  private static final List<GameListener> gameListeners;
  private static final List<GameTerminatedListener> gameTerminatedListeners;

  private static final GameConfiguration configuration;
  private static final RenderEngine graphicsEngine;
  private static final SoundEngine soundEngine;
  private static final IPhysicsEngine physicsEngine;

  private static final GameMetrics metrics;

  private static final GameTime gameTime;

  private static GameInfo gameInfo;
  private static IEnvironment environment;
  private static ICamera camera;
  private static GameLoop gameLoop;
  private static RenderLoop renderLoop;
  private static IScreenManager screenManager;

  private static boolean hasStarted;
  private static boolean initialized;

  static {
    environmentLoadedListeners = new CopyOnWriteArrayList<>();
    environmentUnloadedListeners = new CopyOnWriteArrayList<>();
    gameListeners = new CopyOnWriteArrayList<>();
    gameTerminatedListeners = new CopyOnWriteArrayList<>();

    graphicsEngine = new RenderEngine();
    physicsEngine = new PhysicsEngine();
    soundEngine = new SoundEngine();
    metrics = new GameMetrics();
    gameInfo = new GameInfo();

    gameTime = new GameTime();

    // init configuration before init method in order to use configured values
    // to initialize components
    configuration = new GameConfiguration();

    addGameListener(new InputGameAdapter());
  }

  private Game() {
    throw new UnsupportedOperationException();
  }

  public static void addGameListener(GameListener listener) {
    gameListeners.add(listener);
    gameTerminatedListeners.add(listener);
  }

  public static void removeGameListener(GameListener listener) {
    gameListeners.remove(listener);
    gameTerminatedListeners.remove(listener);
  }

  public static void addGameTerminatedListener(GameTerminatedListener listener) {
    gameTerminatedListeners.add(listener);
  }

  public static void removeGameTerminatedListener(GameTerminatedListener listener) {
    gameTerminatedListeners.remove(listener);
  }

  public static void addEnvironmentLoadedListener(EnvironmentLoadedListener listener) {
    environmentLoadedListeners.add(listener);
  }

  public static void removeEnvironmentLoadedListener(EnvironmentLoadedListener listener) {
    environmentLoadedListeners.remove(listener);
  }

  public static void addEnvironmentUnloadedListener(EnvironmentUnloadedListener listener) {
    environmentUnloadedListeners.add(listener);
  }

  public static void removeEnvironmentUnloadedListener(EnvironmentUnloadedListener listener) {
    environmentUnloadedListeners.remove(listener);
  }

  /**
   * This flag indicates if the game currently supports debugging. This should
   * be set to false for release builds.
   * 
   * The default value here is true and will allow debugging unless explicitly
   * disabled by calling this method or providing the command line argument {@link #COMMADLINE_ARG_RELEASE} when running the game.
   * 
   * @param allow
   *          If set to true, the game will be told to allow debugging.
   */
  public static void allowDebug(boolean allow) {
    debug = allow;
  }

  /**

   * This flag indicates whether the game should display the <code>ScreenManager</code> or not.
   * This can only be set before the game has been initialized with the <code>Game.init(String...)</code> method. Afterwards it doesn't have an effect
   * anymore.
   * If enabled, the <code>ScreenManager#setVisible(boolean)</code> method won't be set to true and the <code>RenderLoop</code> won't be started.
   * Also the <code>Camera</code> won't be updated.
   * 
   * @param noGui
   *          If set to true, the GUI will be hidden.
   * @see ScreenManager
   * @see Game#init(String...)
   * @see ScreenManager#setVisible(boolean)
   * @see RenderLoop
   * @see Camera
   */
  public static void hideGUI(boolean noGui) {
    noGUIMode = noGui;
  }

  /**
   * This flag globally controls the game's debugging state. If enabled, debugging functionality (e.g. rendering collision boxes)
   * can potentially be enabled in the configuration.
   * 
   * @return True if debugging functionality is enabled; otherwise false.
   * 
   * @see Game#allowDebug(boolean)
   * @see GameConfiguration#debug()
   */
  public static boolean isDebug() {
    return debug;
  }

  public static boolean isInNoGUIMode() {
    return noGUIMode;
  }

  public static GameConfiguration getConfiguration() {
    return configuration;
  }

  public static IEnvironment getEnvironment() {
    return environment;
  }

  /**
   * Gets the basic meta information about this game.<br>
   * This instance can be used to define meta information about your game, like it's name, version or web site.<br>
   * <br>

   * <i>It's also possible to provide additional custom information using the method group of<br>
   * <code>Game.getInfo().setValue("CUSTOM_STRING", "my-value")</code>.</i>
   * 
   * @return The game's basic meta information.
   * 
   * @see GameInfo
   * @see ICustomPropertyProvider
   * @see GameInfo#setName(String)
   * @see GameInfo#setValue(String, String)
   */
  public static GameInfo getInfo() {
    return gameInfo;
  }

  public static IGameLoop getLoop() {
    return gameLoop;
  }

  public static GameMetrics getMetrics() {
    return metrics;
  }

  public static IPhysicsEngine getPhysicsEngine() {
    return physicsEngine;
  }

  public static RenderEngine getRenderEngine() {
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

  public static boolean hasStarted() {
    return hasStarted;
  }

  /***
   * Initializes the infrastructure of the LITIengine game.
   * 
   * The following tasks are carried out by this method:
   * <ul>
   * <li>load the <code>GameConfiguration</code></li>
   * <li>handle the specified program parameters</li>
   * <li>configure the logging</li>
   * <li>set the programs <code>Locale</code> according to the configured values.</li>
   * <li>initialize and attach core components like the <code>PhysicsEngine</code></li>
   * <li>initialize the <code>ScreenManger</code></li>
   * <li>initialize the <code>Input</code></li>
   * <li>initialize the <code>GameLoop</code> and <code>RenderLoop</code></li>
   * <li>set a default camera</li>
   * </ul>
   * 
   * @param args
   *          The arguments passed to the programs entry point.
   */
  public static void init(String... args) {
    if (initialized) {
      log.log(Level.INFO, "The game has already been initialized.");
      return;
    }

    handleCommandLineArguments(args);

    getConfiguration().load();
    Locale.setDefault(new Locale(getConfiguration().client().getCountry(), getConfiguration().client().getLanguage()));

    gameLoop = new GameLoop("Main Update Loop", getConfiguration().client().getUpdaterate());
    gameLoop.attach(getPhysicsEngine());

    final ScreenManager scrMgr = new ScreenManager(getInfo().getTitle());

    // setup default exception handling for render and update loop
    renderLoop = new RenderLoop("Render Loop");

    setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());

    screenManager = scrMgr;

    // init screens
    getScreenManager().init(getConfiguration().graphics().getResolutionWidth(), getConfiguration().graphics().getResolutionHeight(), getConfiguration().graphics().isFullscreen());
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

    for (GameListener listener : gameListeners) {
      listener.initialized(args);
    }

    if (!isInNoGUIMode()) {
      if (getConfiguration().client().showGameMetrics()) {
        getScreenManager().getRenderComponent().onRendered(g -> getMetrics().render(g));
      }

      if (getConfiguration().debug().isDebugEnabled()) {
        getRenderEngine().onEntityRendered(e -> DebugRenderer.renderEntityDebugInfo(e.getGraphics(), e.getRenderedObject()));
      }

      getScreenManager().getRenderComponent().onFpsChanged(fps -> getMetrics().setFramesPerSecond(fps));
      getScreenManager().setIconImage(Resources.images().get("litiengine-icon.png"));

      // init mouse inputs
      getScreenManager().getRenderComponent().addMouseListener(Input.mouse());
      getScreenManager().getRenderComponent().addMouseMotionListener(Input.mouse());
      getScreenManager().getRenderComponent().addMouseWheelListener(Input.mouse());

      Input.keyboard().onKeyTyped(KeyEvent.VK_PRINTSCREEN, key -> getScreenManager().getRenderComponent().takeScreenshot());
    }

    Runtime.getRuntime().addShutdownHook(new Thread(Game::terminate, "Shutdown"));

    initialized = true;
  }

  public static void setUncaughtExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {
    gameLoop.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    renderLoop.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
  }

  public static void unloadEnvironment() {
    if (getEnvironment() != null) {
      getEnvironment().unload();
      for (final EnvironmentUnloadedListener listener : environmentUnloadedListeners) {
        listener.environmentUnloaded(getEnvironment());
      }
    }
  }

  public static void loadEnvironment(final IEnvironment env) {
    unloadEnvironment();

    environment = env;
    if (getEnvironment() != null) {
      getEnvironment().load();
    }

    for (final EnvironmentLoadedListener listener : environmentLoadedListeners) {
      listener.environmentLoaded(getEnvironment());
    }

    if (getLoop() != null) {
      environmentLoadTick = getLoop().getTicks();
    }
  }

  /***
   * <p>
   * Starts the <code>GameLoops</code> and other components.
   * After this method is called, the engine will start to render contents of the current <code>Screen</code> of the <code>ScreenManager</code>,
   * the <code>SoundEngine</code> will start to playback <code>Sounds</code>
   * and the different input devices (e.g. <code>Mouse</code>, <code>Keyboard</code>) will start to process player input.
   * </p>
   * <p>
   * When the <code>Game</code> has started up successfully, it'll callback to the registered <code>GameListeners</code>.
   * </p>
   * 
   * @see ScreenManager#getCurrentScreen()
   * @see SoundEngine
   * @see Input
   * @see GameListener#started()
   */
  public static void start() {
    if (!initialized) {
      throw new IllegalStateException("The game cannot be started without being first initialized. Call Game.init(...) before Game.start().");
    }

    gameLoop.start();

    soundEngine.start();

    if (!isInNoGUIMode()) {
      renderLoop.start();
    }

    for (final GameListener listener : gameListeners) {
      listener.started();
    }

    hasStarted = true;
  }

  private static void terminate() {
    for (final GameListener listener : gameListeners) {
      if (!listener.terminating()) {
        return;
      }
    }

    getConfiguration().save();
    gameLoop.terminate();

    soundEngine.terminate();
    if (!isInNoGUIMode()) {
      renderLoop.terminate();
    }

    for (final GameTerminatedListener listener : gameTerminatedListeners) {
      listener.terminated();
    }

    hasStarted = false;
    initialized = false;
  }

  public static void setCamera(final ICamera cam) {
    if (getCamera() != null) {
      Game.getLoop().detach(camera);
    }

    camera = cam;

    if (!isInNoGUIMode()) {
      Game.getLoop().attach(cam);
      getCamera().updateFocus();
    }
  }

  /**
   * Sets the <code>Game's</code> basic information by the specified <code>GameInfo</code> instance.
   * <p>
   * <i>Typically, this should not be called manually because the <code>Game</code> already provides a <code>GameInfo</code> object which can be
   * adjusted.<br>
   * If you just want to edit some of it's information, use the provided instance of {@link Game#getInfo()}.
   * </i>
   * </p>
   * 
   * @param info
   *          The <code>GameInfo</code> that contains the basic information for the game.
   * 
   * @see Game#getInfo()
   * @see GameInfo
   */
  public static void setInfo(final GameInfo info) {
    gameInfo = info;
  }

  /**
   * Sets the <code>Game's</code> basic information by loading the <code>GameInfo</code> from the specified path to an XML file.
   * 
   * @param gameInfoFile
   *          The path to the XML file that contains the serialized <code>GameInfo</code>.
   * 
   * @see Game#setInfo(GameInfo)
   * @see Game#getInfo()
   * @see GameInfo
   */
  public static void setInfo(final String gameInfoFile) {
    GameInfo info;
    try {
      info = XmlUtilities.readFromFile(GameInfo.class, gameInfoFile);
    } catch (JAXBException e) {
      log.log(Level.WARNING, "Could not read game info from {0}", new Object[] { gameInfoFile });
      setInfo((GameInfo)null);
      return;
    }

    setInfo(info);
  }

  private static void handleCommandLineArguments(String[] args) {
    if (args == null || args.length == 0) {
      return;
    }

    if (ArrayUtilities.containsArgument(args, COMMADLINE_ARG_RELEASE)) {
      allowDebug(false);
    }

    if (ArrayUtilities.containsArgument(args, COMMADLINE_ARG_NOGUI)) {
      hideGUI(true);
    }
  }
}