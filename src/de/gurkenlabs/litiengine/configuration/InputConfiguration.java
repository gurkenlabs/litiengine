package de.gurkenlabs.litiengine.configuration;

@ConfigurationGroupInfo(prefix = "input_")
public class InputConfiguration extends ConfigurationGroup {
  private float mouseSensitivity;
  private boolean gamepadSupport;

  public InputConfiguration() {
    this.setMouseSensitivity(1.0F);
    this.setGamepadSupport(true);
  }

  public float getMouseSensitivity() {
    return this.mouseSensitivity;
  }

  public void setMouseSensitivity(final float mouseSensitivity) {
    this.mouseSensitivity = mouseSensitivity;
  }

  public boolean isGamepadSupport() {
    return gamepadSupport;
  }

  public void setGamepadSupport(boolean gamepadSupport) {
    this.gamepadSupport = gamepadSupport;
  }
}
