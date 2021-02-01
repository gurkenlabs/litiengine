package com.litiengine;

import java.awt.event.KeyEvent;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.litiengine.configuration.ClientConfiguration;
import com.litiengine.configuration.DebugConfiguration;
import com.litiengine.configuration.GameConfiguration;
import com.litiengine.configuration.GraphicConfiguration;
import com.litiengine.configuration.InputConfiguration;
import com.litiengine.configuration.SoundConfiguration;
import com.litiengine.entities.ICollisionEntity;
import com.litiengine.entities.IMobileEntity;
import com.litiengine.environment.Environment;
import com.litiengine.environment.GameWorld;
import com.litiengine.environment.tilemap.ICustomPropertyProvider;
import com.litiengine.graphics.Camera;
import com.litiengine.graphics.DebugRenderer;
import com.litiengine.graphics.ImageRenderer;
import com.litiengine.graphics.RenderComponent;
import com.litiengine.graphics.RenderEngine;
import com.litiengine.graphics.ShapeRenderer;
import com.litiengine.graphics.TextRenderer;
import com.litiengine.gui.screens.Screen;
import com.litiengine.gui.screens.ScreenManager;
import com.litiengine.input.Input;
import com.litiengine.input.Input.InputGameAdapter;
import com.litiengine.physics.PhysicsEngine;
import com.litiengine.resources.Resources;
import com.litiengine.sound.Sound;
import com.litiengine.sound.SoundEngine;
import com.litiengine.sound.SoundPlayback;
import com.litiengine.tweening.TweenEngine;
import com.litiengine.util.ArrayUtilities;
import com.litiengine.util.io.XmlUtilities;

/***
 * <p>
 * The {@code Game} class is without any doubt one of the classes that you will call a lot when creating a game with the LITIENGINE.
 * It is designed to be the static container that provides access to all important aspects of the engine, e.g. it holds the {@code GameInfo},
 * the {@code RenderEngine}, the {@code SoundEngine} and many other major components.
 * </p>
 * <p>
 * We designed the API such that all important parts that make up the game are directly accessible via the {@code Game} class in a static manner.
 * To be a little bit more technical, it is essentially a collection of core Singleton instances.
 * </p>
 * <p>
 * This class will also be your starting point when setting up a new LITIENGINE project. In order to launch your game,
 * you need to at least call {@link Game#init(String...)} and {@link Game#start()} from your programs {@code main(String[])} method.
 * </p>
 * <p>
 * Additionally, it provides an interface to hook up event listeners (e.g. {@code GameListener} or {@code EnvironmentLoadedListener}) for
 * the most basic operations of a Game life cycle. 
 * </p>
 * 
 * @see GameListener
 */
public final class Game {
  public static final int EXIT_GAME_CLOSED = 0;
  public static final int EXIT_GAME_CRASHED = -1;

  public static final String COMMADLINE_ARG_RELEASE = "-release";
  public static final String COMMADLINE_ARG_NOGUI = "-nogui";

  private static final List<GameListener> gameListeners = new CopyOnWriteArrayList<>();

  private static final RenderEngine graphicsEngine = new RenderEngine();
  private static final SoundEngine soundEngine = new SoundEngine();
  private static final PhysicsEngine physicsEngine = new PhysicsEngine();

  private static final GameConfiguration configuration = new GameConfiguration();
  private static final GameMetrics metrics = new GameMetrics();
  private static final GameLog log = new GameLog();
  private static final GameTime gameTime = new GameTime();
  private static final GameRandom random = new GameRandom();
  private static GameInfo gameInfo = new GameInfo();
  private static final TweenEngine tweenEngine = new TweenEngine();

  private static GameLoop gameLoop;
  private static ScreenManager screenManager;
  private static GameWindow gameWindow;

  private static GameWorld world = new GameWorld();

  private static boolean debug = true;
  private static boolean noGUIMode = false;
  private static boolean hasStarted;
  private static boolean initialized;

  static {
    world.onLoaded(gameTime);
    addGameListener(new InputGameAdapter());
  }

  private Game() {
    throw new UnsupportedOperationException();
  }

  /**
   * Adds the specified game listener to receive events about the basic game life-cycle.
   * 
   * @param listener
   *          The listener to add.
   */
  public static void addGameListener(GameListener listener) {
    gameListeners.add(listener);
  }

  /**
   * Removes the specified game listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public static void removeGameListener(GameListener listener) {
    gameListeners.remove(listener);
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
   * This flag indicates whether the game should display the {@code GameWindow} or not.
   * This can only be set before the game has been initialized with the {@code Game.init(String...)} method. Afterwards it doesn't have an effect
   * anymore.
   * If enabled, the {@code ScreenManager#setVisible(boolean)} method won't be set to true and the {@code RenderLoop} won't be started.
   * Also the {@code Camera} won't be updated.
   * 
   * @param noGui
   *          If set to true, the GUI will be hidden.
   * @see GameWindow
   * @see Game#init(String...)
   * @see Camera
   * @see #isInNoGUIMode()
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

  /**
   * Indicates whether the game should display the {@code GameWindow} or not.
   * 
   * @return True if the game should display visual components; otherwise false.
   */
  public static boolean isInNoGUIMode() {
    return noGUIMode;
  }

  /**
   * Indicates whether the game has already been started.
   * 
   * @return True if the game has been started; otherwise false.
   * 
   * @see Game#start()
   */
  public static boolean hasStarted() {
    return hasStarted;
  }

  /**
   * Gets the static meta information about this game.<br>
   * This can be used to define meta information about your game, like it's name, version or web site.<br>
   * <br>
   * <i>It's also possible to provide additional custom information using the method group <br>
   * {@code Game.getInfo().setValue("CUSTOM_STRING", "my-value")}.</i>
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
   * {@code Game.config().client().setShowGameMetrics(boolean)} to true or <br>
   * {@code cl_showGameMetrics=true} in the config.settings.
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

  /**
   * Gets time information about the running game/environment.
   * 
   * <p>
   * This allow to measure the time between actions, track how long something took, evaluate cooldowns or just get information about the played game
   * time.
   * </p>
   * 
   * @return The game's temporal information.
   * 
   * @see GameTime#now()
   */
  public static GameTime time() {
    return gameTime;
  }

  /**
   * Gets the game's window in which the {@code RenderComponent} lives.<br>
   * This class e.g. provides the possibility to set a title, provide an icon, get information about the resolution or set a cursor.
   * 
   * @return The window that hosts the game's {@code RenderComponent}.
   * 
   * @see RenderComponent
   * @see GameWindow#getResolution()
   * @see GameWindow#setTitle(String)
   * @see GameWindow#setIcon(java.awt.Image)
   * @see GameWindow#cursor()
   */
  public static GameWindow window() {
    return gameWindow;
  }

  /**
   * Gets the engine's {@code SoundEngine} component that can be used to play sounds and music.<br>
   * Sound can be loaded and accessed using the {@code Resources} API and are managed by the<br>
   * {@code Resources.sounds()} resource container.
   * 
   * <p>
   * <i>
   * Upon playing a sound, the engine returns an {@code SoundPlayback} instance that can then be used to further control the audio line.
   * </i>
   * </p>
   * 
   * @return The engine's {@code SoundEngine} component.
   * 
   * @see Sound
   * @see Resources#sounds()
   * @see SoundPlayback
   * @see SoundEngine#playSound(Sound)
   * @see SoundEngine#playMusic(Sound)
   */
  public static SoundEngine audio() {
    return soundEngine;
  }

  /**
   * Gets the engine's {@code PhysicsEngine} component that can be used to detect and resolve collision and move entities with respect to all
   * collision
   * entities on the environment.<br>
   * The boundaries of the loaded environment also pose a "non-walkable" area that will be taken into account when moving entities with this engine.
   * 
   * <p>
   * <i>It is also possible to manually register static collision {@code Rectangles} that can further restrict the game world.</i>
   * </p>
   * 
   * @return The engine's {@code PhysicsEngine} component.
   * 
   * @see PhysicsEngine
   * @see PhysicsEngine#move(IMobileEntity, float)
   * @see ICollisionEntity
   */
  public static PhysicsEngine physics() {
    return physicsEngine;
  }

  /**
   * Gets the engine's {@code RenderEngine} component that is used to render {@code Images, Shapes or Text} with respect to the environment
   * and the render scale and the {@code Camera}.
   * 
   * <p>
   * <i>In case you want to render something in a static manner that is unrelated to the environment, you can use the engine's different static
   * {@code Renderer} implementations.</i>
   * </p>
   * 
   * @return The engine's {@code RenderEngine} component.
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
   * Gets the game's main loop that is used to execute and manage all game logic apart from input processing.<br>
   * You can attach any {@code Updatable} instance to this loop if you want to execute custom game logic that is executed at the configured
   * max fps.
   * 
   * The game's loop also executes the rendering process on the GameFrame's {@code RenderComponent}.<br>
   * This internally renders the currently active screen which passes the {@code Graphics2D} object to all {@code GuiComponents} and the
   * Environment for rendering.
   * <p>
   * <i>The LITIENGINE has two separate loops for game logic/rendering and input processing. <br>
   * This prevents them from interfering with each other and to be able to process player input independent of the game's framerate.</i>
   * </p>
   * 
   * @return The game's main loop.
   *
   * @see ClientConfiguration#getMaxFps()
   * @see IUpdateable
   * @see ILoop#attach(IUpdateable)
   * @see ILoop#detach(IUpdateable)
   */
  public static IGameLoop loop() {
    return gameLoop;
  }

  /**
   * Gets the game's default logger instance that can be used to quickly log messages without the need to initialize
   * custom logger instances.
   * 
   * @return The game's default logger instance.
   */
  public static Logger log() {
    return log.log();
  }

  /**
   * Gets the game's pseudo-random generator that enhances the default Java {@code Random} implementation
   * with helpful additions.
   * 
   * @return The game's pseudo random generator.
   */
  public static GameRandom random() {
    return random;
  }

  /**
   * Gets the game's {@code ScreenManager} that is responsible for organizing all {@code Screens} of your game and providing the currently
   * active {@code Screen} that is used to render the current {@code Environment}.<br>
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
   * Gets the game's world which is a global environment manager that contains all {@code Environments}
   * and provides the currently active {@code Environment} and
   * {@code Camera}.<br>
   * <p>
   * The {@code GameWorld} returns the same instance for a particular map/mapName until the
   * {@code GameWorld.reset(String)} method is called.
   * </p>
   * 
   * Moreover, it provides the possibility to attach game logic via {@code EnvironmentListeners} to different events of the
   * {@code Envrionment's} life cycle (e.g. loaded, initialized, ...).<br>
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
   * @see GameWorld#reset(String)
   */
  public static GameWorld world() {
    return world;
  }
  
  /**
   * Gets the game's Tween manager that holds all currently active Tween instances.
   * 
   * @return The game's Tween manager.
   */
  public static TweenEngine tweens() {
    return tweenEngine;
  }

  /***
   * Initializes the infrastructure of the LITIENGINE game.
   * 
   * The following tasks are carried out by this method:
   * <ul>
   * <li>load the {@code GameConfiguration}</li>
   * <li>handle the specified program parameters</li>
   * <li>configure the logging</li>
   * <li>set the programs {@code Locale} according to the configured values.</li>
   * <li>initialize and attach core components like the {@code PhysicsEngine}</li>
   * <li>initialize the {@code ScreenManger}</li>
   * <li>initialize the {@code Input}</li>
   * <li>initialize the {@code GameLoop} and {@code RenderLoop}</li>
   * <li>set a default {@code Camera}</li>
   * </ul>
   * 
   * @param args
   *          The arguments passed to the programs entry point.
   */
  public static synchronized void init(String... args) {
    if (initialized) {
      log().log(Level.INFO, "The game has already been initialized.");
      return;
    }

    log.init();
    handleCommandLineArguments(args);

    config().load();
    Locale.setDefault(new Locale(config().client().getCountry(), config().client().getLanguage()));

    gameLoop = new GameLoop("Main Update Loop", config().client().getMaxFps());
    loop().attach(physics());
    loop().attach(world());

    // setup default exception handling for render and update loop
    setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(config().client().exitOnError()));

    screenManager = new ScreenManager();
    gameWindow = new GameWindow();

    // initialize  the game window
    window().init();
    world.setCamera(new Camera());

    for (GameListener listener : gameListeners) {
      listener.initialized(args);
    }

    if (!isInNoGUIMode()) {
      window().getRenderComponent().onRendered(g -> metrics().render(g));

      graphics().addEntityRenderedListener(e -> DebugRenderer.renderEntityDebugInfo(e.getGraphics(), e.getEntity()));

      window().getRenderComponent().onFpsChanged(fps -> metrics().setFramesPerSecond(fps));
      window().setIcons(Arrays.asList(Resources.images().get("liti-logo-x16.png"), Resources.images().get("liti-logo-x20.png"), Resources.images().get("liti-logo-x32.png"), Resources.images().get("liti-logo-x48.png")));

      Input.keyboard().onKeyTyped(KeyEvent.VK_PRINTSCREEN, key -> {
        // don't take a screenshot if a modifier is active
        if (key.getModifiers() != 0) {
          return;
        }

        window().getRenderComponent().takeScreenshot();
      });
    }

    Runtime.getRuntime().addShutdownHook(new Thread(Game::terminate, "Shutdown"));

    initialized = true;
  }

  /**
   * Sets an {@code UncaughtExceptionHandler} used to handle all unexpected exceptions happening in the game.
   * 
   * @param uncaughtExceptionHandler
   *          The handler to be used for uncaught exceptions.
   */
  public static void setUncaughtExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {
    gameLoop.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
  }

  /***
   * <p>
   * Starts the {@code GameLoops} and other components.
   * After this method is called, the engine will start to render contents of the current {@code Screen} of the {@code ScreenManager},
   * the {@code SoundEngine} will start to playback {@code Sounds}
   * and the different input devices (e.g. {@code Mouse}, {@code Keyboard}) will start to process player input.
   * </p>
   * <p>
   * When the {@code Game} has started up successfully, it'll callback to the registered {@code GameListeners}.
   * </p>
   * 
   * @see ScreenManager#current()
   * @see SoundEngine
   * @see Input
   * @see GameListener#started()
   * @see #hasStarted()
   */
  public static synchronized void start() {
    if (!initialized) {
      throw new IllegalStateException("The game cannot be started without being first initialized. Call Game.init(...) before Game.start().");
    }

    gameLoop.start();
    tweenEngine.start();
    soundEngine.start();

    for (final GameListener listener : gameListeners) {
      listener.started();
    }

    hasStarted = true;
  }

  public static void exit(){
    if (terminating()){
      System.exit(Game.EXIT_GAME_CLOSED);
    }
  }

  /**
   * Sets the {@code Game's} basic information by the specified {@code GameInfo} instance.
   * <p>
   * <i>Typically, this should not be called manually because the {@code Game} already provides a {@code GameInfo} object which can be
   * adjusted.<br>
   * If you just want to edit some of it's information, use the provided instance of {@link Game#info()}.
   * </i>
   * </p>
   * 
   * @param info
   *          The {@code GameInfo} that contains the basic information for the game.
   * 
   * @see Game#info()
   * @see GameInfo
   */
  public static void setInfo(final GameInfo info) {
    gameInfo = info;
  }

  /**
   * Sets the {@code Game's} basic information by loading the {@code GameInfo} from the specified path to an XML file.
   * 
   * @param gameInfoFile
   *          The path to the XML file that contains the serialized {@code GameInfo}.
   * 
   * @see Game#setInfo(GameInfo)
   * @see Game#info()
   * @see GameInfo
   */
  public static void setInfo(String gameInfoFile) {
    setInfo(Resources.getLocation(gameInfoFile));
  }

  public static void setInfo(final URL gameInfoFile) {
    GameInfo info;
    try {
      info = XmlUtilities.read(GameInfo.class, gameInfoFile);
    } catch (Exception e) {
      log().log(Level.WARNING, "Could not read game info from {0}", new Object[] { gameInfoFile });
      setInfo((GameInfo) null);
      return;
    }

    setInfo(info);
  }

  static synchronized boolean terminating() {
    for (final GameListener listener : gameListeners) {
      try {
        if (!listener.terminating()) {
          return false;
        }
      } catch (Exception e) {
        log().log(Level.WARNING, "game listener threw an exception while terminating", e);
      }
    }

    return true;
  }

  static synchronized void terminate() {
    if (!initialized) {
      return;
    }

    hasStarted = false;
    initialized = false;

    config().save();
    gameLoop.terminate();
    tweenEngine.terminate();
    soundEngine.terminate();

    world().clear();

    for (final GameListener listener : gameListeners) {
      try {
        listener.terminated();
      } catch (Exception e) {
        log().log(Level.WARNING, "game listener threw an exception during shutdown", e);
      }
    }

    gameLoop = null;
    screenManager = null;
    gameWindow = null;
  }

  private static void handleCommandLineArguments(String[] args) {
    if (args == null || args.length == 0) {
      return;
    }

    if (ArrayUtilities.contains(args, COMMADLINE_ARG_RELEASE, true)) {
      allowDebug(false);
    }

    if (ArrayUtilities.contains(args, COMMADLINE_ARG_NOGUI, true)) {
      hideGUI(true);
    }
  }
}