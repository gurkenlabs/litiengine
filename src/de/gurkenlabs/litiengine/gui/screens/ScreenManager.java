package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.swing.JFrame;

import de.gurkenlabs.litiengine.graphics.DefaultCamera;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.input.Input;

public class ScreenManager extends JFrame implements IScreenManager {
  private static final long serialVersionUID = 7958549828482285935L;
  private static final int SCREENCHANGETIMEOUT = 200;

  /** The resolution observers. */
  private final List<Consumer<Dimension>> resolutionChangedConsumer;

  private final List<Consumer<Integer>> fpsChangedConsumer;

  private final List<Consumer<IScreen>> screenChangedConsumer;

  /** The screens. */
  private final List<IScreen> screens;

  /** The Render canvas. */
  private final Canvas renderCanvas;

  /** The camera. */
  private ICamera camera;

  /** The current screen. */
  private IScreen currentScreen;

  private Image cursorImage;

  /** The last screen change. */
  private long lastScreenChange = 0;

  /** The last fps time. */
  private long lastFpsTime = System.currentTimeMillis();

  /** The frame count. */
  private int frameCount = 0;

  public ScreenManager(final String gameTitle) {
    super(gameTitle);
    this.resolutionChangedConsumer = new CopyOnWriteArrayList<>();
    this.fpsChangedConsumer = new CopyOnWriteArrayList<>();
    this.screenChangedConsumer = new CopyOnWriteArrayList<>();
    this.screens = new CopyOnWriteArrayList<>();

    // set default jframe stuff
    this.setResizable(false);
    this.setBackground(Color.BLACK);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addComponentListener(new ResizedEventListener());

    this.renderCanvas = new Canvas();

    // canvas will scale when the size of this jframe gets changed
    this.renderCanvas.setPreferredSize(this.getResolution());

    // hide default cursor
    BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
    this.renderCanvas.setCursor(blankCursor);

    this.add(this.renderCanvas);
  }

  @Override
  public void addScreen(final IScreen screen) {
    this.screens.add(screen);
    screen.setWidth(this.getWidth());
    screen.setHeight(this.getHeight());
  }

  @Override
  public void changeScreen(final String screen) {
    // if the scren is already displayed or there is no screen with the
    // specified name
    if (this.currentScreen != null && this.currentScreen.getName().equalsIgnoreCase(screen)
        || this.screens.stream().noneMatch(element -> element.getName().equalsIgnoreCase(screen))) {
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
    }

    this.currentScreen = targetScreen;
    this.currentScreen.prepare();
    this.setVisible(true);
    this.lastScreenChange = System.currentTimeMillis();
    for (final Consumer<IScreen> consumer : this.screenChangedConsumer) {
      consumer.accept(this.currentScreen);
    }
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
  public Component getRenderComponent() {
    return this.renderCanvas;
  }

  @Override
  public Dimension getResolution() {
    return this.getSize();
  }

  @Override
  public void init(final int width, final int height, final boolean fullscreen) {
    this.setCamera(new DefaultCamera());
    if (fullscreen) {
      this.setUndecorated(true);
      this.setExtendedState(JFrame.MAXIMIZED_BOTH);
      // this.device.setFullScreenWindow(this);
    } else {
      this.setSize(new Dimension(width, height));
    }

    this.setVisible(true);
    this.requestFocus();
  }

  @Override
  public void onFpsChanged(final Consumer<Integer> fpsConsumer) {
    if (this.fpsChangedConsumer.contains(fpsConsumer)) {
      return;
    }

    this.fpsChangedConsumer.add(fpsConsumer);
  }

  @Override
  public void onResolutionChanged(final Consumer<Dimension> resolutionConsumer) {
    if (this.resolutionChangedConsumer.contains(resolutionConsumer)) {
      return;
    }

    this.resolutionChangedConsumer.add(resolutionConsumer);
  }

  @Override
  public void onScreenChanged(Consumer<IScreen> screenConsumer) {
    if (!this.screenChangedConsumer.contains(screenConsumer)) {
      this.screenChangedConsumer.add(screenConsumer);
    }
  }

  @Override
  public void renderCurrentScreen() {
    if (this.getCurrentScreen() == null) {
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

    this.getCamera().updateFocus();
    this.getCurrentScreen().render(g);
    RenderEngine.renderImage(g, this.cursorImage, Input.MOUSE.getLocation());
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
  public void setCamera(final ICamera camera) {
    this.camera = camera;
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

  @Override
  public Point getScreenLocation() {
    return this.getRenderComponent().getLocationOnScreen();
  }

  @Override
  public void setCursor(Image image) {
    this.cursorImage = image;
  }

  public boolean isFocusOwner() {
    return super.isFocusOwner() || this.getRenderComponent().isFocusOwner();
  }
}
