package de.gurkenlabs.litiengine.gui.screens;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameWindow;
import de.gurkenlabs.litiengine.graphics.RenderComponent;

/**
 * The <code>ScreenManager</code> holds instances of all available screens and handles whenever a different <code>Screen</code> should be shown to the
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
 */
public final class ScreenManager {
  private static final Logger log = Logger.getLogger(ScreenManager.class.getName());
  @Deprecated
  private static final int DEFAULT_CHANGE_COOLDOWN = 200;

  private final List<ScreenChangedListener> screenChangedListeners;

  private final List<Screen> screens;

  // private Screen currentScreen; //OBSOLETE!
  private CopyOnWriteArrayList<Screen> activeScreens;

  @Deprecated
  private int changeCooldown = DEFAULT_CHANGE_COOLDOWN;
  @Deprecated
  private long lastScreenChange = 0;

  /**
   * <p>
   * <b>You should never call this manually! Instead use the <code>Game.screens()</code> instance.</b>
   * </p>
   * 
   * @see Game#screens()
   */
  public ScreenManager() {
    if (Game.screens() != null) {
      throw new UnsupportedOperationException("Never initialize a ScreenManager manually. Use Game.screens() instead.");
    }

    this.screenChangedListeners = new CopyOnWriteArrayList<>();
    this.screens = new CopyOnWriteArrayList<>();
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
   * Removes a screen from the list of active screens by screen name
   * @param screenName name of screen to remove
   * @return whatever a screen was removed from the list of active screens
   */
  public boolean hideScreen(String screenName) {
    if (activeScreens.size() <= 1) {
      log.log(Level.SEVERE, "An attempt to remove the last screen from the game has been made. This cannot be allowed!");
      return false;
    }
    for (int i = 0; i < activeScreens.size(); i++) {
      if(activeScreens.get(i).getName().equalsIgnoreCase(screenName)) {
        activeScreens.remove(i);
        return true;
      }
    }
    return false;
  }

  /**
   * Removes a screen from the list of active screens by reference to the Screen object
   * @param screen The screen object to remove.
   * @return Whatever the screen object was in the list and was removed.
   */
  public boolean hideScreen(Screen screen) {
    if (activeScreens.remove(screen)) {
      screen.suspend();
      return true;
      //TODO: should an event trigger to alert game objects that a screen is now being hidden?
    }
    return false;
  }

  /**
   * Displays a screen based on screen name
   * @param screenName name of screen to display
   * @return whatever the screen was added
   */
  public boolean displayScreen(String screenName) {
    if (activeScreens.stream().anyMatch( s -> s.getName().equalsIgnoreCase(screenName))) {
        log.log(Level.INFO, "Skipping displaying of screen {0} because it is already an active screen.", screenName);
        return false;
    }
    if (this.screens.stream().noneMatch(element -> element.getName().equalsIgnoreCase(screenName))) {
      log.log(Level.WARNING, "Could not display the screen {0} because there is no screen with the specified name.", screenName);
      return false;
    }
    Screen screen = this.get(screenName);
    if (screen == null) {
      return false;
    }

    return this.displayScreen(screen);
  }

  /**
   * Displays a screen based on reference to screen object.
   * If the Screen object is missing from the list of known Screens, add it.
   * @param screen The screen object to display
   * @return Whatever the Screen was added to activeDisplays.
   */
  public boolean displayScreen(Screen screen) {
    if(screen == null) {
      log.log(Level.WARNING, "Could not display requested screen because it has a null value.");
      return false;
    }
    if(activeScreens.contains(screen)){
      log.log(Level.INFO, "Skipping displaying of screen {0} because it is already an active screen.", screen.getName());
      return false;
    }
    if (!this.screens.contains(screen)) {
      this.screens.add(screen);
    }

    //Add the screen to the list of active screens
    activeScreens.add(screen);
    //Sort the screens by layer
    activeScreens.sort(Comparator.comparing(Screen::getLayer));
    if(!Game.isInNoGUIMode()) {
      screen.prepare();
    }
    return true;
    //TODO: should an event trigger to alert game objects that a new screen is being displayed?
  }

  /**
   * Replaces / swaps one screen for another, by string
   * @param newScreenName name of new screen to display
   * @param oldScreenName name of old screen to hide
   * @return Whatever the screen were replaced.
   */
  public boolean replaceScreen(String newScreenName, String oldScreenName) {
    Screen newScreen = get(newScreenName);
    Screen oldScreen = getActive(oldScreenName);
    return replaceScreen(newScreen, oldScreen);
  }

  public boolean replaceScreen(Screen newScreen, Screen oldScreen) {
    if(newScreen == null) {
      log.log(Level.WARNING, "Could not display requested screen because it has a null value.");
      return false;
    }

    //If the new screen was added without problem,
    //and the old screen is either null or was removed without problem..
    boolean success = displayScreen(newScreen) && (oldScreen == null || hideScreen(oldScreen));
    if(!success) {
      //If the operation fails, revert the change, and report the error to the log.
      hideScreen(newScreen);
      displayScreen(oldScreen);
      log.log(Level.WARNING, "unable to replace screen {0} with screen {1}. Operation reverted.", new String[] {oldScreen.getName(), newScreen.getName()});
      return false;
    }

    //Now trigger the ScreenChangedEvent.
    final ScreenChangedEvent event = new ScreenChangedEvent(newScreen, oldScreen);
    for (final ScreenChangedListener listener : this.screenChangedListeners) {
      listener.changed(event);
    }
    return true;
  }


  /**
   * Adds the specified screen instance to the manager.
   * 
   * @param screen
   *          The screen to add.
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
   * @param screen
   *          The screen to remove.
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
   * @deprecated  please use {@link #displayScreen(Screen)} or {@link #replaceScreen(Screen, Screen)} instead
   *              If any screen is active, redirects to {@link #replaceScreen(Screen, Screen)} with {@link #activeScreens}.get(0) as oldScreen
   *              else redirects to {@link #displayScreen(Screen)}
   * @param screen
   *          The screen to be displayed.
   */
  @Deprecated
  public void display(final Screen screen) {
    if(activeScreens.size() < 1)
      displayScreen(screen);
    else
      replaceScreen(screen, activeScreens.get(0));

    /*if (Game.hasStarted() && Game.time().since(this.lastScreenChange) < this.getChangeCooldown()) {
      log.log(Level.INFO, "Skipping displaying of screen {0} because screen changing is currently on cooldown.", screen != null ? screen.getName() : "");
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
    }*/
  }

  /**
   * Displays the <code>Screen</code> with the specified name.
   *
   * @deprecated please use {@link #displayScreen(String)} or {@link #replaceScreen(String, String)} instead
   *
   * @param screenName
   *          The name of the screen to be displayed.
   */
  @Deprecated
  public void display(final String screenName) {
    if (this.current() != null && this.current().getName().equalsIgnoreCase(screenName)) {
      log.log(Level.INFO, "Skipping displaying of screen {0} because it is already the current screen.", screenName);
      return;
    }

    if (this.screens.stream().noneMatch(element -> element.getName().equalsIgnoreCase(screenName))) {
      log.log(Level.WARNING, "Could not display the screen {0} because there is no screen with the specified name.", screenName);
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
   * @param screenName
   *          The name of the screen.
   * @return The requested screen or null
   */
  public Screen get(String screenName) {
    Optional<Screen> opt = this.screens.stream().filter(element -> element.getName().equalsIgnoreCase(screenName)).findFirst();
    return opt.orElse(null);
  }

  /**
   * Gets an active screen by name
   * @param screenName
   *            The name of the screen
   * @return The requested screen or null
   */
  public Screen getActive(String screenName) {
    Optional<Screen> opt = this.activeScreens.stream().filter(element -> element.getName().equalsIgnoreCase(screenName)).findFirst();
    return opt.orElse(null);
  }

  /**
   * Gets all screens of the game.
   * 
   * @return All screens that have been previously added to this instance.
   * 
   * @see #add(Screen)
   */
  public Collection<Screen> getAll() {
    return this.screens;
  }

  /**
   * Gets the currently active screen that is being rendered by the <code>RenderComponent</code>.
   *
   * @deprecated   There may now be more than one 'current' screen.
   *              currentScreen has been replaced with {@link #activeScreens}.
   *              Please use {@link #getActiveScreens()} instead.
   *              Currently redirects to {@link #getActiveScreens()}[0]
   *
   * @return The currently active screen.
   * 
   * @see GameWindow#getRenderComponent()
   * @see RenderComponent#render()
   */
  @Deprecated
  public Screen current() {
    return this.getActiveScreens()[0];
  }

  /**
   * Gets the screen change cooldown which is used to ensure that screens cannot be switched too quickly while the game is running.
   * 
   * @return The current change timeout for screens.
   *
   * @deprecated The cooldown is currently lingering junk code (this may or may not change)
   *
   * @see #DEFAULT_CHANGE_COOLDOWN
   * @see Game#hasStarted()
   */
  @Deprecated
  public int getChangeCooldown() {
    return this.changeCooldown;
  }

  /**
   * Sets the cooldown for changing screens.
   *
   * @deprecated The cooldown is currently lingering junk code (this may or may not change)
   * 
   * @param changeCooldown
   *          The cooldown for changing screens.
   */
  @Deprecated
  public void setChangeCooldown(int changeCooldown) {
    this.changeCooldown = changeCooldown;
  }

  public Screen[] getActiveScreens() {
    return (Screen[]) activeScreens.toArray();
  }
}
