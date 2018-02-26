package de.gurkenlabs.litiengine.configuration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.util.io.FileUtilities;

public class Configuration {
  private static final Logger log = Logger.getLogger(Configuration.class.getName());
  private static final String DEFAULT_CONFIGURATION_FILE_NAME = "config.properties";

  private final List<ConfigurationGroup> configurationGroups;
  private final String fileName;

  public Configuration(final ConfigurationGroup... configurationGroups) {
    this(DEFAULT_CONFIGURATION_FILE_NAME, configurationGroups);
  }

  public Configuration(final String fileName, final ConfigurationGroup... configurationGroups) {
    this.fileName = fileName;
    this.configurationGroups = new ArrayList<>();
    if (configurationGroups != null && configurationGroups.length > 0) {
      for (final ConfigurationGroup group : configurationGroups) {
        this.configurationGroups.add(group);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends ConfigurationGroup> T getConfigurationGroup(final Class<T> groupClass) {
    for (final ConfigurationGroup group : this.getConfigurationGroups()) {
      if (group.getClass().equals(groupClass)) {
        return (T) group;
      }
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends ConfigurationGroup> T getConfigurationGroup(final String prefix) {
    for (final ConfigurationGroup group : this.getConfigurationGroups()) {

      final ConfigurationGroupInfo info = group.getClass().getAnnotation(ConfigurationGroupInfo.class);
      if (info == null) {
        continue;
      }

      if (info.prefix().equals(prefix)) {
        return (T) group;
      }
    }

    return null;
  }

  public List<ConfigurationGroup> getConfigurationGroups() {
    return this.configurationGroups;
  }

  public String getFileName() {
    return this.fileName;
  }

  public void load() {
    this.loadFromFile();
  }

  public void save() {
    final File settingsFile = new File(this.getFileName());
    try {
      final OutputStream out = new FileOutputStream(settingsFile, false);
      for (final ConfigurationGroup group : this.getConfigurationGroups()) {
        if (!Game.isDebug() && group.isDebug()) {
          continue;
        }

        storeConfigurationGroup(out, group);
      }
      out.close();
      log.log(Level.INFO, "Configuration " + this.getFileName() + " saved");
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

  /**
   * Tries to load configuration from file in the application folder. If none
   * exists, it tires to load the file from any resource folder. If none exists,
   * it creates a new configuration file in the application folder.
   */
  private void loadFromFile() {
    final File settingsFile = new File(this.getFileName());
    try (InputStream settingsStream = FileUtilities.getGameResource(this.getFileName())) {
      if (!settingsFile.exists() && settingsStream == null || !settingsFile.isFile()) {
        final OutputStream out = new FileOutputStream(settingsFile);
        this.createDefaultSettingsFile(out);
        out.close();

        log.log(Level.INFO, "Default configuration " + this.getFileName() + " created");
        return;
      }
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    if (settingsFile.exists()) {
      try (InputStream settingsStream = new FileInputStream(settingsFile)) {

        final Properties properties = new Properties();
        BufferedInputStream stream;

        stream = new BufferedInputStream(settingsStream);
        properties.load(stream);
        stream.close();

        this.initializeSettingsByProperties(properties);
        log.log(Level.INFO, "Configuration " + this.getFileName() + " loaded");
      } catch (final IOException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }
}
