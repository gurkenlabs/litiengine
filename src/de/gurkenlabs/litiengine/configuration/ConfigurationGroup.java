package de.gurkenlabs.litiengine.configuration;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.gurkenlabs.litiengine.util.io.CSV;

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
    int numberOfBranches = 27;
    int branches[] = new int [numberOfBranches];
    branches[0] = 1;
    try {
      branches[1] = 1;
      for (final Field field : this.getClass().getDeclaredFields()) {
        branches[2] = 1;
        if (!field.isAccessible()) {
          branches[3] = 1;
          field.setAccessible(true);
        } else {
          branches[4] = 1;
        }
        
        final String propertyKey = this.getPrefix() + field.getName();
        if (field.getType().equals(boolean.class)) {
          branches[5] = 1;
          properties.setProperty(propertyKey, Boolean.toString(field.getBoolean(this)));
        } else if (field.getType().equals(int.class)) {
          branches[6] = 1;
          properties.setProperty(propertyKey, Integer.toString(field.getInt(this)));
        } else if (field.getType().equals(float.class)) {
          branches[7] = 1;
          properties.setProperty(propertyKey, Float.toString(field.getFloat(this)));
        } else if (field.getType().equals(double.class)) {
          branches[8] = 1;
          properties.setProperty(propertyKey, Double.toString(field.getDouble(this)));
        } else if (field.getType().equals(byte.class)) {
          branches[9] = 1;
          properties.setProperty(propertyKey, Byte.toString(field.getByte(this)));
        } else if (field.getType().equals(short.class)) {
          branches[10] = 1;
          properties.setProperty(propertyKey, Short.toString(field.getShort(this)));
        } else if (field.getType().equals(long.class)) {
          branches[11] = 1;
          properties.setProperty(propertyKey, Long.toString(field.getLong(this)));
        } else if (field.getType().equals(String.class)) {
          branches[12] = 1;
          if(field.get(this) != null) {
            branches[13] = 1;
          } else {
            branches[14] = 1;
          }
          properties.setProperty(propertyKey, field.get(this) != null ? (String) field.get(this) : "");
        } else if (field.getType().equals(String[].class)) {
          branches[15] = 1;
          if(field.get(this) != null) {
            branches[16] = 1;
          } else {
            branches[17] = 1;
          }
          properties.setProperty(propertyKey, field.get(this) != null ? String.join(",", (String[]) field.get(this)) : "");
        } else if (field.getType().isEnum()) {
          branches[18] = 1;
          Object val = field.get(this);
          if(val == null && field.getType().getEnumConstants().length > 0) {
            branches[19] = 1;
          } else {
            branches[20] = 1;
          }
          final String value = val == null && field.getType().getEnumConstants().length > 0 ? field.getType().getEnumConstants()[0].toString() : "";
          if(val != null) {
            branches[21] = 1;
          }
          else {
            branches[22] = 1;
          }
          properties.setProperty(propertyKey, val != null ? val.toString() : value);
        } else {
          branches[23] = 1;
        }
      }
      branches[24] = 1;
    } catch (final IllegalArgumentException | IllegalAccessException e) {
      branches[25] = 1;
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    branches[26] = 1;
    try {
      CSV.write(branches, 1);
    } catch (Exception e) {
      System.err.println("Error: " + e);
    }
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }
}
