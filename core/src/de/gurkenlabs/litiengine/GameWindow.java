package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.configuration.DisplayMode;
import de.gurkenlabs.litiengine.configuration.GraphicConfiguration;
import de.gurkenlabs.litiengine.graphics.MouseCursor;
import de.gurkenlabs.litiengine.graphics.RenderComponent;
import de.gurkenlabs.litiengine.gui.screens.Resolution;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Point2D;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code GameWindow} class is a wrapper for the game's visual window in which the {@code RenderComponent} lives.<br>
 * It provides the possibility to set a title, provide an icon, configure the cursor or get information about the resolution.
 *
 * @see RenderComponent
 * @see #getResolution()
 * @see #setTitle(String)
 * @see #cursor()
 * @see #setIcon(java.awt.Image)
 */
public final class GameWindow {
  private static final Logger log = Logger.getLogger(GameWindow.class.getName());
  private static final int ICONIFIED_MAX_FPS = 1;
  private static final int NONE_FOCUS_MAX_FPS = 10;

  private final List<ResolutionChangedListener> resolutionChangedListeners;

  private final JFrame hostControl;
  private final RenderComponent renderCanvas;
  private final MouseCursor cursor;

  private float resolutionScale = 1;

  private Dimension resolution;
  private Point screenLocation;

  GameWindow() {
    this.hostControl = new JFrame();

    this.resolutionChangedListeners = new CopyOnWriteArrayList<>();

    this.renderCanvas = new RenderComponent(Game.config().graphics().getResolution());
    this.cursor = new MouseCursor();
    if (!Game.isInNoGUIMode()) {
      this.hostControl.setBackground(Color.BLACK);
      this.hostControl.add(this.renderCanvas);

      this.initializeEventListeners();

      this.hostControl.setTitle(Game.info().getTitle());
      this.hostControl.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      initializeWindowEventListeners(this.hostControl);
    }
  }

  /**
   * Returns true if the GameWindow is the focus owner.
   *
   * @return true if the GameWindow is the focus owner; false otherwise
   */
  public boolean isFocusOwner() {
    if (this.getRenderComponent() != null && this.getRenderComponent().isFocusOwner()) {
      return true;
    }

    return this.hostControl.isFocusOwner();
  }

  /**
   * Adds the specified resolution changed listener to receive events when the dimensions of this game window are changed.
   *
   * @param listener The listener to add.
   */
  public void onResolutionChanged(final ResolutionChangedListener listener) {
    this.resolutionChangedListeners.add(listener);
  }

  /**
   * Removes the specified resolution changed listener.
   *
   * @param listener The listener to remove.
   */
  public void removeResolutionChangedListener(final ResolutionChangedListener listener) {
    this.resolutionChangedListeners.remove(listener);
  }

  /**
   * Sets the resolution for the GameWindow.
   *
   * @param res The desired Resolution to set for the GameWindow.
   * @see Resolution
   */
  public void setResolution(Resolution res) {
    this.resolutionScale = setResolution(this.getHostControl(), res.getDimension());
  }

  /**
   * Gets the current resolution scale. The resolution scale is a float value dictating how much larger or smaller each pixel is rendered on screen.
   *
   * @return The GameWindow's current resolution scale.
   */
  public float getResolutionScale() {
    return this.resolutionScale;
  }

  /**
   * Gets the current resolution scale. The resolution scale is a float value dictating how much larger or smaller each pixel is rendered on screen.
   *
   * @return The {@code GameWindow}'s current resolution scale.
   */
  public Point2D getCenter() {
    return new Point2D.Double(this.getWidth() / 2.0, this.getHeight() / 2.0);
  }

  /**
   * Gets the {@code GameWindow}'s JFrame, abstracted as a Container.
   *
   * @return The {@code GameWindow}'s {@code JFrame} as an abstract AWT {@code Container}.
   */
  public Container getHostControl() {
    return this.hostControl;
  }

  /**
   * Gets the window width and height wrapped in a {@code Dimension} object.
   *
   * @return The {@code GameWindow}'s size as a {@link Dimension}.
   */
  public Dimension getSize() {
    return this.hostControl.getSize();
  }

  /**
   * Gets the window width.
   *
   * @return The window width.
   */
  public int getWidth() {
    return this.hostControl.getWidth();
  }

  /**
   * Gets the window height.
   *
   * @return The window height.
   */
  public int getHeight() {
    return this.hostControl.getHeight();
  }

  /**
   * Gets the AWT canvas that is used to render the game's content on.
   *
   * @return The AWT render component onto which the game contents are rendered.
   */
  public RenderComponent getRenderComponent() {
    return this.renderCanvas;
  }

  /**
   * Gets the visual representation of the mouse cursor on the {@code GameWindow}.
   *
   * <p>
   * This can be used to provide a custom cursor image, define its visibility or specify a rendering offset from the actual position.
   * </p>
   *
   * @return The mouse cursor of the game.
   */
  public MouseCursor cursor() {
    return this.cursor;
  }

  /**
   * Gets the window resolution wrapped in a {@code Dimension} object.
   *
   * @return The {@code GameWindow}'s internal resolution as a {@link Dimension}.
   */
  public Dimension getResolution() {
    return this.resolution;
  }

  /**
   * Gets the screen location of the window's top left corner.
   *
   * @return The {@code Point} of the window's top left corner.
   * @see Container#getLocationOnScreen
   */
  public Point getLocationOnScreen() {
    if (this.screenLocation != null) {
      return this.screenLocation;
    }

    this.screenLocation = this.hostControl.getLocationOnScreen();
    return this.screenLocation;
  }

  /**
   * Sets the icon image for the window's hosting {@code JFrame}.
   *
   * @param image The {@code Image} to be used as the window icon.
   * @see JFrame#setIconImage
   */
  public void setIcon(Image image) {
    this.hostControl.setIconImage(image);
  }

  /**
   * Sets the icons for the window's hosting {@code JFrame}. Depending on the platform specifications, one or several {@code Icon}s with the correct Dimension
   * will be chosen automatically from the list.
   *
   * @param images A list of {@code Images} to be used as the window icons.
   * @see JFrame#setIconImages
   */
  public void setIcons(List<? extends Image> images) {
    this.hostControl.setIconImages(images);
  }

  /**
   * Sets the title for this window to the specified string.
   *
   * @param title the window title to be displayed in the frame's border.
   *              A {@code null} value
   *              is treated as an empty string, "".
   * @see Frame#setTitle
   */
  public void setTitle(String title) {
    this.hostControl.setTitle(title);
  }

  /**
   * Initialize a {@code JFrame} to host the window with a given {@code DisplayMode} and resolution.
   * <p>
   * For example, {@code BORDERLESS} windows are not
   * resizable and are rendered without a border.
   * </p>
   *
   * @param host        The {@code JFrame} that hosts this window.
   * @param displaymode The {@code DisplayMode} for this window.
   * @param resolution  The desired window resolution.
   * @see DisplayMode
   * @see JFrame
   * @see #setResolution
   */
  static void prepareHostControl(JFrame host, DisplayMode displaymode, Dimension resolution) {
    switch (displaymode) {
    case BORDERLESS:
      host.setResizable(false);
      host.setUndecorated(true);
      host.setExtendedState(Frame.MAXIMIZED_BOTH);
      host.setVisible(true);

      resolution = new Dimension(host.getSize().width, host.getSize().height);
      break;
    case FULLSCREEN:
      GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

      if (!gd.isFullScreenSupported()) {
        log.log(Level.WARNING, "Full screen is not supported on this device. Falling back to borderless display mode.");
        prepareHostControl(host, DisplayMode.BORDERLESS, resolution);
        return;
      }

      gd.setFullScreenWindow(host);
      resolution = new Dimension(host.getSize().width, host.getSize().height);
      break;
    case WINDOWED:
    default:
      host.setVisible(true);
      break;
    }

    setResolution(host, resolution);
  }

  /**
   * Initialize the {@code GameWindow}.
   * If the Game is in "No GUI"-mode, the window resolution is set to (0,0) and the hosting JFrame is hidden.
   * Otherwise, the {@code JFrame} is initialized with the {@code DisplayMode} and resolution defined in the Graphics Configuration.
   * After initializing the hosting {@code JFrame}, the {@code RenderComponent} is also initialized and the window requests focus.
   *
   * @see Game#isInNoGUIMode
   * @see #prepareHostControl
   * @see GraphicConfiguration
   * @see RenderComponent
   */
  void init() {
    if (Game.isInNoGUIMode()) {
      this.resolution = new Dimension(0, 0);
      this.hostControl.setVisible(false);
      return;
    }

    prepareHostControl(this.hostControl, Game.config().graphics().getDisplayMode(), Game.config().graphics().getResolution());

    this.getRenderComponent().init();
    this.resolution = this.getRenderComponent().getSize();
    this.hostControl.requestFocus();
  }

  private static float setResolution(Container host, Dimension dim) {
    Dimension insetAwareDimension = new Dimension(dim.width + host.getInsets().left + host.getInsets().right,
        dim.height + host.getInsets().top + host.getInsets().bottom);

    host.setSize(insetAwareDimension);
    return getUpdatedResolutionScale(dim);
  }

  private static float getUpdatedResolutionScale(Dimension dim) {
    float resolutionScale = 1;
    if (Game.config().graphics().enableResolutionScaling()) {
      resolutionScale = (float) (dim.getWidth() / Resolution.Ratio16x9.RES_1920x1080.getWidth());
    }

    return resolutionScale;
  }

  private void initializeEventListeners() {
    this.getRenderComponent().addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent evt) {
        resolution = getRenderComponent().getSize();
        GameWindow.this.resolutionScale = getUpdatedResolutionScale(GameWindow.this.getSize());
        resolutionChangedListeners.forEach(listener -> listener.resolutionChanged(GameWindow.this.getSize()));
      }
    });

    this.hostControl.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentMoved(final ComponentEvent evt) {
        screenLocation = null;
      }
    });

  }

  private static void initializeWindowEventListeners(Window window) {

    window.addWindowStateListener(e -> {
      if (e.getNewState() == Frame.ICONIFIED) {
        Game.loop().setTickRate(ICONIFIED_MAX_FPS);
      } else {
        Game.loop().setTickRate(Game.config().client().getMaxFps());
      }
    });

    window.addWindowFocusListener(new WindowFocusListener() {
      @Override
      public void windowLostFocus(WindowEvent e) {
        if (Game.config().graphics().reduceFramesWhenNotFocused()) {
          Game.loop().setTickRate(NONE_FOCUS_MAX_FPS);
        }
      }

      @Override
      public void windowGainedFocus(WindowEvent e) {
        Game.loop().setTickRate(Game.config().client().getMaxFps());
      }
    });

    window.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent event) {
        Game.exit();
      }
    });
  }

  /**
   * This listener interface receives resolution changed events of the game window.
   *
   * @see GameWindow#onResolutionChanged(ResolutionChangedListener)
   * @see GameWindow#setResolution(Resolution)
   */
  @FunctionalInterface
  public interface ResolutionChangedListener extends EventListener {
    /**
     * Invoked when the resolution of the {@code GameWindow} changed.
     *
     * @param resolution The new resolution.
     */
    void resolutionChanged(Dimension resolution);
  }

}
