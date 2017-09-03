package de.gurkenlabs.configuration;

import java.util.logging.Level;

@ConfigurationGroupInfo(prefix = "LOG_")
public class LoggingConfigurationGroup extends ConfigurationGroup {
  private Level level;

  public Level getLevel() {
    return this.level;
  }

  public void setLevel(final Level level) {
    this.level = level;
  }
}
