package de.gurkenlabs.litiengine.configuration;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration class that manages multiple configuration groups and handles loading and saving settings.
 */
public class Configuration {
  private static final Logger log = Logger.getLogger(Configuration.class.getName());
  private static final String DEFAULT_CONFIGURATION_FILE_NAME = "config.properties";

  private final List<ConfigurationGroup> configurationGroups;
  private final String fileName;

  /**
   * Initializes a new instance of the {@code Configuration} class.
   *
   * @param configurationGroups The configuration groups managed by this instance.
   */
  public Configuration(final ConfigurationGroup... configurationGroups) {
    this(DEFAULT_CONFIGURATION_FILE_NAME, configurationGroups);
  }

  /**
   * Initializes a new instance of the {@code Configuration} class.
   *
   * @param fileName            The name of the file from which to load the settings.
   * @param configurationGroups The configuration groups managed by this instance.
   */
  public Configuration(final String fileName, final ConfigurationGroup... configurationGroups) {
    this.fileName = fileName;
    this.configurationGroups = new ArrayList<>();
    if (configurationGroups != null && configurationGroups.length > 0) {
      Collections.addAll(this.configurationGroups, configurationGroups);
    }
  }

  /**
   * Gets the strongly typed configuration group if it was previously added to the configuration.
   *
   * @param <T>        The type of the config group.
   * @param groupClass The class that provides the generic type for this method.
   * @return The configuration group of the specified type or null if none can be found.
   */
  public <T extends ConfigurationGroup> T getConfigurationGroup(final Class<T> groupClass) {
    for (final ConfigurationGroup group : this.getConfigurationGroups()) {
      if (group.getClass().equals(groupClass)) {
        return groupClass.cast(group);
      }
    }

    return null;
  }

  /**
   * Gets the configuration group with the specified prefix.
   *
   * @param prefix The prefix of the configuration group to retrieve.
   * @return The configuration group with the specified prefix, or null if none can be found.
   */
  public ConfigurationGroup getConfigurationGroup(final String prefix) {
    for (final ConfigurationGroup group : this.getConfigurationGroups()) {

      final ConfigurationGroupInfo info = group.getClass().getAnnotation(ConfigurationGroupInfo.class);
      if (info == null) {
        continue;
      }

      if (info.prefix().equals(prefix)) {
        return group;
      }
    }

    return null;
  }

  /**
   * Gets all {@code ConfigurationGroups} from the configuration.
   *
   * @return All config groups.
   */
  public List<ConfigurationGroup> getConfigurationGroups() {
    return this.configurationGroups;
  }

  /**
   * Adds the specified configuration group to the configuration.
   *
   * @param group The group to add.
   */
  public void add(ConfigurationGroup group) {
    this.getConfigurationGroups().add(group);
  }

  /**
   * Gets the name of the file to which this configuration is saved.
   *
   * @return The name of the configuration file.
   * @see #save()
   */
  public String getFileName() {
    return this.fileName;
  }

  /**
   * Tries to load the configuration from file in the application folder. If none exists, it tries to load the file from any resource folder. If none
   * exists, it creates a new configuration file in the application folder.
   */
  public void load() {
    final Path settingsFile = Path.of(this.getFileName());
    try (InputStream settingsStream = Resources.get(this.getFileName())) {
      if (!Files.exists(settingsFile) && settingsStream == null || !Files.isRegularFile(settingsFile)) {
        try (OutputStream out = Files.newOutputStream(settingsFile)) {
          this.createDefaultSettingsFile(out);
        }

        log.log(Level.INFO, "Default configuration {0} created", this.getFileName());
        return;
      }
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    if (Files.exists(settingsFile)) {
      try (InputStream settingsStream = Files.newInputStream(settingsFile)) {

        final Properties properties = new Properties();
        BufferedInputStream stream;

        stream = new BufferedInputStream(settingsStream);
        properties.load(stream);
        stream.close();

        this.initializeSettingsByProperties(properties);
        log.log(Level.INFO, "Configuration {0} created", this.getFileName());
      } catch (final IOException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  /**
   * Saves this configuration to a file with the specified name of this instance (config.properties is the engines default config file).
   *
   * @see #getFileName()
   * @see Configuration#DEFAULT_CONFIGURATION_FILE_NAME
   */
  public void save() {
    final Path settingsFile = Path.of(this.getFileName());
    try (OutputStream out = Files.newOutputStream(settingsFile, StandardOpenOption.CREATE_NEW)) {
      for (final ConfigurationGroup group : this.getConfigurationGroups()) {
        if (!Game.isDebug() && group.isDebug()) {
          continue;
        }

        storeConfigurationGroup(out, group);
      }
      log.log(Level.INFO, "Configuration {0} saved", this.getFileName());
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private static void storeConfigurationGroup(final OutputStream out, final ConfigurationGroup group) {
    try {
      final Properties groupProperties = new CleanProperties();
      group.storeProperties(groupProperties);
      groupProperties.store(out, group.getPrefix() + "SETTINGS");
      out.flush();
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private void createDefaultSettingsFile(final OutputStream out) {
    for (final ConfigurationGroup group : this.getConfigurationGroups()) {
      storeConfigurationGroup(out, group);
    }
  }

  private void initializeSettingsByProperties(final Properties properties) {
    for (final String key : properties.stringPropertyNames()) {
      for (final ConfigurationGroup group : this.getConfigurationGroups()) {
        if (key.startsWith(group.getPrefix())) {
          group.initializeByProperty(key, properties.getProperty(key));
        }
      }
    }
  }
}
