package de.gurkenlabs.litiengine.configuration;

@ConfigurationGroupInfo(prefix = "input_")
public class InputConfiguration extends ConfigurationGroup {
  private float mouseSensitivity;
  private boolean gamepadSupport;
  private float gamepadAxisDeadzone;
  private float gamepadTriggerDeadzone;
  private float gamepadStickDeadzone;

  InputConfiguration() {
    this.setMouseSensitivity(1.0F);
    this.setGamepadSupport(false);
    this.setGamepadAxisDeadzone(0.3f);
    this.setGamepadTriggerDeadzone(0.1f);
    this.setGamepadStickDeadzone(0.15f);
  }

  public float getMouseSensitivity() {
    return this.mouseSensitivity;
  }

  public float getGamepadAxisDeadzone() {
    return this.gamepadAxisDeadzone;
  }

  public float getGamepadTriggerDeadzone() {
    return this.gamepadTriggerDeadzone;
  }

  public float getGamepadStickDeadzone() {
    return this.gamepadStickDeadzone;
  }

  public boolean isGamepadSupport() {
    return gamepadSupport;
  }

  public void setMouseSensitivity(final float mouseSensitivity) {
    this.set("mouseSensitivity", mouseSensitivity);
  }

  public void setGamepadSupport(boolean gamepadSupport) {
    this.set("gamepadSupport", gamepadSupport);
  }

  public void setGamepadAxisDeadzone(float gamepadAxisDeadzone) {
    this.set("gamepadAxisDeadzone", gamepadAxisDeadzone);
  }

  public void setGamepadTriggerDeadzone(float gamepadTriggerDeadzone) {
    this.set("gamepadTriggerDeadzone", gamepadTriggerDeadzone);
  }

  public void setGamepadStickDeadzone(float gamepadStickDeadzone) {
    this.set("gamepadStickDeadzone", gamepadStickDeadzone);
  }
}
