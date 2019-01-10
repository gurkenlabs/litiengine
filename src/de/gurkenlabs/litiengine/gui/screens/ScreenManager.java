package de.gurkenlabs.litiengine.gui.screens;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.RenderComponent;

/**
 * The <code>ScreenManager</code> holds instances of all available screen and handles whenever a different <code>Screen</code> should be shown to the
 * player. It provides the
 * currently active Screen for the Game’s <code>RenderComponent</code> which calls the <code>Screen.render(Graphics2D)</code> method on every tick of
 * the <code>RenderLoop</code>.
 * Overwriting this method provides the ability to define a customized render pipeline that suits the need of a particular Screen implementation. With
 * the GameScreen, the LITIengine provides a simple default Screen implementation that renders the current <code>Environment</code> and all its
 * <code>GuiComponents</code>.
 * 
 * @see Screen
 * @see RenderComponent
 * @see GameScreen
 * @see Screen#render(java.awt.Graphics2D)
 * @see Game#renderLoop()
 */
public final class ScreenManager {
  private static final int SCREENCHANGETIMEOUT = 200;

  private final List<Consumer<Screen>> screenChangedConsumer;

  private final List<Screen> screens;

  private Screen currentScreen;

  private long lastScreenChange = 0;

  public ScreenManager() {
    this.screenChangedConsumer = new CopyOnWriteArrayList<>();
    this.screens = new CopyOnWriteArrayList<>();
  }

  public void add(final Screen screen) {
    screen.setWidth(Game.window().getWidth());
    screen.setHeight(Game.window().getHeight());
    this.screens.add(screen);

    if (this.current() == null) {
      this.display(screen);
    }
  }

  public void display(final Screen screen) {
    if (Game.hasStarted() && System.currentTimeMillis() - this.lastScreenChange < SCREENCHANGETIMEOUT) {
      return;
    }

    if (this.current() != null) {
      this.current().suspend();
    }

    if (screen != null && !this.screens.contains(screen)) {
      this.screens.add(screen);
    }

    this.currentScreen = screen;
    if (!Game.isInNoGUIMode() && this.current() != null) {
      this.current().prepare();
    }

    this.lastScreenChange = System.currentTimeMillis();
    for (final Consumer<Screen> consumer : this.screenChangedConsumer) {
      consumer.accept(this.current());
    }
  }

  public void display(final String screenName) {
    // if the screen is already displayed or there is no screen with the
    // specified name
    if (this.current() != null && this.current().getName().equalsIgnoreCase(screenName) || this.screens.stream().noneMatch(element -> element.getName().equalsIgnoreCase(screenName))) {
      // TODO: provide reasonable log, why the screen was not switched
      return;
    }

    Screen screen = this.get(screenName);
    if (screen == null) {
      return;
    }

    this.display(screen);
  }

  public Screen get(String screenName) {
    Optional<Screen> opt = this.screens.stream().filter(element -> element.getName().equalsIgnoreCase(screenName)).findFirst();
    return opt.orElse(null);
  }

  public Screen current() {
    return this.currentScreen;
  }

  public void onScreenChanged(final Consumer<Screen> screenConsumer) {
    if (!this.screenChangedConsumer.contains(screenConsumer)) {
      this.screenChangedConsumer.add(screenConsumer);
    }
  }

  public void remove(Screen screen) {
    this.screens.remove(screen);
    if (this.current() == screen) {
      if (!this.screens.isEmpty()) {
        this.display(this.screens.get(0));
      } else {
        this.display((Screen) null);
      }
    }
  }
}
