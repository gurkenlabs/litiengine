package de.gurkenlabs.litiengine.configuration;

import de.gurkenlabs.configuration.Configuration;
import de.gurkenlabs.configuration.ConfigurationGroup;

public final class GameConfiguration extends Configuration {
  private final ClientConfiguration client;
  private final DebugConfiguration debug;
  private final GraphicConfiguration graphics;
  private final InputConfiguration input;
  private final SoundConfiguration sound;

  public GameConfiguration(final ConfigurationGroup... groups) {
    super(groups);
    this.client = new ClientConfiguration();
    this.sound = new SoundConfiguration();
    this.graphics = new GraphicConfiguration();
    this.input = new InputConfiguration();
    this.debug = new DebugConfiguration();
    this.getConfigurationGroups().add(this.client);
    this.getConfigurationGroups().add(this.sound);
    this.getConfigurationGroups().add(this.graphics);
    this.getConfigurationGroups().add(this.input);
    this.getConfigurationGroups().add(this.debug);
  }

  public ClientConfiguration client() {
    return this.client;
  }

  public DebugConfiguration debug() {
    return this.debug;
  }

  public GraphicConfiguration graphics() {
    return this.graphics;
  }

  public InputConfiguration input() {
    return this.input;
  }

  public SoundConfiguration sound() {
    return this.sound;
  }
}
