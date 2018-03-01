package de.gurkenlabs.litiengine.configuration;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.util.ReflectionUtilities;

/**
 * This class contains some basic functionality for all setting groups. It gets
 * the SettingsGroupInfo annotation and reads out the prefix that is used when
 * reading/ writing the settings into a property file.
 */
@ConfigurationGroupInfo
public abstract class ConfigurationGroup {
  private static final Logger log = Logger.getLogger(ConfigurationGroup.class.getName());

  private final String prefix;
  private boolean debug;

  /**
   * Instantiates a new configuration group.
   */
  public ConfigurationGroup() {
    final ConfigurationGroupInfo info = this.getClass().getAnnotation(ConfigurationGroupInfo.class);
    this.prefix = info.prefix();
    this.debug = info.debug();
  }

  /**
   * Gets the prefix.
   *
   * @return the prefix
   */
  public String getPrefix() {
    return this.prefix != null ? this.prefix : "";
  }

  /**
   * Initialize by property.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   */
  protected void initializeByProperty(final String key, final String value) {
    final String propertyName = key.substring(this.getPrefix().length());
    ReflectionUtilities.setFieldValue(this.getClass(), this, propertyName, value);
  }

  /**
   * Store properties. By default, it is supported to store the following types:
   * boolean, int, double, float, String and enum values. If you need to store
   * any other object, you should overwrite this method as well as the
   * initializeProperty method and implement a custom approach.
   *
   *
   * @param properties
   *          the properties
   */
  protected void storeProperties(final Properties properties) {
    try {
      for (final Field field : this.getClass().getDeclaredFields()) {
        if (!field.isAccessible()) {
          field.setAccessible(true);
        }

        if (field.getType().equals(boolean.class)) {
          properties.setProperty(this.getPrefix() + field.getName(), Boolean.toString(field.getBoolean(this)));
        } else if (field.getType().equals(int.class)) {
          properties.setProperty(this.getPrefix() + field.getName(), Integer.toString(field.getInt(this)));
        } else if (field.getType().equals(float.class)) {
          properties.setProperty(this.getPrefix() + field.getName(), Float.toString(field.getFloat(this)));
        } else if (field.getType().equals(double.class)) {
          properties.setProperty(this.getPrefix() + field.getName(), Double.toString(field.getDouble(this)));
        } else if (field.getType().equals(byte.class)) {
          properties.setProperty(this.getPrefix() + field.getName(), Byte.toString(field.getByte(this)));
        } else if (field.getType().equals(short.class)) {
          properties.setProperty(this.getPrefix() + field.getName(), Short.toString(field.getShort(this)));
        } else if (field.getType().equals(long.class)) {
          properties.setProperty(this.getPrefix() + field.getName(), Long.toString(field.getLong(this)));
        } else if (field.getType().equals(String.class)) {
          properties.setProperty(this.getPrefix() + field.getName(), field.get(this) != null ? (String) field.get(this) : "");
        } else if (field.getType().equals(String[].class)) {
          properties.setProperty(this.getPrefix() + field.getName(), field.get(this) != null ? String.join(",", (String[]) field.get(this)) : "");
        } else if (field.getType() instanceof Class && field.getType().isEnum()) {
          Object val = field.get(this);
          final String value = val == null && field.getType().getEnumConstants().length > 0 ? field.getType().getEnumConstants()[0].toString() : "";
          properties.setProperty(this.getPrefix() + field.getName(), val != null ? val.toString() : value);
        }
      }
    } catch (final IllegalArgumentException | IllegalAccessException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }
}
