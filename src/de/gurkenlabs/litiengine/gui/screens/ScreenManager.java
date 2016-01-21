package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.swing.JFrame;

public class ScreenManager extends JFrame implements IScreenManager {
  private static final long serialVersionUID = 7958549828482285935L;
  private static final int SCREENCHANGETIMEOUT = 200;

  /** The resolution observers. */
  private final List<Consumer<Dimension>> resolutionChangedConsumer;

  private final List<Consumer<Integer>> fpsChangedConsumer;

  /** The screens. */
  private final List<IScreen> screens;

  private final GraphicsDevice device;

  /** The Render canvas. */
  private final Canvas renderCanvas;

  /** The current screen. */
  private IScreen currentScreen;

  /** The last screen change. */
  private long lastScreenChange = 0;

  /** The last fps time. */
  private long lastFpsTime = System.currentTimeMillis();

  /** The frame count. */
  private int frameCount = 0;

  public ScreenManager(String gameTitle) {
    super(gameTitle);
    this.resolutionChangedConsumer = new CopyOnWriteArrayList<>();
    this.fpsChangedConsumer = new CopyOnWriteArrayList<>();
    this.screens = new CopyOnWriteArrayList<>();

    // set default jframe stuff
    this.setResizable(false);
    this.setBackground(Color.BLACK);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addComponentListener(new ResizedEventListener());

    this.device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    this.renderCanvas = new Canvas();

    // canvas will scale when the size of this jframe gets changed
    this.renderCanvas.setPreferredSize(this.getResolution());

    this.add(this.renderCanvas);
  }

  @Override
  public void addScreen(final IScreen screen) {
    this.screens.add(screen);
    screen.setWidth(this.getWidth());
    screen.setHeight(this.getHeight());
  }

  @Override
  public void init(int width, int height, boolean fullscreen) {
    if (fullscreen) {
      this.setUndecorated(true);
      this.device.setFullScreenWindow(this);
    } else {
      this.setSize(new Dimension(width, height));
    }

    this.setVisible(true);
    this.requestFocus();
  }

  @Override
  public void changeScreen(final String screen) {
    // if the scren is already displayed or there is no screen with the
    // specified name
    if (this.currentScreen != null && this.currentScreen.getName().equalsIgnoreCase(screen)
        || this.screens.stream().noneMatch(element -> element.getName().equalsIgnoreCase(screen))) {
      return;
    }
    if (System.currentTimeMillis() - this.lastScreenChange >= SCREENCHANGETIMEOUT) {
      final IScreen targetScreen = this.screens.stream().filter(element -> element.getName().equalsIgnoreCase(screen)).findFirst().get();
      if (targetScreen == null) {
        return;
      }

      if (this.currentScreen != null) {
        this.currentScreen.suspend();
      }

      this.currentScreen = targetScreen;
      this.currentScreen.prepare();
      this.setVisible(true);
      this.lastScreenChange = System.currentTimeMillis();
    }
  }

  @Override
  public IScreen getCurrentScreen() {
    return this.currentScreen;
  }

  @Override
  public Dimension getResolution() {
    return this.getSize();
  }

  @Override
  public void onResolutionChanged(Consumer<Dimension> resolutionConsumer) {
    if (this.resolutionChangedConsumer.contains(resolutionConsumer)) {
      return;
    }

    this.resolutionChangedConsumer.add(resolutionConsumer);
  }

  @Override
  public void onFpsChanged(Consumer<Integer> fpsConsumer) {
    if (this.fpsChangedConsumer.contains(fpsConsumer)) {
      return;
    }

    this.fpsChangedConsumer.add(fpsConsumer);
  }

  @Override
  public void renderCurrentScreen() {
    if(this.getCurrentScreen() == null){
      return;
    }
    
    final long currentMillis = System.currentTimeMillis();
    final BufferStrategy bs = this.renderCanvas.getBufferStrategy();
    if (bs == null) {
      this.renderCanvas.createBufferStrategy(3);
      return;
    }

    final Graphics g = bs.getDrawGraphics();

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, this.getWidth(), this.getHeight());
    this.getCurrentScreen().render(g);

    g.dispose();
    bs.show();

    this.frameCount++;

    if (currentMillis - this.lastFpsTime >= 1000) {
      this.lastFpsTime = currentMillis;
      this.fpsChangedConsumer.forEach(consumer -> consumer.accept(this.frameCount));
      this.frameCount = 0;
    }
  }

  @Override
  public Component getRenderComponent() {
    return this.renderCanvas;
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
