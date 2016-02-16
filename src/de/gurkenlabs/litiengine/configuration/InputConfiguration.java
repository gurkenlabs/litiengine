package de.gurkenlabs.litiengine.configuration;

import de.gurkenlabs.annotation.ConfigurationGroupInfo;
import de.gurkenlabs.configuration.ConfigurationGroup;

@ConfigurationGroupInfo(prefix = "INPUT_")
public class InputConfiguration extends ConfigurationGroup {
  private float mouseSensitivity;

  public InputConfiguration() {
    this.setMouseSensitivity(1.0F);
  }

  public float getMouseSensitivity() {
    return mouseSensitivity;
  }

  public void setMouseSensitivity(float mouseSensitivity) {
    this.mouseSensitivity = mouseSensitivity;
  }
}
