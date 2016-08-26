package de.gurkenlabs.litiengine.gui.screens;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import de.gurkenlabs.litiengine.graphics.Camera;
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

  private final List<Consumer<Graphics2D>> renderedConsumer;

  /** The screens. */
  private final List<IScreen> screens;

  /** The Render canvas. */
  private final Canvas renderCanvas;

  private BufferStrategy bufferStrategy;

  /** The camera. */
  private ICamera camera;

  /** The current screen. */
  private IScreen currentScreen;

  private Image cursorImage;
  private int cursorOffsetX;

  private int cursorOffsetY;

  /** The last screen change. */
  private long lastScreenChange = 0;

  /** The last fps time. */
  private long lastFpsTime = System.currentTimeMillis();

  /** The frame count. */
  private int frameCount = 0;

  private boolean takeScreenShot;

  public ScreenManager(final String gameTitle) {
    super(gameTitle);
    this.resolutionChangedConsumer = new CopyOnWriteArrayList<>();
    this.fpsChangedConsumer = new CopyOnWriteArrayList<>();
    this.screenChangedConsumer = new CopyOnWriteArrayList<>();
    this.screens = new CopyOnWriteArrayList<>();
    this.renderedConsumer = new CopyOnWriteArrayList<>();

    // set default jframe stuff
    this.setResizable(false);
    this.setBackground(Color.BLACK);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addComponentListener(new ResizedEventListener());

    this.renderCanvas = new Canvas();

    // canvas will scale when the size of this jframe gets changed
    this.renderCanvas.setPreferredSize(this.getResolution());

    // hide default cursor
    final BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
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
  public int getCursorOffsetX() {
    return this.cursorOffsetX;
  }

  @Override
  public int getCursorOffsetY() {
    return this.cursorOffsetY;
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
    } else {
      this.setSize(new Dimension(width, height));
    }

    Input.KEYBOARD.onKeyTyped(KeyEvent.VK_PRINTSCREEN, key -> this.takeScreenShot = true);
    this.setVisible(true);
    this.renderCanvas.createBufferStrategy(3);
    this.bufferStrategy = this.renderCanvas.getBufferStrategy();
    this.requestFocus();
  }

  @Override
  public boolean isFocusOwner() {
    return super.isFocusOwner() || this.getRenderComponent().isFocusOwner();
  }

  @Override
  public void onFpsChanged(final Consumer<Integer> fpsConsumer) {
    if (this.fpsChangedConsumer.contains(fpsConsumer)) {
      return;
    }

    this.fpsChangedConsumer.add(fpsConsumer);
  }

  @Override
  public void onRendered(final Consumer<Graphics2D> renderedConsumer) {
    if (!this.renderedConsumer.contains(renderedConsumer)) {
      this.renderedConsumer.add(renderedConsumer);
    }
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
  public void renderCurrentScreen() {
    if (this.getState() == Frame.ICONIFIED || this.getCurrentScreen() == null) {
      return;
    }

    final long currentMillis = System.currentTimeMillis();
    final Graphics2D g = (Graphics2D) this.bufferStrategy.getDrawGraphics();

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, this.getWidth(), this.getHeight());

    this.getCamera().updateFocus();

    g.setClip(new Rectangle(0, 0, (int) this.getResolution().getWidth(), (int) this.getResolution().getHeight()));
    this.getCurrentScreen().render(g);

    if (this.cursorImage != null) {
      RenderEngine.renderImage(g, this.cursorImage, new Point2D.Double(Input.MOUSE.getLocation().getX() + this.getCursorOffsetX(), Input.MOUSE.getLocation().getY() + this.getCursorOffsetY()));
    }

    for (final Consumer<Graphics2D> consumer : this.renderedConsumer) {
      consumer.accept(g);
    }

    if (this.takeScreenShot) {
      final BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
      final Graphics2D imgGraphics = (Graphics2D) img.getGraphics();
      this.getCurrentScreen().render(imgGraphics);

      final Point2D cursorlocation = new Point2D.Double(Input.MOUSE.getLocation().getX() + this.getCursorOffsetX(), Input.MOUSE.getLocation().getY() + this.getCursorOffsetY());
      RenderEngine.renderImage(imgGraphics, this.cursorImage, cursorlocation);
      imgGraphics.dispose();
      this.saveScreenShot(img);
    }

    g.dispose();

    this.bufferStrategy.show();
    Toolkit.getDefaultToolkit().sync();
    this.frameCount++;

    if (currentMillis - this.lastFpsTime >= 1000) {
      this.lastFpsTime = currentMillis;
      this.fpsChangedConsumer.forEach(consumer -> consumer.accept(this.frameCount));
      this.frameCount = 0;
    }

  }

  private void saveScreenShot(final BufferedImage img) {
    try {
      try {
        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        final File folder = new File("./screenshots/");
        if (!folder.exists()) {
          folder.mkdirs();
        }

        ImageIO.write(img, "png", new File("./screenshots/" + timeStamp + ".png"));
      } catch (final IOException e) {
        e.printStackTrace();
      }
    } finally {
      this.takeScreenShot = false;
    }
  }

  @Override
  public void setCamera(final ICamera camera) {
    this.camera = camera;
    this.getCamera().updateFocus();
  }

  @Override
  public void setCursor(final Image image) {
    this.cursorImage = image;
    if (this.cursorImage != null) {
      this.setCursorOffsetX(-(this.cursorImage.getWidth(null) / 2));
      this.setCursorOffsetY(-(this.cursorImage.getHeight(null) / 2));
    }
  }

  @Override
  public void setCursorOffsetX(final int cursorOffsetX) {
    this.cursorOffsetX = cursorOffsetX;
  }

  @Override
  public void setCursorOffsetY(final int cursorOffsetY) {
    this.cursorOffsetY = cursorOffsetY;
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