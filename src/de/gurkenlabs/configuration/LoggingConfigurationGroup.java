package de.gurkenlabs.configuration;

import java.util.logging.Level;

import de.gurkenlabs.annotation.ConfigurationGroupInfo;

@ConfigurationGroupInfo(prefix = "LOG_")
public class LoggingConfigurationGroup extends ConfigurationGroup {
  private Level level;

  public Level getLevel() {
    return level;
  }

  public void setLevel(Level level) {
    this.level = level;
  }
}
