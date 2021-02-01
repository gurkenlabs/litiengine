package com.litiengine.utiliti.handlers;

import com.litiengine.Game;
import com.litiengine.utiliti.components.Editor;

import java.awt.geom.Point2D;
import java.util.EventListener;

public final class Scroll {
  private static final int SCROLL_MAX = 100;
  private static final int SCROLL_SPEED = 50;

  private static ScrollHandler verticalHandler;
  private static ScrollHandler horizontalHandler;

  private static float currentScrollSize;
  private static float currentScrollSpeed;

  private static boolean updating;

  private Scroll() {
  }

  public static void up() {
    final Point2D currentFocus = Game.world().camera().getFocus();
    scroll(currentFocus.getX(), currentFocus.getY() - currentScrollSpeed);
  }

  public static void down() {
    final Point2D currentFocus = Game.world().camera().getFocus();
    scroll(currentFocus.getX(), currentFocus.getY() + currentScrollSpeed);
  }

  public static void left() {
    final Point2D currentFocus = Game.world().camera().getFocus();
    scroll(currentFocus.getX() - currentScrollSpeed, currentFocus.getY());
  }

  public static void right() {
    final Point2D currentFocus = Game.world().camera().getFocus();
    scroll(currentFocus.getX() + currentScrollSpeed, currentFocus.getY());
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

    getVerticalHandler().onScrolled((handler) -> {
      final double y = getScrollValue(handler, Game.world().environment().getMap().getSizeInPixels().height);

      scroll(Game.world().camera().getFocus().getX(), y);
    });

    getHorizontalHandler().onScrolled((handler) -> {
      final double x = getScrollValue(handler, Game.world().environment().getMap().getSizeInPixels().width);

      scroll(x, Game.world().camera().getFocus().getY());
    });

    Game.world().camera().onZoom(e -> updateScrollHandlers());

    Game.world().camera().onFocus(e -> updateScrollHandlers());

    Game.world().onLoaded(e -> updateScrollHandlers());
  }

  private static void updateScrollHandlers() {
    if (Game.world().environment() == null || updating) {
      return;
    }

    updating = true;

    try {
      double relativeX = Game.world().camera().getFocus().getX() / Game.world().environment().getMap().getSizeInPixels().width;
      double relativeY = Game.world().camera().getFocus().getY() / Game.world().environment().getMap().getSizeInPixels().height;

      // decouple the scrollbar from the environment
      currentScrollSize = Math.round(SCROLL_MAX * Math.sqrt(Game.world().camera().getRenderScale()));
      currentScrollSpeed = SCROLL_SPEED / Game.world().camera().getZoom();

      getHorizontalHandler().setMinimum(0);
      getHorizontalHandler().setMaximum((int) currentScrollSize);
      getVerticalHandler().setMinimum(0);
      getVerticalHandler().setMaximum((int) currentScrollSize);

      int valueX = (int) (relativeX * currentScrollSize);
      int valueY = (int) (relativeY * currentScrollSize);

      getHorizontalHandler().setValue(valueX);
      getVerticalHandler().setValue(valueY);
    } finally {
      updating = false;
    }
  }

  private static double getScrollValue(ScrollHandler scrollHandler, double actualSize) {
    double currentValue = scrollHandler.getValue() / currentScrollSize;
    return currentValue * actualSize;
  }

  public interface ScrollHandler {
    int getMinimum();

    int getMaximum();

    int getValue();

    void setMinimum(int min);

    void setMaximum(int max);

    void setValue(int value);

    void onScrolled(ScrollHandlerEventListener listener);
  }

  public interface ScrollHandlerEventListener extends EventListener {
    void scrolled(ScrollHandler handler);
  }
}
