package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.GameWindow;
import de.gurkenlabs.litiengine.graphics.RenderComponent;

//TODO: Separate ScreenManager from GameWindow implementation
public class ScreenManager extends JFrame implements IScreenManager, GameWindow {
  private static final Logger log = Logger.getLogger(ScreenManager.class.getName());
  private static final int SCREENCHANGETIMEOUT = 200;
  private static final int ICONIFIED_MAX_FPS = 1;
  private static final int NONE_FOCUS_MAX_FPS = 10;

  private static final long serialVersionUID = 7958549828482285935L;

  private final transient List<Consumer<Dimension>> resolutionChangedConsumer;

  private final transient List<Consumer<Screen>> screenChangedConsumer;

  private final transient List<Screen> screens;

  private transient Screen currentScreen;

  private long lastScreenChange = 0;

  private final RenderComponent renderCanvas;

  private float resolutionScale = 1;

  private Dimension resolution;
  private Point screenLocation;

  public ScreenManager(final String gameTitle) {
    super(gameTitle);
    this.resolutionChangedConsumer = new CopyOnWriteArrayList<>();
    this.screenChangedConsumer = new CopyOnWriteArrayList<>();
    this.screens = new CopyOnWriteArrayList<>();

    this.renderCanvas = new RenderComponent(Game.config().graphics().getResolution());
    if (!Game.isInNoGUIMode()) {
      // set default jframe stuff
      this.setResizable(false);
      this.setBackground(Color.BLACK);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

      this.add(this.renderCanvas);

      this.initializeEventListeners();
    }
  }

  @Override
  public void add(final Screen screen) {
    screen.setWidth(this.getWidth());
    screen.setHeight(this.getHeight());
    this.screens.add(screen);

    if (this.current() == null) {
      this.display(screen);
    }
  }

  @Override
  public void display(final Screen screen) {
    if (Game.hasStarted() && System.currentTimeMillis() - this.lastScreenChange < SCREENCHANGETIMEOUT) {
      return;
    }

    if (this.current() != null) {
      this.current().suspend();
    }

    if (screen != null && !this.screens.contains(screen)) {
      this.screens.add(screen);
    }

    this.currentScreen = screen;
    if (!Game.isInNoGUIMode() && this.current() != null) {
      this.current().prepare();
      this.setVisible(true);
    }

    this.lastScreenChange = System.currentTimeMillis();
    for (final Consumer<Screen> consumer : this.screenChangedConsumer) {
      consumer.accept(this.current());
    }
  }

  @Override
  public void display(final String screenName) {
    // if the screen is already displayed or there is no screen with the
    // specified name
    if (this.current() != null && this.current().getName().equalsIgnoreCase(screenName) || this.screens.stream().noneMatch(element -> element.getName().equalsIgnoreCase(screenName))) {
      // TODO: provide reasonable log, why the screen was not switched
      return;
    }

    Screen screen = this.get(screenName);
    if (screen == null) {
      return;
    }

    this.display(screen);
  }

  @Override
  public Point2D getCenter() {
    return new Point2D.Double(this.getWidth() / 2.0, this.getHeight() / 2.0);
  }

  @Override
  public Screen get(String screenName) {
    Optional<Screen> opt = this.screens.stream().filter(element -> element.getName().equalsIgnoreCase(screenName)).findFirst();
    return opt.orElse(null);
  }

  @Override
  public Screen current() {
    return this.currentScreen;
  }

  @Override
  public RenderComponent getRenderComponent() {
    return this.renderCanvas;
  }

  @Override
  public Dimension getResolution() {
    return this.resolution;
  }

  @Override
  public Point getWindowLocation() {
    if (this.screenLocation != null) {
      return this.screenLocation;
    }

    this.screenLocation = this.getLocationOnScreen();
    return this.screenLocation;
  }

  @Override
  public void init() {
    if (Game.isInNoGUIMode()) {
      this.resolution = new Dimension(0, 0);
      this.setVisible(false);
      return;
    }

    if (Game.config().graphics().isFullscreen()) {
      this.setUndecorated(true);
      GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

      if (gd.isFullScreenSupported()) {
        gd.setFullScreenWindow(this);
      } else {
        log.log(Level.SEVERE, "Full screen is not supported on this device.");
        this.setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);
      }
      this.setResolution(Resolution.custom(this.getSize().width, this.getSize().height, "fullscreen"));
    } else {
      this.setResolution(Game.config().graphics().getResolution());
      this.setVisible(true);
    }

    this.getRenderComponent().init();
    this.resolution = this.getRenderComponent().getSize();
    this.requestFocus();
  }

  @Override
  public boolean isFocusOwner() {
    if (this.getRenderComponent() instanceof Component && this.getRenderComponent().isFocusOwner()) {
      return true;
    }

    return super.isFocusOwner();
  }

  @Override
  public void onResolutionChanged(final Consumer<Dimension> resolutionConsumer) {
    if (this.resolutionChangedConsumer.contains(resolutionConsumer)) {
      return;
    }

    this.resolutionChangedConsumer.add(resolutionConsumer);
  }

  @Override
  public void onScreenChanged(final Consumer<Screen> screenConsumer) {
    if (!this.screenChangedConsumer.contains(screenConsumer)) {
      this.screenChangedConsumer.add(screenConsumer);
    }
  }

  @Override
  public void remove(Screen screen) {
    this.screens.remove(screen);
    if (this.current() == screen) {
      if (!this.screens.isEmpty()) {
        this.display(this.screens.get(0));
      } else {
        this.display((Screen) null);
      }
    }
  }

  @Override
  public void setResolution(Resolution res) {
    this.setResolution(res.getDimension());
  }

  @Override
  public float getResolutionScale() {
    return this.resolutionScale;
  }

  private void setResolution(Dimension dim) {
    Dimension insetAwareDimension = new Dimension(dim.width + this.getInsets().left + this.getInsets().right, dim.height + this.getInsets().top + this.getInsets().bottom);

    if (Game.config().graphics().enableResolutionScaling()) {
      this.resolutionScale = (float) (dim.getWidth() / Resolution.Ratio16x9.RES_1920x1080.getWidth());
      Game.graphics().setBaseRenderScale(Game.graphics().getBaseRenderScale() * this.resolutionScale);
    }

    this.setSize(insetAwareDimension);
  }

  private void initializeEventListeners() {
    this.getRenderComponent().addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent evt) {
        resolution = getRenderComponent().getSize();
        resolutionChangedConsumer.forEach(consumer -> consumer.accept(ScreenManager.this.getSize()));
      }
    });

    this.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentMoved(final ComponentEvent evt) {
        screenLocation = null;
      }
    });

    this.addWindowStateListener(e -> {
      if (e.getNewState() == Frame.ICONIFIED) {
        Game.renderLoop().setMaxFps(ICONIFIED_MAX_FPS);
      } else {
        Game.renderLoop().setMaxFps(Game.config().client().getMaxFps());
      }
    });

    this.addWindowFocusListener(new WindowFocusListener() {
      @Override
      public void windowLostFocus(WindowEvent e) {
        if (Game.config().graphics().reduceFramesWhenNotFocused()) {
          Game.renderLoop().setMaxFps(NONE_FOCUS_MAX_FPS);
        }
      }

      @Override
      public void windowGainedFocus(WindowEvent e) {
        Game.renderLoop().setMaxFps(Game.config().client().getMaxFps());
      }
    });

    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent event) {
        System.exit(Game.EXIT_GAME_CLOSED);
      }
    });
  }
}
