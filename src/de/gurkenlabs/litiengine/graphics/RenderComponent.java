package de.gurkenlabs.litiengine.graphics;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.util.MathUtilities;

public class RenderComponent extends Canvas implements IRenderComponent {
  private static final long serialVersionUID = 5092360478850476013L;

  private final List<Consumer<Graphics2D>> renderedConsumer;

  private final List<Consumer<Integer>> fpsChangedConsumer;

  private BufferStrategy bufferStrategy;

  /** The last fps time. */
  private long lastFpsTime = System.currentTimeMillis();

  /** The frame count. */
  private int frameCount = 0;

  private boolean takeScreenShot;

  private Image cursorImage;
  private int cursorOffsetX;

  private int cursorOffsetY;

  private int fadeInTime;
  private long fadeInStart;
  private int fadeOutTime;
  private long fadeOutStart;
  private float currentAlpha;

  @Override
  public void init() {
    this.createBufferStrategy(3);
    this.bufferStrategy = this.getBufferStrategy();
    this.currentAlpha = 1.1f;
  }

  public RenderComponent(Dimension size) {
    this.renderedConsumer = new CopyOnWriteArrayList<>();
    this.fpsChangedConsumer = new CopyOnWriteArrayList<>();

    // hide default cursor
    final BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
    this.setCursor(blankCursor);
    this.setSize(size);

    // canvas will scale when the size of this jframe gets changed
    this.setPreferredSize(size);
  }

  public void render(IRenderable screen) {
    final long currentMillis = System.currentTimeMillis();
    final Graphics2D g = (Graphics2D) this.bufferStrategy.getDrawGraphics();

    this.handleFade();

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, this.getWidth(), this.getHeight());

    g.setClip(new Rectangle(0, 0, (int) this.getWidth(), (int) this.getHeight()));

    screen.render(g);
    Rectangle rect = new Rectangle(this.getLocationOnScreen().x, this.getLocationOnScreen().y, this.getWidth(), this.getHeight());
    if (this.cursorImage != null && (Input.MOUSE.isGrabMouse() || rect.contains(MouseInfo.getPointerInfo().getLocation()))) {
      Point2D locationWithOffset = new Point2D.Double(Input.MOUSE.getLocation().getX() - this.getCursorOffsetX(), Input.MOUSE.getLocation().getY() - this.getCursorOffsetY());
      RenderEngine.renderImage(g, this.cursorImage, locationWithOffset);
    }

    if (Game.getConfiguration().DEBUG.isRenderDebugMouse()) {
      g.setColor(Color.RED);
      final int DEBUG_MOUSE_SIZE = 5;
      g.draw(new Line2D.Double(Input.MOUSE.getLocation().getX(), Input.MOUSE.getLocation().getY() - DEBUG_MOUSE_SIZE, Input.MOUSE.getLocation().getX(), Input.MOUSE.getLocation().getY() + DEBUG_MOUSE_SIZE));
      g.draw(new Line2D.Double(Input.MOUSE.getLocation().getX() - DEBUG_MOUSE_SIZE, Input.MOUSE.getLocation().getY(), Input.MOUSE.getLocation().getX() + DEBUG_MOUSE_SIZE, Input.MOUSE.getLocation().getY()));
    }

    for (final Consumer<Graphics2D> consumer : this.renderedConsumer) {
      consumer.accept(g);
    }

    if (this.currentAlpha != -1) {
      g.setColor(new Color(0, 0, 0, 1 - this.currentAlpha));
      g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    if (this.takeScreenShot) {
      final BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
      final Graphics2D imgGraphics = img.createGraphics();
      screen.render(imgGraphics);

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
  public void setCursor(Image image, int offsetX, int offsetY) {
    this.setCursor(image);
    this.setCursorOffset(offsetX, offsetY);
  }

  @Override
  public Image getCursorImage() {
    return this.cursorImage;
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
  public void setCursorOffset(int x, int y) {
    this.setCursorOffsetX(x);
    this.setCursorOffsetY(y);
  }

  public int getCursorOffsetX() {
    return this.cursorOffsetX;
  }

  public int getCursorOffsetY() {
    return this.cursorOffsetY;
  }

  public void onRendered(final Consumer<Graphics2D> renderedConsumer) {
    if (!this.renderedConsumer.contains(renderedConsumer)) {
      this.renderedConsumer.add(renderedConsumer);
    }
  }

  public void onFpsChanged(final Consumer<Integer> fpsConsumer) {
    if (this.fpsChangedConsumer.contains(fpsConsumer)) {
      return;
    }

    this.fpsChangedConsumer.add(fpsConsumer);
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
  public void takeScreenshot() {
    this.takeScreenShot = true;
  }

  @Override
  public void fadeOut(int ms) {
    this.fadeInStart = -1;
    this.fadeInTime = -1;
    this.fadeOutStart = Game.getLoop().getTicks();
    this.fadeOutTime = ms;
  }

  @Override
  public void fadeIn(int ms) {
    this.fadeOutStart = -1;
    this.fadeOutTime = -1;
    this.fadeInStart = Game.getLoop().getTicks();
    this.fadeInTime = ms;
  }

  private void handleFade() {
    if (this.fadeOutStart != -1) {
      long timePassed = Game.getLoop().getDeltaTime(this.fadeOutStart);
      this.currentAlpha = MathUtilities.clamp(1 - timePassed / (float) this.fadeOutTime, 0, 1);
      if (this.currentAlpha == 0) {
        this.fadeOutStart = -1;
        this.fadeOutTime = -1;
      }

      return;
    }

    if (this.fadeInStart != -1) {
      long timePassed = Game.getLoop().getDeltaTime(this.fadeInStart);
      this.currentAlpha = MathUtilities.clamp(timePassed / (float) this.fadeInTime, 0, 1);
      if (this.currentAlpha == 1) {
        this.fadeInStart = -1;
        this.fadeInTime = -1;
        this.currentAlpha = -1;
      }
    }
  }
}
