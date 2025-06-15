package de.gurkenlabs.litiengine.configuration;

import de.gurkenlabs.litiengine.util.ReflectionUtilities;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EventListener;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains some basic functionality for all setting groups. It gets the SettingsGroupInfo annotation and reads out the prefix that is used
 * when reading/ writing the settings into a property file.
 */
@ConfigurationGroupInfo
public abstract class ConfigurationGroup {
  private static final Logger log = Logger.getLogger(ConfigurationGroup.class.getName());

  private final Collection<ConfigurationChangedListener> listeners = ConcurrentHashMap.newKeySet();

  private final String prefix;
  private boolean debug;

  /**
   * Initializes a new instance of the {@code ConfigurationGroup} class.
   */
  protected ConfigurationGroup() {
    final ConfigurationGroupInfo info = this.getClass().getAnnotation(ConfigurationGroupInfo.class);
    this.prefix = info.prefix();
    this.debug = info.debug();
  }

  /**
   * Adds the specified configuration changed listener to receive events about any configuration property that changed.
   *
   * <p>
   * The event is supported for any property that uses the {@link #set(String, Object)} method to set the field value.
   * </p>
   *
   * <p>
   * The event will provide you with the fieldName of the called setter (e.g. "debug" for the "setDebug" call).
   * </p>
   *
   * @param listener The listener to add.
   * @see ConfigurationGroup#set(String, Object)
   */
  public void onChanged(ConfigurationChangedListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Removes the specified configuration changed listener.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(ConfigurationChangedListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Gets the prefix for the configuration group.
   *
   * @return The prefix for the configuration group, or an empty string if the prefix is null.
   */
  public String getPrefix() {
    return this.prefix != null ? this.prefix : "";
  }

  /**
   * Checks if debug mode is enabled.
   *
   * @return true if debug mode is enabled, false otherwise.
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * Sets the debug mode.
   *
   * @param debug true to enable debug mode, false to disable.
   */
  public void setDebug(boolean debug) {
    this.set("debug", debug);
  }

  /**
   * Initializes a property by its key and value.
   *
   * @param key   The key of the property.
   * @param value The value of the property.
   */
  protected void initializeByProperty(final String key, final String value) {
    final String propertyName = key.substring(this.getPrefix().length());
    ReflectionUtilities.setFieldValue(this.getClass(), this, propertyName, value);
  }

  /**
   * Store properties. By default, it is supported to store the following types: boolean, int, double, float, String and enum values. If you need to
   * store any other object, you should overwrite this method as well as the initializeProperty method and implement a custom approach.
   *
   * @param properties the properties
   */
  protected void storeProperties(final Properties properties) {
    try {
      for (Field field : this.getClass().getDeclaredFields()) {
        if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
          continue; // Skip final or static fields
        }

        field.setAccessible(true); // Ensure field is accessible
        String propertyKey = this.getPrefix() + field.getName();
        Object value = field.get(this);

        if (value == null) {
          properties.setProperty(propertyKey, "");
          continue;
        }

        if (field.getType().isPrimitive() || value instanceof String || value instanceof Path) {
          properties.setProperty(propertyKey, value.toString());
        } else if (value instanceof String[]) {
          properties.setProperty(propertyKey, String.join(",", (String[]) value).replace("null", ""));
        } else if (field.getType().isEnum()) {
          properties.setProperty(propertyKey, value.toString());
        }
      }
    } catch (IllegalAccessException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /**
   * Use this method to set configuration properties if you want to support {@code configurationChanged} for your property.
   *
   * @param <T>       The type of the value to set.
   * @param fieldName The name of the field to set.
   * @param value     The value to set.
   */
  protected <T> void set(String fieldName, T value) {
    Field field = ReflectionUtilities.getField(this.getClass(), fieldName, true);
    if (field != null) {
      try {
        if (!field.canAccess(this)) {
          field.setAccessible(true);
        }

        final Object currentValue = field.get(this);

        final PropertyChangeEvent event = new PropertyChangeEvent(this, fieldName, currentValue, value);
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
     * Invoked when a property of the configuration has been changed using the {@link ConfigurationGroup#set(String, Object)} method to support this
     * event.
     *
     * @param event The property changed event.
     */
    void configurationChanged(PropertyChangeEvent event);
  }
}
