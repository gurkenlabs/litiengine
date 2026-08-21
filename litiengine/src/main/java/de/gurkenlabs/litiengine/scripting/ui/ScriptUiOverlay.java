package de.gurkenlabs.litiengine.scripting.ui;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.scripting.Subscription;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** Manages transient UI elements, floating combat text, banners, and script-driven overlays. */
public final class ScriptUiOverlay implements IUpdateable, Subscription {
  private final List<FloatingText> floatingTexts = new CopyOnWriteArrayList<>();
  private final List<ScreenText> screenTexts = new CopyOnWriteArrayList<>();
  private Banner announcement;
  private boolean attached;
  private boolean closed;

  public ScriptUiOverlay() {
    this.ensureAttached();
  }

  public FloatingText floatText(String text, Point2D location, Color color) {
    return this.floatText(text, location, color, null, 1000, -20.0);
  }

  public FloatingText floatText(String text, IEntity entity, Color color) {
    Objects.requireNonNull(entity, "Entity must not be null.");
    Point2D loc = new Point2D.Double(entity.getCenter().getX(), entity.getY() - 4);
    return this.floatText(text, loc, color, null, 1000, -20.0);
  }

  public FloatingText floatText(String text, Point2D location, Color color, Font font, int durationMs, double velocityY) {
    Objects.requireNonNull(location, "Location must not be null.");
    FloatingText item = new FloatingText(text, location, color, font, durationMs, velocityY);
    this.floatingTexts.add(item);
    return item;
  }

  public ScreenText drawScreenText(String text, double screenX, double screenY, Color color) {
    return this.drawScreenText(text, screenX, screenY, color, null, 0);
  }

  public ScreenText drawScreenText(String text, double screenX, double screenY, Color color, Font font, int durationMs) {
    ScreenText item = new ScreenText(text, screenX, screenY, color, font, durationMs);
    this.screenTexts.add(item);
    return item;
  }

  public void showBanner(String title, String subtitle, int durationMs) {
    this.announcement = new Banner(title, subtitle, durationMs);
  }

  public void clear() {
    this.floatingTexts.clear();
    this.screenTexts.clear();
    this.announcement = null;
  }

  public List<FloatingText> getFloatingTexts() {
    return List.copyOf(this.floatingTexts);
  }

  public List<ScreenText> getScreenTexts() {
    return List.copyOf(this.screenTexts);
  }

  @Override
  public void update() {
    if (this.closed) return;
    int delta = (int) Game.loop().getDeltaTime();
    if (delta <= 0) delta = 1000 / Math.max(1, Game.loop().getTickRate());

    for (FloatingText text : this.floatingTexts) {
      text.update(delta);
      if (text.isFinished()) {
        this.floatingTexts.remove(text);
      }
    }

    for (ScreenText screenText : this.screenTexts) {
      if (screenText.duration > 0) {
        screenText.elapsed += delta;
        if (screenText.elapsed >= screenText.duration) {
          this.screenTexts.remove(screenText);
        }
      }
    }

    if (this.announcement != null) {
      this.announcement.elapsed += delta;
      if (this.announcement.elapsed >= this.announcement.duration) {
        this.announcement = null;
      }
    }
  }

  public void render(Graphics2D g) {
    if (this.closed) return;

    // Render world-space floating texts
    for (FloatingText text : this.floatingTexts) {
      float progress = text.getProgress();
      int alpha = Math.clamp((int) ((1f - progress) * text.getColor().getAlpha()), 0, 255);
      Color renderColor = new Color(text.getColor().getRed(), text.getColor().getGreen(), text.getColor().getBlue(), alpha);
      Color outlineColor = new Color(0, 0, 0, alpha);

      Font prevFont = g.getFont();
      if (text.getFont() != null) {
        g.setFont(text.getFont());
      }

      Point2D screenLoc = Game.world().camera().getViewportLocation(text.getLocation().getX(), text.getLocation().getY());
      g.setColor(renderColor);
      TextRenderer.renderWithOutline(g, text.getText(), screenLoc.getX(), screenLoc.getY(), outlineColor);
      g.setFont(prevFont);
    }

    // Render screen-space HUD texts
    for (ScreenText screenText : this.screenTexts) {
      Font prevFont = g.getFont();
      if (screenText.font != null) {
        g.setFont(screenText.font);
      }
      g.setColor(screenText.color != null ? screenText.color : Color.WHITE);
      TextRenderer.renderWithOutline(g, screenText.text, screenText.x, screenText.y, Color.BLACK);
      g.setFont(prevFont);
    }

    // Render screen banner if active
    if (this.announcement != null && !Game.isInNoGUIMode()) {
      float progress = (float) this.announcement.elapsed / (float) this.announcement.duration;
      int alpha = 255;
      if (progress > 0.8f) {
        alpha = Math.clamp((int) ((1f - (progress - 0.8f) / 0.2f) * 255), 0, 255);
      }
      Color bannerColor = new Color(255, 255, 255, alpha);
      Color outline = new Color(0, 0, 0, alpha);
      double screenWidth = Game.window().getResolution().getWidth();
      double centerY = Game.window().getResolution().getHeight() * 0.3;

      g.setColor(bannerColor);
      TextRenderer.renderWithOutline(g, this.announcement.title, screenWidth * 0.5, centerY, outline);
      if (this.announcement.subtitle != null && !this.announcement.subtitle.isBlank()) {
        TextRenderer.renderWithOutline(g, this.announcement.subtitle, screenWidth * 0.5, centerY + 24, outline);
      }
    }
  }

  private final java.util.function.Consumer<Graphics2D> renderConsumer = this::render;

  @Override
  public void close() {
    this.closed = true;
    this.clear();
    if (this.attached) {
      if (Game.loop() != null) {
        Game.loop().detach(this);
      }
      if (!Game.isInNoGUIMode() && Game.window() != null && Game.window().getRenderComponent() != null) {
        Game.window().getRenderComponent().removeRenderedConsumer(this.renderConsumer);
      }
      this.attached = false;
    }
  }

  private void ensureAttached() {
    if (this.attached) return;
    if (Game.loop() != null) {
      Game.loop().attach(this);
    }
    if (!Game.isInNoGUIMode() && Game.window() != null && Game.window().getRenderComponent() != null) {
      Game.window().getRenderComponent().onRendered(this.renderConsumer);
    }
    this.attached = true;
  }

  public static final class ScreenText {
    private final String text;
    private final double x;
    private final double y;
    private final Color color;
    private final Font font;
    private final int duration;
    private int elapsed;

    ScreenText(String text, double x, double y, Color color, Font font, int duration) {
      this.text = text == null ? "" : text;
      this.x = x;
      this.y = y;
      this.color = color;
      this.font = font;
      this.duration = duration;
    }

    public String getText() { return this.text; }
    public double getX() { return this.x; }
    public double getY() { return this.y; }
    public Color getColor() { return this.color; }
  }

  private static final class Banner {
    private final String title;
    private final String subtitle;
    private final int duration;
    private int elapsed;

    Banner(String title, String subtitle, int duration) {
      this.title = title == null ? "" : title;
      this.subtitle = subtitle;
      this.duration = Math.max(100, duration);
    }
  }
}
