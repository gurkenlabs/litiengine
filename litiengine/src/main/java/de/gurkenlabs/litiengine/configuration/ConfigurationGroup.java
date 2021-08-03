package de.gurkenlabs.litiengine.configuration;

import de.gurkenlabs.litiengine.util.ReflectionUtilities;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EventListener;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains some basic functionality for all setting groups. It gets the
 * SettingsGroupInfo annotation and reads out the prefix that is used when reading/ writing the
 * settings into a property file.
 */
@ConfigurationGroupInfo
public abstract class ConfigurationGroup {
  private static final Logger log = Logger.getLogger(ConfigurationGroup.class.getName());

  private final Collection<ConfigurationChangedListener> listeners = ConcurrentHashMap.newKeySet();

  private final String prefix;
  private boolean debug;

  /** Initializes a new instance of the {@code ConfigurationGroup} class. */
  protected ConfigurationGroup() {
    final ConfigurationGroupInfo info = this.getClass().getAnnotation(ConfigurationGroupInfo.class);
    this.prefix = info.prefix();
    this.debug = info.debug();
  }

  /**
   * Adds the specified configuration changed listener to receive events about any configuration
   * property that changed.
   *
   * <p>The event is supported for any property that uses the {@link #set(String, Object)} method to
   * set the field value.
   *
   * <p>The event will provide you with the fieldName of the called setter (e.g. "debug" for the
   * "setDebug" call).
   *
   * @param listener The listener to add.
   * @see ConfigurationGroup#set(String, Object)
   */
  public void onChanged(ConfigurationChangedListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(ConfigurationChangedListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Gets the prefix.
   *
   * @return the prefix
   */
  public String getPrefix() {
    return this.prefix != null ? this.prefix : "";
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.set("debug", debug);
  }

  /**
   * Initialize by property.
   *
   * @param key the key
   * @param value the value
   */
  protected void initializeByProperty(final String key, final String value) {
    final String propertyName = key.substring(this.getPrefix().length());
    ReflectionUtilities.setFieldValue(this.getClass(), this, propertyName, value);
  }

  /**
   * Store properties. By default, it is supported to store the following types: boolean, int,
   * double, float, String and enum values. If you need to store any other object, you should
   * overwrite this method as well as the initializeProperty method and implement a custom approach.
   *
   * @param properties the properties
   */
  protected void storeProperties(final Properties properties) {
    try {
      for (final Field field : this.getClass().getDeclaredFields()) {
        if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
          // final or static fields are not part of the configurable properties.
          continue;
        }

        if (!field.isAccessible()) {
          field.setAccessible(true);
        }

        final String propertyKey = this.getPrefix() + field.getName();
        if (field.getType().equals(boolean.class)) {
          properties.setProperty(propertyKey, Boolean.toString(field.getBoolean(this)));
        } else if (field.getType().equals(int.class)) {
          properties.setProperty(propertyKey, Integer.toString(field.getInt(this)));
        } else if (field.getType().equals(float.class)) {
          properties.setProperty(propertyKey, Float.toString(field.getFloat(this)));
        } else if (field.getType().equals(double.class)) {
          properties.setProperty(propertyKey, Double.toString(field.getDouble(this)));
        } else if (field.getType().equals(byte.class)) {
          properties.setProperty(propertyKey, Byte.toString(field.getByte(this)));
        } else if (field.getType().equals(short.class)) {
          properties.setProperty(propertyKey, Short.toString(field.getShort(this)));
        } else if (field.getType().equals(long.class)) {
          properties.setProperty(propertyKey, Long.toString(field.getLong(this)));
        } else if (field.getType().equals(String.class)) {
          properties.setProperty(
              propertyKey, field.get(this) != null ? (String) field.get(this) : "");
        } else if (field.getType().equals(String[].class)) {
          if (field.get(this) == null) {
            properties.setProperty(propertyKey, "");
          } else {
            String[] arr = (String[]) field.get(this);
            String value = String.join(",", arr);
            value = value.replace("null", "");
            properties.setProperty(propertyKey, value);
          }
        } else if (field.getType().isEnum()) {
          Object val = field.get(this);
          final String value =
              val == null && field.getType().getEnumConstants().length > 0
                  ? field.getType().getEnumConstants()[0].toString()
                  : "";
          properties.setProperty(propertyKey, val != null ? val.toString() : value);
        }
      }
    } catch (final IllegalArgumentException | IllegalAccessException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /**
   * Use this method to set configuration properties if you want to support {@code
   * configurationChanged} for your property.
   *
   * @param <T> The type of the value to set.
   * @param fieldName The name of the field to set.
   * @param value The value to set.
   */
  protected <T> void set(String fieldName, T value) {
    Field field = ReflectionUtilities.getField(this.getClass(), fieldName, true);
    if (field != null) {
      try {
        if (!field.isAccessible()) {
          field.setAccessible(true);
        }

        final Object currentValue = field.get(this);

        final PropertyChangeEvent event =
            new PropertyChangeEvent(this, fieldName, currentValue, value);
        field.set(this, value);

        for (ConfigurationChangedListener listener : this.listeners) {
          listener.configurationChanged(event);
        }
      } catch (IllegalArgumentException | IllegalAccessException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  /**
   * This listener interface receives events when any property of the configuration changed.
   *
   * @see ConfigurationGroup#onChanged(ConfigurationChangedListener)
   */
  @FunctionalInterface
  public interface ConfigurationChangedListener extends EventListener {
    /**
     * Invoked when a a property of the configuration has been changed using the {@link
     * ConfigurationGroup#set(String, Object)} method to support this event.
     *
     * @param event The property changed event.
     */
    void configurationChanged(PropertyChangeEvent event);
  }
}
