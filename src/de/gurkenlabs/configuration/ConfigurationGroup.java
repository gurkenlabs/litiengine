/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import de.gurkenlabs.annotation.ConfigurationGroupInfo;

/**
 * This class contains some basic functionality for all setting groups. It gets
 * the SettingsGroupInfo annotation and reads out the prefix that is used when
 * reading/ writing the settings into a property file.
 */
@ConfigurationGroupInfo
public abstract class ConfigurationGroup {

  /** The prefix. */
  private final String prefix;

  /**
   * Instantiates a new configuration group.
   */
  public ConfigurationGroup() {
    final ConfigurationGroupInfo info = this.getClass().getAnnotation(ConfigurationGroupInfo.class);
    this.prefix = info.prefix();
  }

  /**
   * Gets the prefix.
   *
   * @return the prefix
   */
  public String getPrefix() {
    return this.prefix;
  }

  private Field getField(final String fieldName) {
    for (final Field field : this.getClass().getDeclaredFields()) {
      if (field.getName().equalsIgnoreCase(fieldName)) {
        return field;
      }
    }

    return null;
  }

  private Method getSetter(final String fieldName, final Class<?> fieldType) {
    for (final Method method : this.getClass().getMethods()) {
      // method must start with "set" and have only one parameter, mathich the
      // specified fieldType
      if (method.getName().equalsIgnoreCase("set" + fieldName) && method.getParameters().length == 1) {
        if (!method.isAccessible()) {
          method.setAccessible(true);
        }
        
        return method;
      }
    }

    return null;
  }

  private <T> void setPropertyValue(final String propertyName, final T value) {
    try {
      final Method method = this.getSetter(propertyName, value.getClass());
      if (method != null) {
        // set the new value with the setter
        method.invoke(this, value);
      } else {
        // if no setter is present, try to set the field directly
        for (final Field field : this.getClass().getDeclaredFields()) {
          if (field.getName().equals(propertyName) && field.getType().equals(value.getClass())) {
            if (!field.isAccessible()) {
              field.setAccessible(true);
            }

            field.set(this, value);
          }
        }
      }
    } catch (final SecurityException e) {
      e.printStackTrace();
    } catch (final IllegalAccessException e) {
      e.printStackTrace();
    } catch (final IllegalArgumentException e) {
      e.printStackTrace();
    } catch (final InvocationTargetException e) {
      e.printStackTrace();
    }
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
    this.initializeProperty(propertyName, value);
  }

  /**
   * Initialize property.
   *
   * @param propertyName
   *          the property name
   * @param value
   *          the value
   */
  protected void initializeProperty(final String propertyName, final String value) {
    // if a setter is present, this method will use it, otherwise it will
    // directly try to set the field.
    final Field field = this.getField(propertyName);
    if (field == null) {
      return;
    }

    try {
      if (field.getType().equals(boolean.class)) {
        this.setPropertyValue(propertyName, Boolean.parseBoolean(value));
      } else if (field.getType().equals(int.class)) {
        this.setPropertyValue(propertyName, Integer.parseInt(value));
      } else if (field.getType().equals(float.class)) {
        this.setPropertyValue(propertyName, Float.parseFloat(value));
      } else if (field.getType().equals(double.class)) {
        this.setPropertyValue(propertyName, Double.parseDouble(value));
      } else if (field.getType().equals(String.class)) {
        this.setPropertyValue(propertyName, value);
      } else if (field.getType() instanceof Class && ((Class<?>) field.getType()).isEnum()) {
        final Object[] enumArray = field.getType().getEnumConstants();

        for (final Object enumConst : enumArray) {
          if (enumConst != null && enumConst.toString().equalsIgnoreCase(value)) {
            this.setPropertyValue(propertyName, field.getType().cast(enumConst));
          }
        }
      }
    } catch (final NumberFormatException e) {
      e.printStackTrace();
    }
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
        } else if (field.getType().equals(String.class)) {
          properties.setProperty(this.getPrefix() + field.getName(), (String) field.get(this));
        } else if (field.getType() instanceof Class && ((Class<?>) field.getType()).isEnum()) {
          properties.setProperty(this.getPrefix() + field.getName(), field.get(this).toString());
        }
      }
    } catch (final IllegalArgumentException e) {
      e.printStackTrace();
    } catch (final IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
