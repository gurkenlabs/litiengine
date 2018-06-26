package de.gurkenlabs.litiengine.graphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.screens.IScreen;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.util.ImageProcessing;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.io.ImageSerializer;

@SuppressWarnings("serial")
public class RenderComponent extends Canvas implements IRenderComponent {
  private static final int DEBUG_MOUSE_SIZE = 5;
  private static final Color DEFAULT_BACKGROUND_COLOR = Color.BLACK;
  private final transient List<Consumer<Integer>> fpsChangedConsumer;
  private final transient List<Consumer<Graphics2D>> renderedConsumer;

  private transient BufferStrategy currentBufferStrategy;

  private float currentAlpha;

  private transient Image cursorImage;
  private transient AffineTransform cursorTransform;
  private int cursorOffsetX;
  private int cursorOffsetY;

  private long fadeInStart;
  private int fadeInTime;
  private long fadeOutStart;
  private int fadeOutTime;

  private int frameCount = 0;
  private long lastFpsTime = System.currentTimeMillis();

  private boolean takeScreenShot;

  public RenderComponent(final Dimension size) {
    this.renderedConsumer = new CopyOnWriteArrayList<>();
    this.fpsChangedConsumer = new CopyOnWriteArrayList<>();

    this.setBackground(DEFAULT_BACKGROUND_COLOR);

    // hide default cursor
    final BufferedImage cursorImg = ImageProcessing.getCompatibleImage(16, 16);
    final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
    this.setCursor(blankCursor);
    this.setSize(size);

    // canvas will scale when the size of this jframe gets changed
    this.setPreferredSize(size);
  }

  @Override
  public void fadeIn(final int ms) {
    this.fadeOutStart = -1;
    this.fadeOutTime = -1;
    this.fadeInStart = Game.getLoop().getTicks();
    this.fadeInTime = ms;
  }

  @Override
  public void fadeOut(final int ms) {
    this.fadeInStart = -1;
    this.fadeInTime = -1;
    this.fadeOutStart = Game.getLoop().getTicks();
    this.fadeOutTime = ms;
  }

  @Override
  public Image getCursorImage() {
    return this.cursorImage;
  }

  @Override
  public AffineTransform getCursorTransform() {
    return this.cursorTransform;
  }

  public int getCursorOffsetX() {
    return this.cursorOffsetX;
  }

  public int getCursorOffsetY() {
    return this.cursorOffsetY;
  }

  @Override
  public void init() {
    this.createBufferStrategy(2);
    this.currentBufferStrategy = this.getBufferStrategy();
    this.currentAlpha = 1.1f;
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
  public void render() {
    final long currentMillis = System.currentTimeMillis();
    this.handleFade();
    Graphics2D g = null;
    do {
      try {

        g = (Graphics2D) this.currentBufferStrategy.getDrawGraphics();

        g.setColor(this.getBackground());

        final Rectangle bounds = new Rectangle(0, 0, this.getWidth(), this.getHeight());
        g.setClip(bounds);
        g.fill(bounds);

        final IScreen currentScreen = Game.getScreenManager().getCurrentScreen();
        if (currentScreen != null) {
          currentScreen.render(g);
        }

        final Point locationOnScreen = this.getLocationOnScreen();
        final Rectangle rect = new Rectangle(locationOnScreen.x, locationOnScreen.y, this.getWidth(), this.getHeight());
        final PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (this.cursorImage != null && (Input.mouse().isGrabMouse() || pointerInfo != null && rect.contains(pointerInfo.getLocation()))) {
          final Point2D locationWithOffset = new Point2D.Double(Input.mouse().getLocation().getX() - this.getCursorOffsetX(), Input.mouse().getLocation().getY() - this.getCursorOffsetY());
          RenderEngine.renderImage(g, this.cursorImage, locationWithOffset, this.getCursorTransform());
        }

        if (Game.getConfiguration().debug().isRenderDebugMouse()) {
          g.setColor(Color.RED);

          g.draw(new Line2D.Double(Input.mouse().getLocation().getX(), Input.mouse().getLocation().getY() - DEBUG_MOUSE_SIZE, Input.mouse().getLocation().getX(), Input.mouse().getLocation().getY() + DEBUG_MOUSE_SIZE));
          g.draw(new Line2D.Double(Input.mouse().getLocation().getX() - DEBUG_MOUSE_SIZE, Input.mouse().getLocation().getY(), Input.mouse().getLocation().getX() + DEBUG_MOUSE_SIZE, Input.mouse().getLocation().getY()));
        }

        for (final Consumer<Graphics2D> consumer : this.renderedConsumer) {
          consumer.accept(g);
        }

        if (this.currentAlpha != -1) {
          final int visibleAlpha = MathUtilities.clamp(Math.round(255 * (1 - this.currentAlpha)), 0, 255);
          g.setColor(new Color(this.getBackground().getRed(), this.getBackground().getGreen(), this.getBackground().getBlue(), visibleAlpha));
          g.fill(bounds);
        }

        if (this.takeScreenShot && currentScreen != null) {
          final BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
          final Graphics2D imgGraphics = img.createGraphics();
          currentScreen.render(imgGraphics);

          imgGraphics.dispose();
          this.saveScreenShot(img);
        }
      } finally {
        if (g != null) {
          g.dispose();
        }
      }

      // PERFORMANCE HINT: this method call basically takes up all the time required by this method
      this.currentBufferStrategy.show();
    } while (this.currentBufferStrategy.contentsLost());

    Toolkit.getDefaultToolkit().sync();
    this.frameCount++;

    if (currentMillis - this.lastFpsTime >= 1000) {
      this.lastFpsTime = currentMillis;
      this.fpsChangedConsumer.forEach(consumer -> consumer.accept(this.frameCount));
      this.frameCount = 0;
    }
  }

  @Override
  public void setCursor(final Image image) {
    this.cursorImage = image;
    if (this.cursorImage != null) {
      this.setCursorOffsetX(-(this.cursorImage.getWidth(null) / 2));
      this.setCursorOffsetY(-(this.cursorImage.getHeight(null) / 2));
    } else {
      this.setCursorOffsetX(0);
      this.setCursorOffsetY(0);
    }
  }

  @Override
  public void setCursor(final Image image, final int offsetX, final int offsetY) {
    this.setCursor(image);
    this.setCursorOffset(offsetX, offsetY);
  }

  @Override
  public void setCursorOffset(final int x, final int y) {
    this.setCursorOffsetX(x);
    this.setCursorOffsetY(y);
  }

  @Override
  public void setCursorOffsetX(final int cursorOffsetX) {
    this.cursorOffsetX = cursorOffsetX;
  }

  @Override
  public void setCursorOffsetY(final int cursorOffsetY) {
    this.cursorOffsetY = cursorOffsetY;
  }

  @Override
  public void setCursorTransform(AffineTransform transform) {
    this.cursorTransform = transform;
  }

  @Override
  public void takeScreenshot() {
    this.takeScreenShot = true;
  }

  private void handleFade() {
    if (this.fadeOutStart != -1) {
      final long timePassed = Game.getLoop().getDeltaTime(this.fadeOutStart);
      this.currentAlpha = MathUtilities.clamp(1 - timePassed / (float) this.fadeOutTime, 0, 1);
      if (this.currentAlpha == 0) {
        this.fadeOutStart = -1;
        this.fadeOutTime = -1;
      }

      return;
    }

    if (this.fadeInStart != -1) {
      final long timePassed = Game.getLoop().getDeltaTime(this.fadeInStart);
      this.currentAlpha = MathUtilities.clamp(timePassed / (float) this.fadeInTime, 0, 1);
      if (this.currentAlpha == 1) {
        this.fadeInStart = -1;
        this.fadeInTime = -1;
        this.currentAlpha = -1;
      }
    }
  }

  private void saveScreenShot(final BufferedImage img) {
    try {
      final String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
      final File folder = new File("./screenshots/");
      if (!folder.exists()) {
        folder.mkdirs();
      }

      ImageSerializer.saveImage(new File("./screenshots/" + timeStamp + ImageFormat.PNG.toExtension()).toString(), img);
    } finally {
      this.takeScreenShot = false;
    }
  }
}
