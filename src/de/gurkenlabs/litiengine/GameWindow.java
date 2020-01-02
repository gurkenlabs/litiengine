package de.gurkenlabs.litiengine;

import java.awt.Color;
import java.awt.Component;
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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import de.gurkenlabs.litiengine.configuration.DisplayMode;
import de.gurkenlabs.litiengine.graphics.MouseCursor;
import de.gurkenlabs.litiengine.graphics.RenderComponent;
import de.gurkenlabs.litiengine.gui.screens.Resolution;

/**
 * The <code>GameWindow</code> class is a wrapper for the game's visual window in which the <code>RenderComponent</code> lives.<br>
 * It provides the possibility to set a title, provide an icon, configure the cursor or get information about the resolution.
 * 
 * @see RenderComponent
 * @see #getResolution()
 * @see #setTitle(String)
 * @see #cursor()
 * @see #setIconImage(java.awt.Image)
 */
public final class GameWindow {
  private static final Logger log = Logger.getLogger(GameWindow.class.getName());
  private static final int ICONIFIED_MAX_FPS = 1;
  private static final int NONE_FOCUS_MAX_FPS = 10;

  private final List<Consumer<Dimension>> resolutionChangedConsumer;

  private final JFrame hostControl;
  private final RenderComponent renderCanvas;
  private final MouseCursor cursor;

  private float resolutionScale = 1;

  private Dimension resolution;
  private Point screenLocation;

  public GameWindow() {
    this.hostControl = new JFrame();

    this.resolutionChangedConsumer = new CopyOnWriteArrayList<>();

    this.renderCanvas = new RenderComponent(Game.config().graphics().getResolution());
    this.cursor = new MouseCursor();
    if (!Game.isInNoGUIMode()) {
      this.hostControl.setBackground(Color.BLACK);
      this.hostControl.add(this.renderCanvas);

      this.initializeEventListeners();

      this.hostControl.setTitle(Game.info().getTitle());
      this.hostControl.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      initializeWindowEventListeners(this.hostControl);
    }
  }

  public boolean isFocusOwner() {
    if (this.getRenderComponent() instanceof Component && this.getRenderComponent().isFocusOwner()) {
      return true;
    }

    return this.hostControl.isFocusOwner();
  }

  public void onResolutionChanged(final Consumer<Dimension> resolutionConsumer) {
    if (this.resolutionChangedConsumer.contains(resolutionConsumer)) {
      return;
    }

    this.resolutionChangedConsumer.add(resolutionConsumer);
  }

  public void setResolution(Resolution res) {
    this.resolutionScale = setResolution(this.getHostControl(), res.getDimension());
  }

  public float getResolutionScale() {
    return this.resolutionScale;
  }

  public Point2D getCenter() {
    return new Point2D.Double(this.getWidth() / 2.0, this.getHeight() / 2.0);
  }

  public Container getHostControl() {
    return this.hostControl;
  }

  public Dimension getSize() {
    return this.hostControl.getSize();
  }

  public int getWidth() {
    return this.hostControl.getWidth();
  }

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
   * Gets the visual representation of the mouse cursor on the <code>GameWindow</code>.
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

  public Dimension getResolution() {
    return this.resolution;
  }

  public Point getWindowLocation() {
    if (this.screenLocation != null) {
      return this.screenLocation;
    }

    this.screenLocation = this.hostControl.getLocationOnScreen();
    return this.screenLocation;
  }

  public void setIconImage(Image image) {
    this.hostControl.setIconImage(image);
  }

  public void setIconImages(List<? extends Image> image) {
    this.hostControl.setIconImages(image);
  }

  public void setTitle(String name) {
    this.hostControl.setTitle(name);
  }

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
      host.setVisible(true);
      break;
    }

    setResolution(host, resolution);
  }

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
    Dimension insetAwareDimension = new Dimension(dim.width + host.getInsets().left + host.getInsets().right, dim.height + host.getInsets().top + host.getInsets().bottom);

    float resolutionScale = 1;
    if (Game.config().graphics().enableResolutionScaling()) {
      resolutionScale = (float) (dim.getWidth() / Resolution.Ratio16x9.RES_1920x1080.getWidth());
      Game.graphics().setBaseRenderScale(Game.graphics().getBaseRenderScale() * resolutionScale);
    }

    host.setSize(insetAwareDimension);
    return resolutionScale;
  }

  private void initializeEventListeners() {
    this.getRenderComponent().addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent evt) {
        resolution = getRenderComponent().getSize();
        resolutionChangedConsumer.forEach(consumer -> consumer.accept(GameWindow.this.getSize()));
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
        if (Game.terminating()) {
          System.exit(Game.EXIT_GAME_CLOSED);
        }
      }
    });
  }
}
