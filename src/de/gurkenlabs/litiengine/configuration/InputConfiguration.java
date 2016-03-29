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
    return this.mouseSensitivity;
  }

  public void setMouseSensitivity(final float mouseSensitivity) {
    this.mouseSensitivity = mouseSensitivity;
  }
}
