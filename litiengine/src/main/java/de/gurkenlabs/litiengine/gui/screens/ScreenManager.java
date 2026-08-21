package de.gurkenlabs.litiengine.gui.screens;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameWindow;
import de.gurkenlabs.litiengine.graphics.RenderComponent;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code ScreenManager} holds instances of all available screens and handles whenever a different {@code Screen}
 * should be shown to the player. It provides the currently active Screens for the Game's {@code RenderComponent} which
 * calls the {@code Screen.render(Graphics2D)} method on every tick of the {@code RenderLoop}. Overwriting this method
 * provides the ability to define a customized render pipeline that suits the need of a particular Screen
 * implementation. With the GameScreen, the LITIENGINE provides a simple default Screen implementation that renders the
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

  /** All screens currently being rendered, ordered by their screen layer. */
  private final List<Screen> activeScreens;

  private int changeCooldown = DEFAULT_CHANGE_COOLDOWN;
  private long lastScreenChange = 0;
  private boolean resolutionListenerRegistered = false;

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
    this.activeScreens = new CopyOnWriteArrayList<>();
  }

  /**
   * Adds the specified screen changed listener to receive events when the current screen was changed.
   *
   * @param listener
   *          The listener to add.
   */
  public void addScreenChangedListener(ScreenChangedListener listener) {
    this.screenChangedListeners.add(listener);
  }

  /**
   * Removes the specified screen changed listener.
   *
   * @param listener
   *          The listener to remove.
   */
  public void removeScreenChangedListener(ScreenChangedListener listener) {
    this.screenChangedListeners.remove(listener);
  }

  /**
   * Adds the specified screen instance to the manager.
   *
   * @param screen
   *          The screen to add.
   */
  public void add(final Screen screen) {
    this.ensureRegistered(screen);

    if (this.current() == null) {
      this.display(screen);
    }
  }

  /**
   * Removes the specified screen instance from the manager.
   *
   * @param screen
   *          The screen to remove.
   */
  public void remove(Screen screen) {
    final boolean wasCurrent = this.current() == screen;
    this.screens.remove(screen);
    if (this.activeScreens.contains(screen)) {
      this.removeScreen(screen);
    }
    if (wasCurrent && this.activeScreens.isEmpty()) {
      if (!this.screens.isEmpty()) {
        this.display(this.screens.get(0));
      } else {
        this.display((Screen) null);
      }
    }
  }

  /**
   * Displays the specified screen by setting it as the (sole) active screen, replacing the previously active screen.
   *
   * @param screen
   *          The screen to be displayed.
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
    for (Screen activeScreen : new ArrayList<>(this.activeScreens)) {
      this.removeScreen(activeScreen);
    }

    if (screen != null) {
      this.ensureRegistered(screen);
      this.activeScreens.add(screen);
      this.sortActiveScreens();

      if (!Game.isInNoGUIMode()) {
        screen.prepare();
      }
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
   * @param screenName
   *          The name of the screen to be displayed.
   */
  public void display(final String screenName) {
    if (this.current() != null
        && this.current().getName().equalsIgnoreCase(screenName)
        && this.activeScreens.size() == 1) {
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
   * Replaces the specified old active screen with the new screen.
   *
   * @param oldScreen The screen to remove from the active screens (may be {@code null}).
   * @param newScreen The screen to add as an active screen (may be {@code null}).
   */
  public void replaceDisplay(final Screen oldScreen, final Screen newScreen) {
    if (Game.hasStarted() && Game.time().since(this.lastScreenChange) < this.getChangeCooldown()) {
      log.log(
          Level.INFO,
          "Skipping displaying of screen {0} because screen changing is currently on cooldown.",
          newScreen != null ? newScreen.getName() : "");
      return;
    }

    final Screen previous = this.current();

    final boolean removedOldScreen = this.removeActiveScreen(oldScreen);

    if (newScreen == null) {
      this.lastScreenChange = Game.loop().getTicks();
      final ScreenChangedEvent event = new ScreenChangedEvent(this.current(), previous);
      for (final ScreenChangedListener listener : this.screenChangedListeners) {
        listener.changed(event);
      }
      return;
    }

    if (this.activeScreens.contains(newScreen)) {
      if (!removedOldScreen) {
        return;
      }

      this.lastScreenChange = Game.loop().getTicks();
      final ScreenChangedEvent event = new ScreenChangedEvent(this.current(), previous);
      for (final ScreenChangedListener listener : this.screenChangedListeners) {
        listener.changed(event);
      }
      return;
    }

    this.ensureRegistered(newScreen);
    this.activeScreens.add(newScreen);
    this.sortActiveScreens();

    if (!Game.isInNoGUIMode()) {
      newScreen.prepare();
    }

    this.lastScreenChange = Game.loop().getTicks();

    final ScreenChangedEvent event = new ScreenChangedEvent(this.current(), previous);
    for (final ScreenChangedListener listener : this.screenChangedListeners) {
      listener.changed(event);
    }
  }

  /**
   * Replaces the screen with the specified old name by the screen with the specified new name.
   *
   * @param oldScreenName The name of the screen to remove (may be {@code null}).
   * @param newScreenName The name of the screen to add.
   */
  public void replaceDisplay(final String oldScreenName, final String newScreenName) {
    Screen oldScreen = oldScreenName != null ? this.getActive(oldScreenName) : null;

    Screen newScreen = this.get(newScreenName);
    if (newScreen == null) {
      log.log(Level.WARNING,
          "Could not display the screen {0} because there is no screen with the specified name.",
          newScreenName);
      return;
    }

    this.replaceDisplay(oldScreen, newScreen);
  }

  /**
   * Adds the specified screen to the active screens without removing any existing ones.
   *
   * @param screen The screen to add as an active screen.
   */
  public void addDisplay(final Screen screen) {
    this.replaceDisplay(null, screen);
  }

  /**
   * Adds the screen with the specified name to the active screens without removing any existing ones.
   *
   * @param screenName The name of the screen to add.
   */
  public void addDisplay(final String screenName) {
    this.replaceDisplay((String) null, screenName);
  }

  /**
   * Removes the specified screen from the active screens.
   *
   * @param screen The screen to remove from the active screens (may be {@code null}).
   */
  public void removeScreen(final Screen screen) {
    this.removeActiveScreen(screen);
  }

  /**
   * Removes the screen with the specified name from the active screens.
   *
   * @param screenName The name of the screen to remove.
   */
  public void removeScreen(final String screenName) {
    Screen screen = this.getActive(screenName);
    this.removeScreen(screen);
  }

  /**
   * Gets the screen by its name.
   *
   * @param screenName
   *          The name of the screen.
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
   * Gets the currently active screens being rendered. The list is ordered by screen layer (ascending).
   *
   * @return An unmodifiable list of all currently active screens.
   */
  public List<Screen> getActiveScreens() {
    return Collections.unmodifiableList(this.activeScreens);
  }

  /**
   * Gets the currently active screen that is being rendered by the {@code RenderComponent}.
   * When multiple screens are active, returns the first one (lowest layer).
   *
   * @return The currently active screen.
   * @see GameWindow#getRenderComponent()
   * @see RenderComponent#render()
   */
  public Screen current() {
    if (this.activeScreens.isEmpty()) {
      return null;
    }
    return this.activeScreens.get(0);
  }

  /**
   * Gets the screen change cooldown which is used to ensure that screens cannot be switched too quickly while the game is
   * running.
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
   * @param changeCooldown
   *          The cooldown for changing screens.
   */
  public void setChangeCooldown(int changeCooldown) {
    this.changeCooldown = changeCooldown;
  }

  private Screen getActive(String screenName) {
    return this.activeScreens.stream()
        .filter(s -> s.getName().equalsIgnoreCase(screenName))
        .findFirst()
        .orElse(null);
  }

  private void onWindowResolutionChanged(Dimension newResolution) {
    for (Screen screen : this.screens) {
      screen.onResolutionChanged(newResolution);
    }
  }

  private void ensureRegistered(Screen screen) {
    if (screen == null || this.screens.contains(screen)) {
      return;
    }

    screen.setWidth(Game.window().getWidth());
    screen.setHeight(Game.window().getHeight());
    this.screens.add(screen);

    if (!this.resolutionListenerRegistered && !Game.isInNoGUIMode()) {
      Game.window().onResolutionChanged(this::onWindowResolutionChanged);
      this.resolutionListenerRegistered = true;
    }
  }

  private boolean removeActiveScreen(Screen screen) {
    if (screen == null || !this.activeScreens.contains(screen)) {
      return false;
    }

    screen.suspend();
    this.activeScreens.remove(screen);
    return true;
  }

  void sortActiveScreens() {
    this.activeScreens.sort(Comparator.comparingInt(Screen::getScreenLayer));
  }
}
