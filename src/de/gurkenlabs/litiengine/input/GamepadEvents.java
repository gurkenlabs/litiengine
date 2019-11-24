package de.gurkenlabs.litiengine.input;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface GamepadEvents {

  public void onPoll(String identifier, Consumer<Float> consumer);

  public void onPoll(BiConsumer<String, Float> consumer);

  public void onPressed(String identifier, Consumer<Float> consumer);

  public void onPressed(BiConsumer<String, Float> consumer);

  public void onReleased(String identifier, Consumer<Float> consumer);

  public void onReleased(BiConsumer<String, Float> consumer);

  /**
   * Removes all registered event consumers from the Gamepad instance.
   */
  public void clearEventConsumers();

  /**
   * Determines whether the specified Gamepad component is currently pressed.
   * This is useful for button type components.
   * 
   * @param gamepadComponent
   *          The component to check against.
   * @return True if the component is pressed, otherwise false.
   * 
   * @see Gamepad.Buttons
   * @see Gamepad.Xbox
   */
  public boolean isPressed(String gamepadComponent);
}
