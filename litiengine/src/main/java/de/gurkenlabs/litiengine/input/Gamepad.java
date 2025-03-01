package de.gurkenlabs.litiengine.input;

import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.InputDeviceListener;
import de.gurkenlabs.input4j.components.Axis;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code Gamepad} class is designed as a wrapper implementation for any gamepad input that provides events and
 * information about player input via gamepad.
 */
public final class Gamepad extends GamepadEvents implements IUpdateable, InputDeviceListener {
  private final InputDevice inputDevice;

  private final Collection<InputComponent.ID> gamepadComponents;

  private final Collection<InputComponent.ID> pressedComponents;

  private float axisDeadzone = Game.config().input().getGamepadAxisDeadzone();
  private float triggerDeadzone = Game.config().input().getGamepadTriggerDeadzone();

  private String type;

  Gamepad(final InputDevice inputDevice) {
    this.pressedComponents = ConcurrentHashMap.newKeySet();
    this.inputDevice = inputDevice;
    this.inputDevice.onInputValueChanged(this);

    ArrayList<InputComponent.ID> components = new ArrayList<>();
    for (var comp : this.inputDevice.getComponents()) {
      components.add(comp.getId());
    }

    this.gamepadComponents = Collections.unmodifiableList(components);
    this.type = inputDevice.getProductName();
  }

  /**
   * Gets the unique id of this gamepad by which it is identified.
   *
   * @return The unique id of this gamepad.
   */
  public String getId() {
    return this.inputDevice.getID();
  }

  /**
   * Gets the name of this gamepad.
   *
   * @return The name of this gamepad.
   */
  public String getName() {
    return this.inputDevice.getName();
  }

  /**
   * Gets the poll data for the specified component on this gamepad.
   *
   * <p>
   * Returns the data from the last time the control has been polled.If this axis is a button, the value returned will be
   * either 0.0f or 1.0f.If this axis is normalized, the value returned will be between -1.0f and1.0f.
   *
   * @param componentId The component to retrieve the poll data for.
   * @return The data from the last time the specified component has been polled; 0 if this gamepad doesn't provide the
   * requested component.
   */
  public float getPollData(final InputComponent.ID componentId) {
    var component = this.inputDevice.getComponent(componentId);
    return component.map(InputComponent::getData).orElse(0F);

  }

  /**
   * Gets the deadzone for any axis components on this gamepad.
   *
   * <p>
   * A deadzone defines the poll value at which the events of this gamepad are not being triggered. This is useful to
   * smooth out controller input and not react to idle noise.
   *
   * @return The axis deadzone for this component.
   * @see #setAxisDeadzone(float)
   */
  public float getAxisDeadzone() {
    return this.axisDeadzone;
  }

  /**
   * Gets the deadzone for any trigger components on this gamepad.
   *
   * <p>
   * A deadzone defines the poll value at which the events of this gamepad are not being triggered. This is useful to
   * smooth out controller input and not react to idle noise.
   *
   * @return The trigger deadzone for this gamepad.
   * @see #setTriggerDeadzone(float)
   */
  public float getTriggerDeadzone() {
    return this.triggerDeadzone;
  }

  @Override
  public boolean isButtonPressed(int buttonId) {
    return this.isButtonPressed(InputComponent.ID.getButton(buttonId));
  }

  @Override
  public boolean isButtonPressed(InputComponent.ID gamepadComponent) {
    return this.pressedComponents.stream().anyMatch(x -> x.equals(gamepadComponent));
  }

  public Collection<InputComponent.ID> getComponents() {
    return this.gamepadComponents;
  }

  public boolean hasComponent(String gamepadComponent) {
    return this.inputDevice.getComponent(gamepadComponent).isPresent();
  }

  /**
   * Sets the deadzone for any axis components on this gamepad.
   *
   * @param axisDeadzone The axis deadzone for this gamepad.
   * @see #getAxisDeadzone()
   */
  public void setAxisDeadzone(float axisDeadzone) {
    this.axisDeadzone = axisDeadzone;
  }

  /**
   * Sets the deadzone for any trigger components on this gamepad.
   *
   * @param triggerDeadzone The trigger deadzone for this gamepad.
   * @see #getTriggerDeadzone()
   */
  public void setTriggerDeadzone(float triggerDeadzone) {
    this.triggerDeadzone = triggerDeadzone;
  }

  @Override
  public void update() {
    this.inputDevice.poll();
  }

  @Override
  public String toString() {
    return "Gamepad " + this.getId() + " - " + this.inputDevice.toString();
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public void onValueChanged(InputComponent.InputValueChangedEvent event) {
    final var component = event.component();
    if (component.isButton()) {
      if (event.newValue() == 1) {
        this.handleButtonPressed(component);
      } else {
        this.handleButtonReleased(component);
      }

      return;
    }

    if (Math.abs(event.newValue()) > this.getDeadZone(component.getId())) {
      this.handleValueChanged(event);
    }
  }

  private void handleButtonPressed(InputComponent component) {
    final var id = component.getId();
    final var event = new GamepadEvent(this, component);

    for (final var listener : this.pressedListeners) {
      listener.pressed(event);
    }

    if (this.componentPressedListeners.containsKey(id)) {
      for (var listener : this.componentPressedListeners.get(id)) {
        listener.pressed(event);
      }
    }

    if (!this.pressedComponents.contains(id)) {
      this.pressedComponents.add(id);
    }
  }

  private void handleButtonReleased(InputComponent component) {
    final var id = component.getId();
    if (!this.pressedComponents.contains(id)) {
      return;
    }

    this.pressedComponents.remove(id);

    final var event = new GamepadEvent(this, component);

    for (final var listener : this.releasedListeners) {
      listener.buttonReleased(event);
    }

    if (this.componentReleasedListeners.containsKey(id)) {
      for (final var listener : this.componentReleasedListeners.get(id)) {
        listener.buttonReleased(event);
      }
    }
  }

  private void handleValueChanged(InputComponent.InputValueChangedEvent event) {
    final var gamepadEvent = new GamepadEvent(this, event.component());

    for (final var listener : this.pollListeners) {
      listener.valueChanged(gamepadEvent);
    }

    final var listeners = this.componentPollListeners.get(event.component().getId());
    if (listeners != null) {
      for (final var listener : listeners) {
        listener.valueChanged(gamepadEvent);
      }
    }
  }

  private float getDeadZone(final InputComponent.ID id) {
    if (id.equals(Axis.AXIS_X)
      || id.equals(Axis.AXIS_Y)
      || id.equals(Axis.AXIS_RX)
      || id.equals(Axis.AXIS_RY)) {
      return this.getAxisDeadzone();
    }

    if (id.equals(Axis.AXIS_Z)) {
      return this.getTriggerDeadzone();
    }

    return 0;
  }
}
