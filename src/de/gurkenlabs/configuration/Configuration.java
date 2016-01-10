package de.gurkenlabs.configuration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.gurkenlabs.annotation.ConfigurationGroupInfo;

public abstract class Configuration {
  /** The Constant CONFIGURATION_FILE_NAME. */
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

  protected static void storeConfigurationGroup(final OutputStream out, final ConfigurationGroup group) {
    try {
      final Properties groupProperties = new Properties();
      group.storeProperties(groupProperties);
      groupProperties.store(out, group.getPrefix() + "SETTINGS");
      out.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends ConfigurationGroup> T getConfigurationGroup(final Class<T> groupClass) {
    for (final ConfigurationGroup group : this.getConfigurationGroups()) {
      if (group.getPrefix().startsWith(groupClass.getAnnotation(ConfigurationGroupInfo.class).prefix())) {
        return (T) group;
      }
    }

    return null;
  }

  public String getFileName() {
    return this.fileName;
  }

  public void load() {
    this.loadFromFile();
  }

  private void loadFromFile() {
    final File settingsFile = new File(this.getFileName());
    if (!settingsFile.exists() || !settingsFile.isFile()) {
      try {
        final OutputStream out = new FileOutputStream(settingsFile);
        this.createDefaultSettingsFile(out);
        out.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }

      System.out.printf("Default configuration %s created \n", this.getFileName());
      return;
    }

    final Properties properties = new Properties();
    BufferedInputStream stream;
    try {
      stream = new BufferedInputStream(new FileInputStream(settingsFile));
      properties.load(stream);
      stream.close();
    } catch (final FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    this.initializeSettingsByProperties(properties);
    System.out.printf("Configuration %s loaded \n", this.getFileName());
  }

  protected void createDefaultSettingsFile(final OutputStream out) {
    for (final ConfigurationGroup group : this.getConfigurationGroups()) {
      storeConfigurationGroup(out, group);
    }
  }

  protected List<ConfigurationGroup> getConfigurationGroups() {
    return this.configurationGroups;
  }

  protected void initializeSettingsByProperties(final Properties properties) {
    for (final String key : properties.stringPropertyNames()) {
      for (final ConfigurationGroup group : this.getConfigurationGroups()) {
        if (key.startsWith(group.getPrefix())) {
          group.initializeByProperty(key, properties.getProperty(key));
        }
      }
    }
  }
}
