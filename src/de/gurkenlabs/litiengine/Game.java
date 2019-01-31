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

import de.gurkenlabs.litiengine.configuration.ClientConfiguration;
import de.gurkenlabs.litiengine.configuration.DebugConfiguration;
import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.configuration.GraphicConfiguration;
import de.gurkenlabs.litiengine.configuration.InputConfiguration;
import de.gurkenlabs.litiengine.configuration.SoundConfiguration;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.EnvironmentLoadedListener;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider;
import de.gurkenlabs.litiengine.graphics.Camera;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.GameWindow;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.RenderComponent;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.gui.screens.ScreenManager;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.input.Input.InputGameAdapter;
import de.gurkenlabs.litiengine.physics.PhysicsEngine;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.ISoundPlayback;
import de.gurkenlabs.litiengine.sound.Sound;
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

  private static final Logger log = Logger.getLogger(Game.class.getName());
  private static final String LOGGING_CONFIG_FILE = "logging.properties";

  private static boolean debug = true;
  private static boolean noGUIMode = false;
  private static final List<GameListener> gameListeners;
  private static final List<GameTerminatedListener> gameTerminatedListeners;

  private static final RenderEngine graphicsEngine;
  private static final SoundEngine soundEngine;
  private static final PhysicsEngine physicsEngine;

  private static final GameConfiguration configuration;
  private static final GameMetrics metrics;
  private static final GameTime gameTime;
  private static GameInfo gameInfo;

  private static GameLoop gameLoop;
  private static RenderLoop renderLoop;
  private static GameLoop inputLoop;
  private static ScreenManager screenManager;
  private static GameWindow gameWindow;
  private static GameWorld world;

  private static boolean hasStarted;
  private static boolean initialized;

  static {
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
    world = new GameWorld();
    world.addLoadedListener(gameTime);

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
   * If enabled, the <code>ScreenManager#setVisible(boolean)</code> method won't be set to true and the <code>RenderLoop</code> won't be started.
   * Also the <code>Camera</code> won't be updated.
   * 
   * @param noGui
   *          If set to true, the GUI will be hidden.
   * @see GameWindow
   * @see Game#init(String...)
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
   * Gets the static meta information about this game.<br>
   * This can be used to define meta information about your game, like it's name, version or web site.<br>
   * <br>
   * <i>It's also possible to provide additional custom information using the method group <br>
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
   * Gets the game's window in which the <code>RenderComponent</code> lives.<br>
   * This class e.g. provides the possibility to set a title, provide an icon or get information about the resolution.
   * 
   * @return The window that hosts the game's <code>RenderComponent</code>.
   * 
   * @see RenderComponent
   * @see GameWindow#getResolution()
   * @see GameWindow#setTitle(String)
   * @see GameWindow#setIconImage(java.awt.Image)
   */
  public static GameWindow window() {
    return gameWindow;
  }

  /**
   * Gets the engine's <code>SoundEngine</code> component that can be used to play sounds and music.<br>
   * Sound can be loaded and accessed using the <code>Resources</code> API and are managed by the<br>
   * <code>Resources.sounds()</code> resource container.
   * 
   * <p>
   * <i>
   * Upon playing a sound, the engine returns an <code>ISoundPlayback</code> instance that can then be used to further control the audio line.
   * </i>
   * </p>
   * 
   * @return The engine's <code>SoundEngine</code> component.
   * 
   * @see Sound
   * @see Resources#sounds()
   * @see ISoundPlayback
   * @see SoundEngine#playSound(de.gurkenlabs.litiengine.sound.Sound)
   * @see SoundEngine#playMusic(de.gurkenlabs.litiengine.sound.Sound)
   */
  public static SoundEngine audio() {
    return soundEngine;
  }

  /**
   * Gets the engine's <code>PhysicsEngine</code> component that can be used to detect and resolve collision and move entities with respect to all
   * collision
   * entities on the environment.<br>
   * The boundaries of the loaded environment also pose a "non-walkable" area that will be taken into account when moving entities with this engine.
   * 
   * <p>
   * <i>It is also possible to manually register static collision <code>Rectangles</code> that can further restrict the game world.</i>
   * </p>
   * 
   * @return The engine's <code>PhysicsEngine</code> component.
   * 
   * @see PhysicsEngine
   * @see PhysicsEngine#move(IMobileEntity, float)
   * @see ICollisionEntity
   */
  public static PhysicsEngine physics() {
    return physicsEngine;
  }

  /**
   * Gets the engine's <code>RenderEngine</code> component that is used to render <code>Images, Shapes or Text</code> with respect to the environment
   * and the render scale and the <code>Camera</code>.
   * 
   * <p>
   * <i>In case you want to render something in a static manner that is unrelated to the environment, you can use the engine's different static
   * <code>Renderer</code> implementations.</i>
   * </p>
   * 
   * @return The engine's <code>RenderEngine</code> component.
   * 
   * @see RenderEngine#getBaseRenderScale()
   * @see TextRenderer
   * @see ShapeRenderer
   * @see ImageRenderer
   */
  public static RenderEngine graphics() {
    return graphicsEngine;
  }

  /**
   * Gets the game's main loop that is used to execute and manage all game logic apart from rendering and input processing.<br>
   * You can attach any <code>Updatable</code> instance to this loop if you want to execute custom game logic that is executed at the configured
   * updaterate.
   * 
   * <p>
   * <i>The LITIengine has separate loops for game logic, rendering and input processing. <br>
   * This prevents them from interfering with each other and also properly separates tasks by their category.</i>
   * </p>
   * 
   * 
   * @return The game's main loop.
   *
   * @see ClientConfiguration#getUpdaterate()
   * @see IUpdateable
   * @see ILoop#attach(IUpdateable)
   * @see ILoop#detach(IUpdateable)
   * @see Game#inputLoop()
   * @see Game#renderLoop()
   */
  public static IGameLoop loop() {
    return gameLoop;
  }

  /**
   * Gets the game's input loop that processes all the player input.
   * 
   * <p>
   * <i>We need an own update loop because otherwise input won't work if the game has been paused.</i>
   * </p>
   * 
   * @return The game's input loop.
   */
  public static IGameLoop inputLoop() {
    return inputLoop;
  }

  /**
   * Gets the game's loop that executes the rendering process on the GameFrame's <code>RenderComponent</code>.<br>
   * This internally renders the currently active screen which passes the <code>Graphics2D</code> object to all <code>GuiComponents</code> and the
   * Environment for rendering. This loop will try to execute at the configured frames-per-second and limit the frames to this value.
   * 
   * <p>
   * <i>It's also possible to register <code>Updatable</code> instances to this loop which is useful if you want to execute something that is directly
   * related to
   * the rendering process and needs to be executed right before the game's rendering starts.</i>
   * </p>
   * 
   * @return The game's render loop.
   * 
   * @see ClientConfiguration#getMaxFps()
   * @see RenderComponent#render()
   * @see Screen#render(java.awt.Graphics2D)
   * @see GuiComponent#render(java.awt.Graphics2D)
   * @see Environment#render(java.awt.Graphics2D)
   */
  public static RenderLoop renderLoop() {
    return renderLoop;
  }

  /**
   * Gets the game's <code>ScreenManager</code> that is responsible for organizing all <code>Screens</code> of your game and providing the currently
   * active <code>Screen</code> that is used to render the current <code>Environment</code>.<br>
   * Screens are the containers that allow you to organize the visible contents of your game and are identified and addressed by a unique name.
   * 
   * <p>
   * <i>Examples: Menu Screen, Credits Screen, Game Screen, Inventory Screen</i>
   * </p>
   * 
   * @return The game's screen manager.
   * 
   * @see Screen
   * @see GameWorld#environment()
   * @see Game#world()
   */
  public static ScreenManager screens() {
    return screenManager;
  }

  /**
   * Gets the game's world which is a global environment manager that contains all <code>Environments</code>
   * and provides the currently active <code>Environment</code> and
   * <code>Camera</code>.<br>
   * <p>
   * The <code>GameWorld</code> returns the same instance for a particular map/mapName until the
   * <code>GameWorld.reset(String)</code> method is called.
   * </p>
   * 
   * Moreover, it provides the possibility to attach game logic via <code>EnvironmentListeners</code> to different events of the
   * <code>Envrionment's</code> life cycle (e.g. loaded, initialized, ...).<br>
   * <i>This is typically used to provide some per-level logic or to trigger
   * general loading behavior.</i>
   * 
   * @return The game's environment manager.
   * 
   * @see GameWorld
   * @see Environment
   * @see Camera
   * @see GameWorld#environment()
   * @see GameWorld#camera()
   */
  public static GameWorld world() {
    return world;
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
  public static synchronized void init(String... args) {
    if (initialized) {
      log.log(Level.INFO, "The game has already been initialized.");
      return;
    }

    handleCommandLineArguments(args);

    config().load();
    Locale.setDefault(new Locale(config().client().getCountry(), config().client().getLanguage()));

    gameLoop = new GameLoop("Main Update Loop", config().client().getUpdaterate());
    loop().attach(physics());
    loop().attach(world());

    final ScreenManager scrMgr = new ScreenManager();

    // setup default exception handling for render and update loop
    renderLoop = new RenderLoop("Render Loop");
    inputLoop = new GameLoop("Input Loop", loop().getUpdateRate());

    setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(config().client().exitOnError()));

    screenManager = scrMgr;
    gameWindow = new GameWindow();

    // initialize  the game window
    window().init();
    world.setCamera(new Camera());

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
   * @see ScreenManager#current()
   * @see SoundEngine
   * @see Input
   * @see GameListener#started()
   */
  public static synchronized void start() {
    if (!initialized) {
      throw new IllegalStateException("The game cannot be started without being first initialized. Call Game.init(...) before Game.start().");
    }

    gameLoop.start();
    inputLoop.start();

    if (!isInNoGUIMode()) {
      renderLoop.start();
    }

    soundEngine.start();

    for (final GameListener listener : gameListeners) {
      listener.started();
    }

    hasStarted = true;
  }

  /**
   * This method <b>should not be called manually</b>. <br>
   * It's internally called when the main application shuts down.
   */
  public static synchronized void terminate() {
    for (final GameListener listener : gameListeners) {
      if (!listener.terminating()) {
        return;
      }
    }

    hasStarted = false;
    initialized = false;

    config().save();
    gameLoop.terminate();
    inputLoop.terminate();

    soundEngine.terminate();

    world().clear();
    if (!isInNoGUIMode()) {
      renderLoop.terminate();
    }

    for (final GameTerminatedListener listener : gameTerminatedListeners) {
      listener.terminated();
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
    GameInfo info;
    try {
      info = XmlUtilities.readFromFile(GameInfo.class, gameInfoFile);
    } catch (JAXBException e) {
      log.log(Level.WARNING, "Could not read game info from {0}", new Object[] { gameInfoFile });
      setInfo((GameInfo) null);
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
