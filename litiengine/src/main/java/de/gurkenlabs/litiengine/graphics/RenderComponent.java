package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.util.TimeUtilities;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * The {@code RenderComponent} class extends {@link Canvas} and handles the rendering of the game screen, including managing fade effects, capturing
 * screenshots, and rendering the game cursor.
 */
public class RenderComponent extends Canvas {
  private static final Logger log = Logger.getLogger(RenderComponent.class.getName());
  /**
   * The default background color for the rendering component.
   */
  public static final Color DEFAULT_BACKGROUND_COLOR = Color.BLACK;

  /**
   * The default font for rendering text in the component.
   */
  public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

  private final transient List<IntConsumer> fpsChangedConsumer = new CopyOnWriteArrayList<>();
  private final transient List<Consumer<Graphics2D>> renderedConsumer = new CopyOnWriteArrayList<>();

  private transient BufferStrategy currentBufferStrategy;

  private float currentAlpha = Float.NaN;
  private long fadeInStart = -1;
  private long fadeOutStart = -1;
  private int fadeInTime;
  private int fadeOutTime;

  private int frameCount = 0;
  private long lastFpsTime = System.currentTimeMillis();

  private boolean takeScreenShot;

  /**
   * Constructs a new {@code RenderComponent} with the specified size.
   *
   * @param size The size of the rendering component.
   */
  public RenderComponent(final Dimension size) {
    setBackground(DEFAULT_BACKGROUND_COLOR);
    setFont(DEFAULT_FONT);
    setSize(size);
    setPreferredSize(size);
  }

  /**
   * Initiates a fade-in effect over the specified duration.
   *
   * @param ms The duration of the fade-in effect in milliseconds.
   */
  public void fadeIn(final int ms) {
    resetFade();
    this.fadeInStart = Game.time().now();
    this.fadeInTime = ms;
  }

  /**
   * Initiates a fade-out effect over the specified duration.
   *
   * @param ms The duration of the fade-out effect in milliseconds.
   */
  public void fadeOut(final int ms) {
    resetFade();
    this.fadeOutStart = Game.time().now();
    this.fadeOutTime = ms;
  }

  /**
   * Resets the fade-in and fade-out timers.
   */
  private void resetFade() {
    this.fadeInStart = this.fadeOutStart = -1;
    this.fadeInTime = this.fadeOutTime = 0;
  }

  /**
   * Initializes the {@code RenderComponent}, setting up the buffer strategy for rendering.
   */
  public void init() {
    createBufferStrategy(2);
    this.currentBufferStrategy = getBufferStrategy();
  }

  /**
   * Registers a consumer to be notified when the frames per second (FPS) change.
   *
   * @param fpsConsumer The consumer to notify of FPS changes.
   */
  public void onFpsChanged(final IntConsumer fpsConsumer) {
    if (!fpsChangedConsumer.contains(fpsConsumer)) {
      fpsChangedConsumer.add(fpsConsumer);
    }
  }

  /**
   * Registers a consumer to be notified after the component has been rendered.
   *
   * @param renderedConsumer The consumer to notify after rendering.
   */
  public void onRendered(final Consumer<Graphics2D> renderedConsumer) {
    if (!this.renderedConsumer.contains(renderedConsumer)) {
      this.renderedConsumer.add(renderedConsumer);
    }
  }

  /**
   * Renders the game screen, including handling fade effects, cursor rendering, and screenshot capture.
   */
  public void render() {
    if (System.currentTimeMillis() - lastFpsTime >= 1000) {
      lastFpsTime = System.currentTimeMillis();
      fpsChangedConsumer.forEach(consumer -> consumer.accept(frameCount));
      frameCount = 0;
    }

    handleFade();
    do {
      Graphics2D g = (Graphics2D) currentBufferStrategy.getDrawGraphics();
      try {
        renderGraphics(g);
      } finally {
        g.dispose();
      }
      currentBufferStrategy.show();
    } while (currentBufferStrategy.contentsLost());

    Toolkit.getDefaultToolkit().sync();
    frameCount++;
  }

  /**
   * Clears the background and renders the current screen and other graphical elements.
   *
   * @param g The {@link Graphics2D} object used for rendering.
   */
  private void renderGraphics(Graphics2D g) {
    clearBackground(g);
    applyRenderingHints(g);

    Screen currentScreen = Game.screens().current();
    if (currentScreen != null) {
      renderScreen(g, currentScreen);
    }

    Game.window().cursor().render(g);
    renderedConsumer.forEach(consumer -> consumer.accept(g));
    applyFadeOverlay(g);

    if (takeScreenShot && currentScreen != null) {
      takeAndSaveScreenshot(currentScreen);
    }
  }

  /**
   * Clears the background of the component.
   *
   * @param g The {@link Graphics2D} object used for rendering.
   */
  private void clearBackground(Graphics2D g) {
    g.setColor(getBackground());
    Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());
    g.setClip(bounds);
    g.fill(bounds);
  }

  /**
   * Applies rendering hints for antialiasing and interpolation.
   *
   * @param g The {@link Graphics2D} object used for rendering.
   */
  private void applyRenderingHints(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      Game.config().graphics().colorInterpolation() ?
        RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
      Game.config().graphics().colorInterpolation() ?
        RenderingHints.VALUE_INTERPOLATION_BILINEAR : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
  }

  /**
   * Renders the current game screen.
   *
   * @param g      The {@link Graphics2D} object used for rendering.
   * @param screen The current {@link Screen} to render.
   */
  private void renderScreen(Graphics2D g, Screen screen) {
    long renderStart = System.nanoTime();
    screen.render(g);

    if (Game.config().debug().trackRenderTimes()) {
      double totalRenderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
      Game.metrics().trackRenderTime("screen", totalRenderTime);
    }
  }

  /**
   * Applies a fade overlay to the rendered content, if applicable.
   *
   * @param g The {@link Graphics2D} object used for rendering.
   */
  private void applyFadeOverlay(Graphics2D g) {
    if (!Float.isNaN(currentAlpha)) {
      int visibleAlpha = Math.clamp(Math.round(255 * (1 - currentAlpha)), 0, 255);
      g.setColor(new Color(getBackground().getRGB() & 0xFFFFFF | (visibleAlpha << 24), true));
      g.fill(new Rectangle(0, 0, getWidth(), getHeight()));
    }
  }

  /**
   * Captures and saves a screenshot of the current screen.
   *
   * @param screen The current {@link Screen} to capture.
   */
  private void takeAndSaveScreenshot(Screen screen) {
    BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D imgGraphics = img.createGraphics();
    screen.render(imgGraphics);
    imgGraphics.dispose();
    saveScreenshot(img);
  }

  /**
   * Signals the {@code RenderComponent} to take a screenshot on the next render cycle.
   */
  public void takeScreenshot() {
    this.takeScreenShot = true;
  }

  /**
   * Handles the fade-in and fade-out effects, adjusting the current alpha value based on time elapsed.
   */
  private void handleFade() {
    if (fadeOutStart != -1) {
      updateAlpha(fadeOutStart, fadeOutTime, false);
      if (currentAlpha == 0f) {
        resetFade();
      }
    } else if (fadeInStart != -1) {
      updateAlpha(fadeInStart, fadeInTime, true);
      if (currentAlpha == 1f) {
        resetFade();
      }
    }
  }

  /**
   * Updates the alpha value for fade effects.
   *
   * @param startTime The start time of the fade effect.
   * @param duration  The duration of the fade effect.
   * @param fadeIn    {@code true} if this is a fade-in effect, {@code false} if it's a fade-out.
   */
  private void updateAlpha(long startTime, int duration, boolean fadeIn) {
    long timePassed = Game.time().since(startTime);
    currentAlpha = Math.clamp((fadeIn ? timePassed : duration - timePassed) / (float) duration, 0, 1);
  }

  /**
   * Saves the provided image as a screenshot to the file system.
   *
   * @param img The {@link BufferedImage} to save as a screenshot.
   */
  private void saveScreenshot(BufferedImage img) {
    try {
      String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
      Path folder = Path.of("./screenshots/");
      if (!Files.exists(folder)) {
        Files.createDirectories(folder);
      }
      ImageIO.write(img, ImageFormat.PNG.toFileExtension(), Path.of("screenshots", timeStamp + ImageFormat.PNG.toFileExtension()).toFile());
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    } finally {
      takeScreenShot = false;
    }
  }
}
