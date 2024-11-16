package de.gurkenlabs.litiengine.configuration;

@ConfigurationGroupInfo(prefix = "input_")
public class InputConfiguration extends ConfigurationGroup {
  private float mouseSensitivity;
  private boolean gamepadSupport;
  private float gamepadAxisDeadzone;
  private float gamepadTriggerDeadzone;
  private float gamepadStickDeadzone;

  /**
   * Constructs a new InputConfiguration with default settings.
   */
  InputConfiguration() {
    this.setMouseSensitivity(1.0F);
    this.setGamepadSupport(false);
    this.setGamepadAxisDeadzone(0.3f);
    this.setGamepadTriggerDeadzone(0.1f);
    this.setGamepadStickDeadzone(0.15f);
  }

  /**
   * Gets the current mouse sensitivity.
   *
   * @return the mouse sensitivity.
   */
  public float getMouseSensitivity() {
    return mouseSensitivity;
  }

  /**
   * Gets the current gamepad axis deadzone.
   *
   * @return the gamepad axis deadzone.
   */
  public float getGamepadAxisDeadzone() {
    return gamepadAxisDeadzone;
  }

  /**
   * Gets the current gamepad trigger deadzone.
   *
   * @return the gamepad trigger deadzone.
   */
  public float getGamepadTriggerDeadzone() {
    return gamepadTriggerDeadzone;
  }

  /**
   * Gets the current gamepad stick deadzone.
   *
   * @return the gamepad stick deadzone.
   */
  public float getGamepadStickDeadzone() {
    return gamepadStickDeadzone;
  }

  /**
   * Checks if gamepad support is enabled.
   *
   * @return true if gamepad support is enabled, false otherwise.
   */
  public boolean isGamepadSupport() {
    return gamepadSupport;
  }

  /**
   * Sets the mouse sensitivity.
   *
   * @param mouseSensitivity the new mouse sensitivity.
   */
  public void setMouseSensitivity(final float mouseSensitivity) {
    this.set("mouseSensitivity", mouseSensitivity);
  }

  /**
   * Sets the gamepad support.
   *
   * @param gamepadSupport the new gamepad support status.
   */
  public void setGamepadSupport(boolean gamepadSupport) {
    this.set("gamepadSupport", gamepadSupport);
  }

  /**
   * Sets the gamepad axis deadzone.
   *
   * @param gamepadAxisDeadzone the new gamepad axis deadzone.
   */
  public void setGamepadAxisDeadzone(float gamepadAxisDeadzone) {
    this.set("gamepadAxisDeadzone", gamepadAxisDeadzone);
  }

  /**
   * Sets the gamepad trigger deadzone.
   *
   * @param gamepadTriggerDeadzone the new gamepad trigger deadzone.
   */
  public void setGamepadTriggerDeadzone(float gamepadTriggerDeadzone) {
    this.set("gamepadTriggerDeadzone", gamepadTriggerDeadzone);
  }

  /**
   * Sets the gamepad stick deadzone.
   *
   * @param gamepadStickDeadzone the new gamepad stick deadzone.
   */
  public void setGamepadStickDeadzone(float gamepadStickDeadzone) {
    this.set("gamepadStickDeadzone", gamepadStickDeadzone);
  }
}
