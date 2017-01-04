package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.swing.JFrame;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.Camera;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.graphics.RenderComponent;

public class ScreenManager extends JFrame implements IScreenManager {

  private static final long serialVersionUID = 7958549828482285935L;

  private static final int SCREENCHANGETIMEOUT = 200;

  /** The resolution observers. */
  private final List<Consumer<Dimension>> resolutionChangedConsumer;

  private final List<Consumer<IScreen>> screenChangedConsumer;

  /** The screens. */
  private final List<IScreen> screens;

  /** The Render canvas. */
  private final RenderComponent renderCanvas;

  /** The camera. */
  private ICamera camera;

  /** The current screen. */
  private IScreen currentScreen;

  /** The last screen change. */
  private long lastScreenChange = 0;

  private Dimension resolution;

  public ScreenManager(final String gameTitle) {
    super(gameTitle);
    this.resolution = this.getSize();
    this.resolutionChangedConsumer = new CopyOnWriteArrayList<>();
    this.screenChangedConsumer = new CopyOnWriteArrayList<>();
    this.screens = new CopyOnWriteArrayList<>();

    // set default jframe stuff
    this.setResizable(false);
    this.setBackground(Color.BLACK);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addComponentListener(new ResizedEventListener());

    RenderComponent comp = new RenderComponent(this.getResolution());
    this.add(comp);
    this.renderCanvas = comp;
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
  public void displayScreen(final String screen) {
    // if the scren is already displayed or there is no screen with the
    // specified name
    if (this.currentScreen != null && this.currentScreen.getName().equalsIgnoreCase(screen) || this.screens.stream().noneMatch(element -> element.getName().equalsIgnoreCase(screen))) {
      return;
    }
    if (System.currentTimeMillis() - this.lastScreenChange < SCREENCHANGETIMEOUT) {
      return;
    }

    final IScreen targetScreen = this.screens.stream().filter(element -> element.getName().equalsIgnoreCase(screen)).findFirst().get();
    if (targetScreen == null) {
      return;
    }

    if (this.currentScreen != null) {
      this.currentScreen.suspend();
      Game.getRenderLoop().unregister(this.currentScreen);
    }

    this.currentScreen = targetScreen;
    this.currentScreen.prepare();
    this.setVisible(true);
    Game.getRenderLoop().register(this.currentScreen);
    this.lastScreenChange = System.currentTimeMillis();
    for (final Consumer<IScreen> consumer : this.screenChangedConsumer) {
      consumer.accept(this.currentScreen);
    }
  }

  @Override
  public void displayScreen(IScreen screen) {
    this.displayScreen(screen.getName());
  }

  @Override
  public ICamera getCamera() {
    return this.camera;
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
    this.setCamera(new Camera());
    if (fullscreen) {
      this.setUndecorated(true);
      this.setExtendedState(Frame.MAXIMIZED_BOTH);
      // this.device.setFullScreenWindow(this);
    }

    this.setSize(Game.getConfiguration().GRAPHICS.getResolution());
    this.resolution = this.getSize();

    this.setVisible(true);
    this.getRenderComponent().init();
    this.requestFocus();
  }

  @Override
  public boolean isFocusOwner() {
    if (this.getRenderComponent() instanceof Component && ((Component) this.getRenderComponent()).isFocusOwner()) {
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
  public void setCamera(final ICamera camera) {
    if (this.getCamera() != null) {
      Game.getLoop().unregisterFromUpdate(this.camera);
    }

    this.camera = camera;
    Game.getLoop().registerForUpdate(this.getCamera());
    this.getCamera().updateFocus();
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
    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.
     * ComponentEvent)
     */
    @Override
    public void componentResized(final ComponentEvent evt) {
      ScreenManager.this.resolutionChangedConsumer.forEach(consumer -> consumer.accept(ScreenManager.this.getSize()));
    }
  }
}