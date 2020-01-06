package de.gurkenlabs.litiengine.input;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.Event;

public final class Gamepad implements GamepadEvents, IUpdateable {
  private static final Map<String, Identifier> components = new HashMap<>();

  private final Controller controller;

  private final int index;

  private final Map<String, Collection<GamepadPollListener>> componentPollListeners;
  private final Map<String, Collection<GamepadPressedListener>> componentPressedListeners;
  private final Map<String, Collection<GamepadReleasedListener>> componentReleasedListeners;

  private final Collection<GamepadPollListener> pollListeners;
  private final Collection<GamepadPressedListener> pressedListeners;
  private final Collection<GamepadReleasedListener> releasedListeners;

  private final Collection<String> pressedComponents;

  private float axisDeadzone = Game.config().input().getGamepadAxisDeadzone();
  private float triggerDeadzone = Game.config().input().getGamepadTriggerDeadzone();

  Gamepad(final int index, final Controller controller) {
    this.componentPollListeners = new ConcurrentHashMap<>();
    this.componentPressedListeners = new ConcurrentHashMap<>();
    this.componentReleasedListeners = new ConcurrentHashMap<>();
    this.pollListeners = ConcurrentHashMap.newKeySet();
    this.pressedListeners = ConcurrentHashMap.newKeySet();
    this.releasedListeners = ConcurrentHashMap.newKeySet();

    this.pressedComponents = ConcurrentHashMap.newKeySet();

    this.index = index;
    this.controller = controller;
    Game.inputLoop().attach(this);
  }

  public int getIndex() {
    return this.index;
  }

  public String getName() {
    return this.controller.getName();
  }

  public float getPollData(final String component) {
    if (components.containsKey(component)) {
      return getPollData(components.get(component));
    }

    return 0;
  }

  public float getAxisDeadzone() {
    return this.axisDeadzone;
  }

  public float getTriggerDeadzone() {
    return this.triggerDeadzone;
  }

  @Override
  public void onPoll(final String identifier, final GamepadPollListener listener) {
    GamepadManager.addComponentListener(this.componentPollListeners, identifier, listener);
  }

  @Override
  public void removePollListener(String identifier, GamepadPollListener listener) {
    GamepadManager.removeComponentListener(this.componentPollListeners, identifier, listener);
  }

  @Override
  public void onPressed(final String identifier, final GamepadPressedListener listener) {
    GamepadManager.addComponentListener(this.componentPressedListeners, identifier, listener);
  }

  @Override
  public void removePressedListener(String identifier, GamepadPressedListener listener) {
    GamepadManager.removeComponentListener(this.componentPressedListeners, identifier, listener);
  }

  @Override
  public void onReleased(String identifier, GamepadReleasedListener listener) {
    GamepadManager.addComponentListener(this.componentReleasedListeners, identifier, listener);
  }

  @Override
  public void removeReleasedListener(String identifier, GamepadReleasedListener listener) {
    GamepadManager.removeComponentListener(this.componentReleasedListeners, identifier, listener);
  }

  @Override
  public void onPoll(GamepadPollListener listener) {
    this.pollListeners.add(listener);
  }

  @Override
  public void removePollListener(GamepadPollListener listener) {
    this.pollListeners.remove(listener);
  }

  @Override
  public void onPressed(GamepadPressedListener listener) {
    this.pressedListeners.add(listener);
  }

  @Override
  public void removePressedListener(GamepadPressedListener listener) {
    this.pressedListeners.remove(listener);
  }

  @Override
  public void onReleased(GamepadReleasedListener listener) {
    this.releasedListeners.add(listener);
  }

  @Override
  public void removeReleasedListener(GamepadReleasedListener listener) {
    this.releasedListeners.remove(listener);
  }

  @Override
  public void clearEventListeners() {
    this.releasedListeners.clear();
    this.componentReleasedListeners.clear();

    this.pressedListeners.clear();
    this.componentPressedListeners.clear();

    this.pollListeners.clear();
    this.componentPollListeners.clear();
  }

  @Override
  public boolean isPressed(String gamepadComponent) {
    return this.pressedComponents.stream().anyMatch(x -> x.equals(gamepadComponent));
  }

  public void setAxisDeadzone(float gamepadAxisDeadzone) {
    this.axisDeadzone = gamepadAxisDeadzone;
  }

  public void setTriggerDeadzone(float gamepadTriggerDeadzone) {
    this.triggerDeadzone = gamepadTriggerDeadzone;
  }

  @Override
  public void update() {
    final boolean couldPoll = this.controller.poll();
    if (!couldPoll) {
      this.dispose();
    }

    final Event event = new Event();
    while (this.controller.getEventQueue().getNextEvent(event)) {
      this.handlePollEvents(event);
    }

    for (Component comp : this.controller.getComponents()) {

      if (Math.abs(comp.getPollData()) > this.getDeadZone(comp.getIdentifier())) {
        this.handlePressed(comp);
      } else {
        this.handleRelease(comp);
      }
    }
  }

  @Override
  public String toString() {
    return "Gamepad " + this.getIndex() + " - " + this.controller.toString();
  }

  protected float getPollData(final Identifier identifier) {
    final Component comp = this.controller.getComponent(identifier);
    if (comp == null) {
      return 0;
    }

    return comp.getPollData();
  }

  private void handlePressed(Component comp) {
    final String name = comp.getIdentifier().getName();

    final GamepadEvent event = new GamepadEvent(this, comp);

    for (final GamepadPressedListener listener : this.pressedListeners) {
      listener.pressed(event);
    }

    if (this.componentPressedListeners.containsKey(name)) {
      for (GamepadPressedListener listener : this.componentPressedListeners.get(name)) {
        listener.pressed(event);
      }
    }

    if (!this.pressedComponents.contains(name)) {
      this.pressedComponents.add(name);
    }
  }

  private float getDeadZone(final Identifier ident) {
    if (ident.getName().equals(Axis.X)
        || ident.getName().equals(Axis.Y)) {
      return this.getAxisDeadzone();
    }

    if (ident.getName().equals(Axis.Z)) {
      return this.getTriggerDeadzone();
    }

    return 0;
  }

  private static final String addComponent(final Identifier identifier) {
    components.put(identifier.getName(), identifier);
    return identifier.getName();
  }

  protected static final Identifier get(final String name) {
    return components.get(name);
  }

  private void dispose() {
    Game.inputLoop().detach(this);
    this.componentPollListeners.clear();
    this.componentPressedListeners.clear();
    Input.gamepads().remove(this);
  }

  private void handlePollEvents(Event event) {
    final GamepadEvent gamepadEvent = new GamepadEvent(this, event.getComponent());

    for (final GamepadPollListener listener : this.pollListeners) {
      listener.polled(gamepadEvent);
    }

    final Collection<GamepadPollListener> listeners = this.componentPollListeners.get(event.getComponent().getIdentifier().getName());
    if (listeners != null) {
      for (final GamepadPollListener listener : listeners) {
        listener.polled(gamepadEvent);
      }
    }
  }

  private void handleRelease(Component comp) {
    final String name = comp.getIdentifier().getName();
    if (!this.pressedComponents.contains(name)) {
      return;
    }

    this.pressedComponents.remove(name);

    final GamepadEvent event = new GamepadEvent(this, comp);

    for (final GamepadReleasedListener listener : this.releasedListeners) {
      listener.released(event);
    }

    final Collection<GamepadReleasedListener> listeners = this.componentReleasedListeners.get(comp.getIdentifier().getName());
    if (listeners != null) {
      for (final GamepadReleasedListener listener : listeners) {
        listener.released(event);
      }
    }
  }

  public static class Axis {
    public static final String POV = addComponent(Identifier.Axis.POV);
    public static final String RX = addComponent(Identifier.Axis.RX);
    public static final String RX_ACCELERATION = addComponent(Identifier.Axis.RX_ACCELERATION);
    public static final String RX_FORCE = addComponent(Identifier.Axis.RX_FORCE);
    public static final String RX_VELOCITY = addComponent(Identifier.Axis.RX_VELOCITY);
    public static final String RY = addComponent(Identifier.Axis.RY);
    public static final String RY_ACCELERATION = addComponent(Identifier.Axis.RY_ACCELERATION);
    public static final String RY_FORCE = addComponent(Identifier.Axis.RY_FORCE);
    public static final String RY_VELOCITY = addComponent(Identifier.Axis.RY_VELOCITY);
    public static final String RZ = addComponent(Identifier.Axis.RZ);
    public static final String RZ_ACCELERATION = addComponent(Identifier.Axis.RZ_ACCELERATION);
    public static final String RZ_FORCE = addComponent(Identifier.Axis.RZ_FORCE);
    public static final String RZ_VELOCITY = addComponent(Identifier.Axis.RZ_VELOCITY);
    public static final String SLIDER = addComponent(Identifier.Axis.SLIDER);
    public static final String SLIDER_ACCELERATION = addComponent(Identifier.Axis.SLIDER_ACCELERATION);
    public static final String SLIDER_FORCE = addComponent(Identifier.Axis.SLIDER_FORCE);
    public static final String SLIDER_VELOCITY = addComponent(Identifier.Axis.SLIDER_VELOCITY);
    public static final String X = addComponent(Identifier.Axis.X);
    public static final String X_ACCELERATION = addComponent(Identifier.Axis.X_ACCELERATION);
    public static final String X_FORCE = addComponent(Identifier.Axis.X_FORCE);
    public static final String X_VELOCITY = addComponent(Identifier.Axis.X_VELOCITY);
    public static final String Y = addComponent(Identifier.Axis.Y);
    public static final String Y_ACCELERATION = addComponent(Identifier.Axis.Y_ACCELERATION);
    public static final String Y_FORCE = addComponent(Identifier.Axis.Y_FORCE);
    public static final String Y_VELOCITY = addComponent(Identifier.Axis.Y_VELOCITY);
    public static final String Z = addComponent(Identifier.Axis.Z);
    public static final String Z_ACCELERATION = addComponent(Identifier.Axis.Z_ACCELERATION);
    public static final String Z_FORCE = addComponent(Identifier.Axis.Z_FORCE);
    public static final String Z_VELOCITY = addComponent(Identifier.Axis.Z_VELOCITY);

    private Axis() {
    }
  }

  public static class Buttons {
    public static final String BUTTON_0 = addComponent(Identifier.Button._0);
    public static final String BUTTON_1 = addComponent(Identifier.Button._1);
    public static final String BUTTON_10 = addComponent(Identifier.Button._10);
    public static final String BUTTON_11 = addComponent(Identifier.Button._11);
    public static final String BUTTON_12 = addComponent(Identifier.Button._12);
    public static final String BUTTON_13 = addComponent(Identifier.Button._13);
    public static final String BUTTON_14 = addComponent(Identifier.Button._14);
    public static final String BUTTON_15 = addComponent(Identifier.Button._15);
    public static final String BUTTON_16 = addComponent(Identifier.Button._16);
    public static final String BUTTON_17 = addComponent(Identifier.Button._17);
    public static final String BUTTON_18 = addComponent(Identifier.Button._18);
    public static final String BUTTON_19 = addComponent(Identifier.Button._19);
    public static final String BUTTON_2 = addComponent(Identifier.Button._2);
    public static final String BUTTON_20 = addComponent(Identifier.Button._20);
    public static final String BUTTON_21 = addComponent(Identifier.Button._21);
    public static final String BUTTON_22 = addComponent(Identifier.Button._22);
    public static final String BUTTON_23 = addComponent(Identifier.Button._23);
    public static final String BUTTON_24 = addComponent(Identifier.Button._24);
    public static final String BUTTON_25 = addComponent(Identifier.Button._25);
    public static final String BUTTON_26 = addComponent(Identifier.Button._26);
    public static final String BUTTON_27 = addComponent(Identifier.Button._27);
    public static final String BUTTON_28 = addComponent(Identifier.Button._28);
    public static final String BUTTON_29 = addComponent(Identifier.Button._29);
    public static final String BUTTON_3 = addComponent(Identifier.Button._3);
    public static final String BUTTON_30 = addComponent(Identifier.Button._30);
    public static final String BUTTON_31 = addComponent(Identifier.Button._31);
    public static final String BUTTON_4 = addComponent(Identifier.Button._4);
    public static final String BUTTON_5 = addComponent(Identifier.Button._5);
    public static final String BUTTON_6 = addComponent(Identifier.Button._6);
    public static final String BUTTON_7 = addComponent(Identifier.Button._7);
    public static final String BUTTON_8 = addComponent(Identifier.Button._8);
    public static final String BUTTON_9 = addComponent(Identifier.Button._9);

    // gamepad buttons (not for xbox :( )
    public static final String A = addComponent(Identifier.Button.A);
    public static final String B = addComponent(Identifier.Button.B);
    public static final String BASE = addComponent(Identifier.Button.BASE);
    public static final String BASE2 = addComponent(Identifier.Button.BASE2);
    public static final String BASE3 = addComponent(Identifier.Button.BASE3);
    public static final String BASE4 = addComponent(Identifier.Button.BASE4);
    public static final String BASE5 = addComponent(Identifier.Button.BASE5);
    public static final String BASE6 = addComponent(Identifier.Button.BASE6);
    public static final String C = addComponent(Identifier.Button.C);
    public static final String LEFT_THUMB = addComponent(Identifier.Button.LEFT_THUMB);
    public static final String LEFT_THUMB2 = addComponent(Identifier.Button.LEFT_THUMB2);
    public static final String LEFT_THUMB3 = addComponent(Identifier.Button.LEFT_THUMB3);
    public static final String MODE = addComponent(Identifier.Button.MODE);
    public static final String PINKIE = addComponent(Identifier.Button.PINKIE);
    public static final String RIGHT_THUMB = addComponent(Identifier.Button.RIGHT_THUMB);

    public static final String RIGHT_THUMB2 = addComponent(Identifier.Button.RIGHT_THUMB2);
    public static final String RIGHT_THUMB3 = addComponent(Identifier.Button.RIGHT_THUMB3);
    public static final String SELECT = addComponent(Identifier.Button.SELECT);
    public static final String START = addComponent(Identifier.Button.START);
    public static final String THUMB = addComponent(Identifier.Button.THUMB);
    public static final String THUMB2 = addComponent(Identifier.Button.THUMB2);
    public static final String TOP = addComponent(Identifier.Button.TOP);
    public static final String TOP2 = addComponent(Identifier.Button.TOP2);
    // joystick buttons
    public static final String TRIGGER = addComponent(Identifier.Button.TRIGGER);
    public static final String X = addComponent(Identifier.Button.X);
    public static final String Y = addComponent(Identifier.Button.Y);
    public static final String Z = addComponent(Identifier.Button.Z);

    // incomplete list... right now we don't support stylus / mouse or
    // extra buttons
    private Buttons() {
    }
  }

  public static class DPad {
    /**
     * Standard value for center HAT position
     */
    public static final float OFF = 0.0f;
    /**
     * Synonmous with OFF
     */
    public static final float CENTER = OFF;
    /**
     * Standard value for down HAT position
     */
    public static final float DOWN = 0.75f;
    /**
     * Standard value for down-left HAT position
     */
    public static final float DOWN_LEFT = 0.875f;
    /**
     * Standard value for down-right HAT position
     */
    public static final float DOWN_RIGHT = 0.625f;
    /**
     * Standard value for left HAT position
     */
    public static final float LEFT = 1.0f;

    /**
     * Standard value for right HAT position
     */
    public static final float RIGHT = 0.50f;
    /**
     * Standard value for up HAT position
     */
    public static final float UP = 0.25f;
    /**
     * Standard value for up-left HAT position
     */
    public static final float UP_LEFT = 0.125f;
    /**
     * Standard value for up-right HAT position
     */
    public static final float UP_RIGHT = 0.375f;

    private DPad() {
    }
  }

  public static class Xbox {
    public static final String A = Buttons.BUTTON_0;
    public static final String B = Buttons.BUTTON_1;
    public static final String DPAD = Axis.POV;
    public static final String LB = Buttons.BUTTON_4;
    public static final String LEFT_STICK_PRESS = Buttons.BUTTON_8;
    public static final String LEFT_STICK_X = Axis.X;
    public static final String LEFT_STICK_Y = Axis.Y;

    // range 0 - 1
    public static final String LT = Axis.Z;
    public static final String RB = Buttons.BUTTON_5;
    public static final String RIGHT_STICK_PRESS = Buttons.BUTTON_9;
    public static final String RIGHT_STICK_X = Axis.RX;
    public static final String RIGHT_STICK_Y = Axis.RY;
    // range -1 - 0
    public static final String RT = Axis.Z;
    public static final String SELECT = Buttons.BUTTON_6;
    public static final String START = Buttons.BUTTON_7;
    public static final String X = Buttons.BUTTON_2;

    public static final String Y = Buttons.BUTTON_3;

    private Xbox() {
    }
  }
}
