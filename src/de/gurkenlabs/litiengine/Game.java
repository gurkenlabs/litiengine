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

import de.gurkenlabs.litiengine.configuration.ClientConfiguration;
import de.gurkenlabs.litiengine.configuration.DebugConfiguration;
import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.configuration.GraphicConfiguration;
import de.gurkenlabs.litiengine.configuration.InputConfiguration;
import de.gurkenlabs.litiengine.configuration.SoundConfiguration;
import de.gurkenlabs.litiengine.environment.EnvironmentLoadedListener;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;
import de.gurkenlabs.litiengine.graphics.Camera;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.GameWindow;
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
  private static ScreenManager screenManager;

  private static boolean hasStarted;
  private static boolean initialized;

  static {
    environmentLoadedListeners = new CopyOnWriteArrayList<>();
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
   * This flag indicates whether the game should display the <code>GameWindow</code> or not.
   * This can only be set before the game has been initialized with the <code>Game.init(String...)</code> method. Afterwards it doesn't have an effect
   * anymore.
   * If enabled, the <code>GameWindow#setVisible(boolean)</code> method won't be set to true and the <code>RenderLoop</code> won't be started.
   * Also the <code>Camera</code> won't be updated.
   * 
   * @param noGui
   *          If set to true, the GUI will be hidden.
   * @see GameWindow
   * @see Game#init(String...)
   * @see GameWindow#setVisible(boolean)
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
  public static GameInfo info() {
    return gameInfo;
  }

  /**
   * Gets the game's runtime configuration.<br>
   * It contains default engine settings for the game client, graphics, audio, input and debugging.<br>
   * Additionally, it can be used to register and manage custom settings that are specific to your game.
   * <p>
   * <i>
   * Elements of this configuration are also presented in a config.properties file in the game's root directory. <br>
   * This way its possible to adjust elements without having to recompile the game.
   * </i>
   * </p>
   * 
   * @return The game's runtime configuration.
   * 
   * @see SoundConfiguration
   * @see GraphicConfiguration
   * @see ClientConfiguration
   * @see DebugConfiguration
   * @see InputConfiguration
   */
  public static GameConfiguration config() {
    return configuration;
  }

  /**
   * Gets basic client metrics about the game's runtime.
   * This includes information about network, the frames-per-second or the updates-per-second and the used memory.
   * 
   * <p>
   * <i>
   * This information can be rendered by setting <br>
   * <code>Game.config().client().setShowGameMetrics(boolean)</code> to true or <br>
   * <code>cl_showGameMetrics=true</code> in the config.settings.
   * </i>
   * </p>
   * 
   * @return Metrics about the game's runtime.
   * 
   * @see GameMetrics#getFramesPerSecond()
   * @see ClientConfiguration#setShowGameMetrics(boolean)
   */
  public static GameMetrics metrics() {
    return metrics;
  }

  public static GameTime time() {
    return gameTime;
  }

  /**
   * 
   * @return
   */
  public static GameWindow window() {
    return screenManager;
  }

  public static ISoundEngine audio() {
    return soundEngine;
  }

  public static IPhysicsEngine physics() {
    return physicsEngine;
  }

  public static RenderEngine graphics() {
    return graphicsEngine;
  }

  public static IGameLoop loop() {
    return gameLoop;
  }

  public static RenderLoop renderLoop() {
    return renderLoop;
  }

  public static IScreenManager screens() {
    return screenManager;
  }

  public static ICamera getCamera() {
    return camera;
  }

  public static IEnvironment getEnvironment() {
    return environment;
  }

  public static void loadEnvironment(final IEnvironment env) {
    if (getEnvironment() != null) {
      getEnvironment().unload();
    }

    environment = env;
    if (getEnvironment() != null) {
      getEnvironment().load();
    }

    for (final EnvironmentLoadedListener listener : environmentLoadedListeners) {
      listener.environmentLoaded(getEnvironment());
    }

    if (loop() != null) {
      environmentLoadTick = loop().getTicks();
    }
  }

  public static void addEnvironmentLoadedListener(EnvironmentLoadedListener listener) {
    environmentLoadedListeners.add(listener);
  }

  public static void removeEnvironmentLoadedListener(EnvironmentLoadedListener listener) {
    environmentLoadedListeners.remove(listener);
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

    config().load();
    Locale.setDefault(new Locale(config().client().getCountry(), config().client().getLanguage()));

    gameLoop = new GameLoop("Main Update Loop", config().client().getUpdaterate());
    gameLoop.attach(physics());
    gameLoop.attach(metrics());

    final ScreenManager scrMgr = new ScreenManager(info().getTitle());

    // setup default exception handling for render and update loop
    renderLoop = new RenderLoop("Render Loop");

    setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());

    screenManager = scrMgr;

    // initialize  the game window
    window().init();
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
      if (config().client().showGameMetrics()) {
        window().getRenderComponent().onRendered(g -> metrics().render(g));
      }

      if (config().debug().isDebugEnabled()) {
        graphics().onEntityRendered(e -> DebugRenderer.renderEntityDebugInfo(e.getGraphics(), e.getRenderedObject()));
      }

      window().getRenderComponent().onFpsChanged(fps -> metrics().setFramesPerSecond(fps));
      window().setIconImage(Resources.images().get("litiengine-icon.png"));

      // init mouse inputs
      window().getRenderComponent().addMouseListener(Input.mouse());
      window().getRenderComponent().addMouseMotionListener(Input.mouse());
      window().getRenderComponent().addMouseWheelListener(Input.mouse());

      Input.keyboard().onKeyTyped(KeyEvent.VK_PRINTSCREEN, key -> window().getRenderComponent().takeScreenshot());
    }

    Runtime.getRuntime().addShutdownHook(new Thread(Game::terminate, "Shutdown"));

    initialized = true;
  }

  public static void setUncaughtExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {
    gameLoop.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    renderLoop.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
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

    config().save();
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
      Game.loop().detach(camera);
    }

    camera = cam;

    if (!isInNoGUIMode()) {
      Game.loop().attach(cam);
      getCamera().updateFocus();
    }
  }

  /**
   * Sets the <code>Game's</code> basic information by the specified <code>GameInfo</code> instance.
   * <p>
   * <i>Typically, this should not be called manually because the <code>Game</code> already provides a <code>GameInfo</code> object which can be
   * adjusted.<br>
   * If you just want to edit some of it's information, use the provided instance of {@link Game#info()}.
   * </i>
   * </p>
   * 
   * @param info
   *          The <code>GameInfo</code> that contains the basic information for the game.
   * 
   * @see Game#info()
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
   * @see Game#info()
   * @see GameInfo
   */
  public static void setInfo(final String gameInfoFile) {
    GameInfo info = XmlUtilities.readFromFile(GameInfo.class, gameInfoFile);
    if (info == null) {
      log.log(Level.WARNING, "Could not read game info from {0}", new Object[] { gameInfoFile });
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