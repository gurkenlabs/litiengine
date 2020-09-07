package de.gurkenlabs.litiengine.configuration;

/**
 * This class contains all default {@code ConfigurationGroups} that are provided by the LITIengine.
 * Additionally, it can be used to register and manage custom settings that are specific to your game.
 *
 * @see ConfigurationGroup
 * @see Configuration#add(ConfigurationGroup)
 * @see Configuration#getConfigurationGroup(Class)
 */
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

  /**
   * Gets the basic game client configuration like update-rate or localization.
   * 
   * @return The game client configuration.
   */
  public ClientConfiguration client() {
    return this.client;
  }

  /**
   * Gets the configuration group with all default debugging settings.
   * 
   * @return The debugging configuration.
   */
  public DebugConfiguration debug() {
    return this.debug;
  }

  /**
   * Gets the configuration group with all default graphics settings.
   * Elements in this group will allow you to adjust the game's rendering behavior.
   * 
   * @return The graphics configuration.
   */
  public GraphicConfiguration graphics() {
    return this.graphics;
  }

  /**
   * Gets the configuration group with all default input settings.
   * 
   * @return The input configuration.
   */
  public InputConfiguration input() {
    return this.input;
  }

  /**
   * Gets the configuration group with all default sound settings.
   * 
   * @return The sound configuration.
   */
  public SoundConfiguration sound() {
    return this.sound;
  }
}
