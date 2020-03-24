package de.gurkenlabs.litiengine.graphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.TimeUtilities;
import de.gurkenlabs.litiengine.util.io.ImageSerializer;

@SuppressWarnings("serial")
public class RenderComponent extends Canvas {
  public static final Color DEFAULT_BACKGROUND_COLOR = Color.BLACK;
  public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

  private final transient List<IntConsumer> fpsChangedConsumer;
  private final transient List<Consumer<Graphics2D>> renderedConsumer;

  private transient BufferStrategy currentBufferStrategy;

  private float currentAlpha;

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
    this.setFont(DEFAULT_FONT);

    this.setSize(size);

    // canvas will scale when the size of this jframe gets changed
    this.setPreferredSize(size);
  }

  public void fadeIn(final int ms) {
    this.fadeOutStart = -1;
    this.fadeOutTime = -1;
    this.fadeInStart = Game.time().now();
    this.fadeInTime = ms;
  }

  public void fadeOut(final int ms) {
    this.fadeInStart = -1;
    this.fadeInTime = -1;
    this.fadeOutStart = Game.time().now();
    this.fadeOutTime = ms;
  }

  public void init() {
    this.createBufferStrategy(2);
    this.currentBufferStrategy = this.getBufferStrategy();
    this.currentAlpha = 1.1f;
  }

  public void onFpsChanged(final IntConsumer fpsConsumer) {
    if (this.fpsChangedConsumer.contains(fpsConsumer)) {
      return;
    }

    this.fpsChangedConsumer.add(fpsConsumer);
  }

  public void onRendered(final Consumer<Graphics2D> renderedConsumer) {
    if (!this.renderedConsumer.contains(renderedConsumer)) {
      this.renderedConsumer.add(renderedConsumer);
    }
  }

  public void render() {
    final long currentMillis = System.currentTimeMillis();
    if (currentMillis - this.lastFpsTime >= 1000) {
      this.lastFpsTime = currentMillis;
      this.fpsChangedConsumer.forEach(consumer -> consumer.accept(this.frameCount));
      this.frameCount = 0;
    }
    this.handleFade();
    Graphics2D g = null;
    do {
      try {

        g = (Graphics2D) this.currentBufferStrategy.getDrawGraphics();

        g.setColor(this.getBackground());

        final Rectangle bounds = new Rectangle(0, 0, this.getWidth(), this.getHeight());
        g.setClip(bounds);
        g.fill(bounds);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, Game.config().graphics().colorInterpolation() ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, Game.config().graphics().colorInterpolation() ? RenderingHints.VALUE_INTERPOLATION_BILINEAR : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        final Screen currentScreen = Game.screens().current();
        if (currentScreen != null) {

          long renderStart = System.nanoTime();

          //set up local instance of Graphics2D for the screen
          Graphics2D screenG = currentScreen.MakeGraphics(g);
          //render screen using new Graphics2D instance
          currentScreen.render(screenG);
          //free up memory from the now used Graphics2D instance
          screenG.dispose();

          if (Game.config().debug().trackRenderTimes()) {
            final double totalRenderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
            Game.metrics().trackRenderTime("screen", totalRenderTime);
          }
        }

        Game.window().cursor().render(g);

        for (final Consumer<Graphics2D> consumer : this.renderedConsumer) {
          consumer.accept(g);
        }

        if (this.currentAlpha != Float.NaN) {
          final int visibleAlpha = MathUtilities.clamp(Math.round(255 * (1 - this.currentAlpha)), 0, 255);
          g.setColor(new Color(this.getBackground().getRGB() & 0xffffff | visibleAlpha << 24, true));
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
  }

  public void takeScreenshot() {
    this.takeScreenShot = true;
  }

  private void handleFade() {
    if (this.fadeOutStart != -1) {
      final long timePassed = Game.time().since(this.fadeOutStart);
      this.currentAlpha = MathUtilities.clamp(1 - timePassed / (float) this.fadeOutTime, 0, 1);
      if (this.currentAlpha == 0f) {
        this.fadeOutStart = -1;
        this.fadeOutTime = -1;
      }

      return;
    }

    if (this.fadeInStart != -1) {
      final long timePassed = Game.time().since(this.fadeInStart);
      this.currentAlpha = MathUtilities.clamp(timePassed / (float) this.fadeInTime, 0, 1);
      if (this.currentAlpha == 1f) {
        this.fadeInStart = -1;
        this.fadeInTime = -1;
        this.currentAlpha = Float.NaN;
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

      ImageSerializer.saveImage(new File("./screenshots/" + timeStamp + ImageFormat.PNG.toFileExtension()).toString(), img);
    } finally {
      this.takeScreenShot = false;
    }
  }
}
