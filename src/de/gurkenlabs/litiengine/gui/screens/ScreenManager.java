package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowStateListener;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.swing.JFrame;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.RenderComponent;

public class ScreenManager extends JFrame implements IScreenManager, WindowStateListener, WindowFocusListener {

  private static final int SCREENCHANGETIMEOUT = 200;
  private static final int ICONIFIED_MAX_FPS = 1;
  private static final int NONE_FOCUS_MAX_FPS = 10;

  private static final long serialVersionUID = 7958549828482285935L;

  /** The resolution observers. */
  private final transient List<Consumer<Dimension>> resolutionChangedConsumer;

  private final transient List<Consumer<IScreen>> screenChangedConsumer;

  /** The screens. */
  private final transient List<IScreen> screens;

  /** The current screen. */
  private transient IScreen currentScreen;

  /** The last screen change. */
  private long lastScreenChange = 0;

  /** The Render canvas. */
  private final RenderComponent renderCanvas;

  private float resolutionScale = 1;

  private Dimension resolution;

  public ScreenManager(final String gameTitle) {
    super(gameTitle);
    this.resolutionChangedConsumer = new CopyOnWriteArrayList<>();
    this.screenChangedConsumer = new CopyOnWriteArrayList<>();
    this.screens = new CopyOnWriteArrayList<>();

    // set default jframe stuff
    this.setResizable(false);
    this.setBackground(Color.BLACK);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addComponentListener(new ResizedEventListener());
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    final RenderComponent comp = new RenderComponent(Game.getConfiguration().graphics().getResolution());
    this.add(comp);
    this.renderCanvas = comp;

    this.addWindowStateListener(this);
    this.addWindowFocusListener(this);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent event) {
        // ensures that we terminate the game, when the window is being closed
        Game.terminate();
      }
    });
  }

  @Override
  public void addScreen(final IScreen screen) {
    this.screens.add(screen);
    screen.setWidth(this.getWidth());
    screen.setHeight(this.getHeight());
    if (this.getCurrentScreen() == null) {
      this.displayScreen(screen);
    }
  }

  @Override
  public void displayScreen(final IScreen screen) {
    this.displayScreen(screen.getName());
  }

  @Override
  public void displayScreen(final String screen) {
    // if the screen is already displayed or there is no screen with the
    // specified name
    if (this.getCurrentScreen() != null && this.getCurrentScreen().getName().equalsIgnoreCase(screen) || this.screens.stream().noneMatch(element -> element.getName().equalsIgnoreCase(screen))) {
      // TODO: provide reasonable log, why the screen was not switched
      return;
    }

    if (Game.hasStarted() && System.currentTimeMillis() - this.lastScreenChange < SCREENCHANGETIMEOUT) {
      return;
    }

    Optional<IScreen> opt = this.screens.stream().filter(element -> element.getName().equalsIgnoreCase(screen)).findFirst();
    if (!opt.isPresent()) {
      return;
    }

    final IScreen targetScreen = opt.get();
    if (this.getCurrentScreen() != null) {
      this.getCurrentScreen().suspend();
      Game.getRenderLoop().unregister(this.getCurrentScreen());
    }

    this.currentScreen = targetScreen;
    this.getCurrentScreen().prepare();
    this.setVisible(true);
    Game.getRenderLoop().register(this.getCurrentScreen());
    this.lastScreenChange = System.currentTimeMillis();
    for (final Consumer<IScreen> consumer : this.screenChangedConsumer) {
      consumer.accept(this.getCurrentScreen());
    }
  }

  @Override
  public IScreen getCurrentScreen() {
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
  public Point getScreenLocation() {
    return this.getLocationOnScreen();
  }

  @Override
  public void init(final int width, final int height, final boolean fullscreen) {
    if (fullscreen) {
      this.setUndecorated(true);
      this.setExtendedState(Frame.MAXIMIZED_BOTH);
      this.setVisible(true);
      this.setResolution(Resolution.custom(this.getSize().width, this.getSize().height, "fullscreen"));
    } else {
      this.setResolution(Game.getConfiguration().graphics().getResolution());
      this.setVisible(true);
    }

    this.getRenderComponent().init();
    resolution = this.getRenderComponent().getSize();
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
  public void onScreenChanged(final Consumer<IScreen> screenConsumer) {
    if (!this.screenChangedConsumer.contains(screenConsumer)) {
      this.screenChangedConsumer.add(screenConsumer);
    }
  }

  @Override
  public void setResolution(Resolution res) {
    this.setResolution(res.getDimension());
  }

  @Override
  public void windowStateChanged(WindowEvent e) {
    if (e.getNewState() == Frame.ICONIFIED) {
      Game.getRenderLoop().setMaxFps(ICONIFIED_MAX_FPS);
    } else {
      Game.getRenderLoop().setMaxFps(Game.getConfiguration().client().getMaxFps());
    }
  }

  @Override
  public void windowGainedFocus(WindowEvent e) {
    Game.getRenderLoop().setMaxFps(Game.getConfiguration().client().getMaxFps());
  }

  @Override
  public void windowLostFocus(WindowEvent e) {
    Game.getRenderLoop().setMaxFps(NONE_FOCUS_MAX_FPS);
  }

  private void setResolution(Dimension dim) {
    Dimension insetAwareDimension = new Dimension(dim.width + this.getInsets().left + this.getInsets().right, dim.height + this.getInsets().top + this.getInsets().bottom);

    if (Game.getConfiguration().graphics().enableResolutionScale()) {
      this.resolutionScale = (float) (dim.getWidth() / Resolution.Ratio16x9.RES_1920x1080.getWidth());
      Game.getInfo().setDefaultRenderScale(Game.getInfo().getDefaultRenderScale() * this.resolutionScale);
    }

    this.setSize(insetAwareDimension);
  }

  /**
   * The listener interface for receiving resizedEvent events. The class that is
   * interested in processing a resizedEvent event implements this interface,
   * and the object created with that class is registered with a component using
   * the component's <code>addResizedEventListener<code> method. When the
   * resizedEvent event occurs, that object's appropriate method is invoked.
   *
   * @see ResizedEventEvent
   */
  private class ResizedEventListener extends ComponentAdapter {
    @Override
    public void componentResized(final ComponentEvent evt) {
      resolution = getRenderComponent().getSize();
      ScreenManager.this.resolutionChangedConsumer.forEach(consumer -> consumer.accept(ScreenManager.this.getSize()));
    }
  }

  @Override
  public float getResolutionScale() {
    return this.resolutionScale;
  }
}