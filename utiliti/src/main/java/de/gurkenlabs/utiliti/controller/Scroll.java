package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import java.awt.geom.Point2D;
import java.util.EventListener;
import javax.swing.SwingUtilities;

/** Synchronizes camera focus with conditional, extent-aware viewport scrollbars. */
public final class Scroll {
  private static final int MODEL_MAX = 1_000_000;
  private static final int SCROLL_SPEED = 50;

  private static ScrollHandler verticalHandler;
  private static ScrollHandler horizontalHandler;
  private static float currentScrollSpeed = SCROLL_SPEED;
  private static boolean updating;

  private Scroll() {}

  public static void up() {
    panBy(0, -currentScrollSpeed);
  }

  public static void down() {
    panBy(0, currentScrollSpeed);
  }

  public static void left() {
    panBy(-currentScrollSpeed, 0);
  }

  public static void right() {
    panBy(currentScrollSpeed, 0);
  }

  private static void panBy(double deltaX, double deltaY) {
    Point2D currentFocus = Game.world().camera().getFocus();
    AxisModel horizontal = horizontalModel();
    AxisModel vertical = verticalModel();
    double x = deltaX != 0 && horizontal != null && horizontal.visible()
        ? Math.clamp(currentFocus.getX() + deltaX, horizontal.minimumFocus(), horizontal.maximumFocus())
        : currentFocus.getX();
    double y = deltaY != 0 && vertical != null && vertical.visible()
        ? Math.clamp(currentFocus.getY() + deltaY, vertical.minimumFocus(), vertical.maximumFocus())
        : currentFocus.getY();
    if (x == currentFocus.getX() && y == currentFocus.getY()) {
      return;
    }
    Editor.instance().getMapComponent().exitFitMode();
    scroll(x, y);
  }

  public static void scroll(double x, double y) {
    if (Editor.instance().getMapComponent().isLoading()) {
      return;
    }
    Game.world().camera().setFocus(x, y);
  }

  public static ScrollHandler getVerticalHandler() {
    return verticalHandler;
  }

  public static ScrollHandler getHorizontalHandler() {
    return horizontalHandler;
  }

  public static void init(ScrollHandler vertical, ScrollHandler horizontal) {
    verticalHandler = vertical;
    horizontalHandler = horizontal;

    vertical.onScrolled(handler -> {
      if (updating) {
        return;
      }
      AxisModel model = verticalModel();
      if (model == null || !model.visible()) {
        return;
      }
      Editor.instance().getMapComponent().exitFitMode();
      scroll(Game.world().camera().getFocus().getX(), model.focusForValue(handler.getValue()));
    });

    horizontal.onScrolled(handler -> {
      if (updating) {
        return;
      }
      AxisModel model = horizontalModel();
      if (model == null || !model.visible()) {
        return;
      }
      Editor.instance().getMapComponent().exitFitMode();
      scroll(model.focusForValue(handler.getValue()), Game.world().camera().getFocus().getY());
    });

    Game.world().camera().onZoom(event -> updateScrollHandlers(true));
    Game.world().camera().onFocus(event -> updateScrollHandlers(false));
    Game.world().onLoaded(event -> updateScrollHandlers(true));
    updateScrollHandlers();
  }

  public static void updateScrollHandlers() {
    updateScrollHandlers(true);
  }

  private static void updateScrollHandlers(boolean normalizeFocus) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> updateScrollHandlers(normalizeFocus));
      return;
    }
    if (updating || verticalHandler == null || horizontalHandler == null) {
      return;
    }

    updating = true;
    try {
      AxisModel horizontal = horizontalModel();
      AxisModel vertical = verticalModel();
      if (horizontal == null || vertical == null) {
        horizontalHandler.setVisible(false);
        verticalHandler.setVisible(false);
        return;
      }

      currentScrollSpeed = SCROLL_SPEED / Math.max(0.01f, Game.world().camera().getZoom());
      if (normalizeFocus && !Editor.instance().getMapComponent().isLoading()) {
        Point2D focus = Game.world().camera().getFocus();
        double normalizedX = horizontal.visible()
            ? Math.clamp(focus.getX(), horizontal.minimumFocus(), horizontal.maximumFocus())
            : horizontal.minimumFocus();
        double normalizedY = vertical.visible()
            ? Math.clamp(focus.getY(), vertical.minimumFocus(), vertical.maximumFocus())
            : vertical.minimumFocus();
        if (Math.abs(normalizedX - focus.getX()) > 0.02 || Math.abs(normalizedY - focus.getY()) > 0.02) {
          Game.world().camera().setFocus(normalizedX, normalizedY);
        }
      }
      apply(horizontalHandler, horizontal);
      apply(verticalHandler, vertical);
    } finally {
      updating = false;
    }
  }

  private static void apply(ScrollHandler handler, AxisModel model) {
    handler.setValues(model.value(), model.extent(), 0, MODEL_MAX);
    handler.setVisible(model.visible());
  }

  private static AxisModel horizontalModel() {
    IMap map = currentMap();
    if (map == null || Game.window() == null || Game.world().camera() == null) {
      return null;
    }
    double renderScale = Math.max(0.0001, Game.world().camera().getRenderScale());
    double viewport = Game.window().getRenderComponent().getWidth() / renderScale;
    double padding = 32 * Editor.preferences().getUiScale() / renderScale;
    return AxisModel.create(
        map.getSizeInPixels().getWidth(), viewport, map.getTileWidth(), padding,
        Game.world().camera().getFocus().getX());
  }

  private static AxisModel verticalModel() {
    IMap map = currentMap();
    if (map == null || Game.window() == null || Game.world().camera() == null) {
      return null;
    }
    double renderScale = Math.max(0.0001, Game.world().camera().getRenderScale());
    double viewport = Game.window().getRenderComponent().getHeight() / renderScale;
    double padding = 32 * Editor.preferences().getUiScale() / renderScale;
    return AxisModel.create(
        map.getSizeInPixels().getHeight(), viewport, map.getTileHeight(), padding,
        Game.world().camera().getFocus().getY());
  }

  private static IMap currentMap() {
    return Game.world() != null && Game.world().environment() != null
        ? Game.world().environment().getMap()
        : null;
  }

  static record AxisModel(
      boolean visible, int value, int extent, double minimumFocus, double maximumFocus) {
    static AxisModel create(
        double contentSize, double viewportSize, double bleed, double padding, double focus) {
      double margin = Math.max(0, bleed) + Math.max(0, padding);
      double expandedSize = Math.max(1, contentSize + margin * 2);
      if (contentSize + Math.max(0, padding) * 2 <= viewportSize + 0.5) {
        return new AxisModel(false, 0, MODEL_MAX, contentSize / 2, contentSize / 2);
      }

      double minimumFocus = -margin + viewportSize / 2;
      double maximumFocus = contentSize + margin - viewportSize / 2;
      int extent = Math.max(1, Math.min(MODEL_MAX - 1,
          (int) Math.round(viewportSize / expandedSize * MODEL_MAX)));
      int track = MODEL_MAX - extent;
      double ratio = (focus - minimumFocus) / Math.max(0.0001, maximumFocus - minimumFocus);
      int value = (int) Math.round(Math.clamp(ratio, 0, 1) * track);
      return new AxisModel(true, value, extent, minimumFocus, maximumFocus);
    }

    double focusForValue(int value) {
      int track = MODEL_MAX - this.extent;
      if (track <= 0 || this.maximumFocus <= this.minimumFocus) {
        return this.minimumFocus;
      }
      double ratio = Math.clamp(value / (double) track, 0, 1);
      return this.minimumFocus + ratio * (this.maximumFocus - this.minimumFocus);
    }
  }

  public interface ScrollHandler {
    int getValue();
    void setValues(int value, int extent, int min, int max);
    void setVisible(boolean visible);
    void onScrolled(ScrollHandlerEventListener listener);
  }

  public interface ScrollHandlerEventListener extends EventListener {
    void scrolled(ScrollHandler handler);
  }
}
