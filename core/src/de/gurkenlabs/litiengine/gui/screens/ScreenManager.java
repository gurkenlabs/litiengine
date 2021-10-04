package de.gurkenlabs.litiengine.gui.screens;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameWindow;
import de.gurkenlabs.litiengine.graphics.RenderComponent;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code ScreenManager} holds instances of all available screens and handles whenever a
 * different {@code Screen} should be shown to the player. It provides the currently active Screen
 * for the Game’s {@code RenderComponent} which calls the {@code Screen.render(Graphics2D)} method
 * on every tick of the {@code RenderLoop}. Overwriting this method provides the ability to define a
 * customized render pipeline that suits the need of a particular Screen implementation. With the
 * GameScreen, the LITIENGINE provides a simple default Screen implementation that renders the
 * current {@code Environment} and all its {@code GuiComponents}.
 *
 * @see Screen
 * @see RenderComponent
 * @see GameScreen
 * @see Screen#render(java.awt.Graphics2D)
 */
public final class ScreenManager {
  private static final Logger log = Logger.getLogger(ScreenManager.class.getName());
  private static final int DEFAULT_CHANGE_COOLDOWN = 200;

  private final List<ScreenChangedListener> screenChangedListeners;

  private final List<Screen> screens;

  private Screen currentScreen;

  private int changeCooldown = DEFAULT_CHANGE_COOLDOWN;
  private long lastScreenChange = 0;

  /**
   * <b>You should never call this manually! Instead use the {@code Game.screens()} instance.</b>
   *
   * @see Game#screens()
   */
  public ScreenManager() {
    if (Game.screens() != null) {
      throw new UnsupportedOperationException(
          "Never initialize a ScreenManager manually. Use Game.screens() instead.");
    }

    this.screenChangedListeners = new CopyOnWriteArrayList<>();
    this.screens = new CopyOnWriteArrayList<>();
  }

  /**
   * Adds the specified screen changed listener to receive events when the current screen was
   * changed.
   *
   * @param listener The listener to add.
   */
  public void addScreenChangedListener(ScreenChangedListener listener) {
    this.screenChangedListeners.add(listener);
  }

  /**
   * Removes the specified screen changed listener.
   *
   * @param listener The listener to remove.
   */
  public void removeScreenChangedListener(ScreenChangedListener listener) {
    this.screenChangedListeners.remove(listener);
  }

  /**
   * Adds the specified screen instance to the manager.
   *
   * @param screen The screen to add.
   */
  public void add(final Screen screen) {
    screen.setWidth(Game.window().getWidth());
    screen.setHeight(Game.window().getHeight());
    this.screens.add(screen);

    if (this.current() == null) {
      this.display(screen);
    }
  }

  /**
   * Removes the specified screen instance from the manager.
   *
   * @param screen The screen to remove.
   */
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

  /**
   * Displays the specified screen by setting
   *
   * @param screen The screen to be displayed.
   */
  public void display(final Screen screen) {
    if (Game.hasStarted() && Game.time().since(this.lastScreenChange) < this.getChangeCooldown()) {
      log.log(
          Level.INFO,
          "Skipping displaying of screen {0} because screen changing is currently on cooldown.",
          screen != null ? screen.getName() : "");
      return;
    }

    final Screen previous = this.current();
    if (previous != null) {
      previous.suspend();
    }

    if (screen != null && !this.screens.contains(screen)) {
      this.screens.add(screen);
    }

    this.currentScreen = screen;
    if (!Game.isInNoGUIMode() && this.current() != null) {
      this.current().prepare();
    }

    this.lastScreenChange = Game.loop().getTicks();

    final ScreenChangedEvent event = new ScreenChangedEvent(this.current(), previous);
    for (final ScreenChangedListener listener : this.screenChangedListeners) {
      listener.changed(event);
    }
  }

  /**
   * Displays the {@code Screen} with the specified name.
   *
   * @param screenName The name of the screen to be displayed.
   */
  public void display(final String screenName) {
    if (this.current() != null && this.current().getName().equalsIgnoreCase(screenName)) {
      log.log(
          Level.INFO,
          "Skipping displaying of screen {0} because it is already the current screen.",
          screenName);
      return;
    }

    if (this.screens.stream()
        .noneMatch(element -> element.getName().equalsIgnoreCase(screenName))) {
      log.log(
          Level.WARNING,
          "Could not display the screen {0} because there is no screen with the specified name.",
          screenName);
      return;
    }

    Screen screen = this.get(screenName);
    if (screen == null) {
      return;
    }

    this.display(screen);
  }

  /**
   * Gets the screen by its name.
   *
   * @param screenName The name of the screen.
   * @return The
   */
  public Screen get(String screenName) {
    Optional<Screen> opt =
        this.screens.stream()
            .filter(element -> element.getName().equalsIgnoreCase(screenName))
            .findFirst();
    return opt.orElse(null);
  }

  /**
   * Gets all screens of the game.
   *
   * @return All screens that have been previously added to this instance.
   * @see #add(Screen)
   */
  public Collection<Screen> getAll() {
    return this.screens;
  }

  /**
   * Gets the currently active screen that is being rendered by the {@code RenderComponent}.
   *
   * @return The currently active screen.
   * @see GameWindow#getRenderComponent()
   * @see RenderComponent#render()
   */
  public Screen current() {
    return this.currentScreen;
  }

  /**
   * Gets the screen change cooldown which is used to ensure that screens cannot be switched too
   * quickly while the game is running.
   *
   * @return The current change timeout for screens.
   * @see #DEFAULT_CHANGE_COOLDOWN
   * @see Game#hasStarted()
   */
  public int getChangeCooldown() {
    return this.changeCooldown;
  }

  /**
   * Sets the cooldown for changing screens.
   *
   * @param changeCooldown The cooldown for changing screens.
   */
  public void setChangeCooldown(int changeCooldown) {
    this.changeCooldown = changeCooldown;
  }
}
