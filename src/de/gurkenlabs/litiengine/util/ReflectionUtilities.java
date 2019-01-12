package de.gurkenlabs.litiengine.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ReflectionUtilities {
  private static final Logger log = Logger.getLogger(ReflectionUtilities.class.getName());

  private ReflectionUtilities() {
    throw new UnsupportedOperationException();
  }

  public static <T> Field getField(Class<T> cls, final String fieldName) {
    for (final Field field : cls.getDeclaredFields()) {
      if (field.getName().equalsIgnoreCase(fieldName)) {
        return field;
      }
    }

    return null;
  }

  public static <T, C> void setValue(Class<C> cls, Object instance, final String fieldName, final T value) {
    try {
      final Method method = getSetter(cls, fieldName);
      if (method != null) {
        // set the new value with the setter
        method.invoke(instance, value);
      } else {
        // if no setter is present, try to set the field directly
        for (final Field field : cls.getDeclaredFields()) {
          if (field.getName().equals(fieldName) && (field.getType() == value.getClass() || isWrapperType(field.getType(), value.getClass()) || isWrapperType(value.getClass(), field.getType()))) {
            if (!field.isAccessible()) {
              field.setAccessible(true);
            }

            field.set(instance, value);
          }
        }
      }
    } catch (final SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public static <T> void setEnumPropertyValue(Class<T> cls, Object instance, final Field field, String propertyName, String value) {
    final Object[] enumArray = field.getType().getEnumConstants();

    for (final Object enumConst : enumArray) {
      if (enumConst != null && enumConst.toString().equalsIgnoreCase(value)) {
        ReflectionUtilities.setValue(cls, instance, propertyName, field.getType().cast(enumConst));
      }
    }
  }

  public static <T> Method getSetter(Class<T> cls, final String fieldName) {
    for (final Method method : cls.getMethods()) {
      // method must start with "set" and have only one parameter, matching the
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

  public static <T, C> boolean isWrapperType(Class<T> primitive, Class<C> potentialWrapper) {
    if (!primitive.isPrimitive() || potentialWrapper.isPrimitive()) {
      return false;
    }

    if (primitive == boolean.class) {
      return potentialWrapper == Boolean.class;
    }

    if (primitive == char.class) {
      return potentialWrapper == Character.class;
    }

    if (primitive == byte.class) {
      return potentialWrapper == Byte.class;
    }

    if (primitive == short.class) {
      return potentialWrapper == Short.class;
    }

    if (primitive == int.class) {
      return potentialWrapper == Integer.class;
    }

    if (primitive == long.class) {
      return potentialWrapper == Long.class;
    }

    if (primitive == float.class) {
      return potentialWrapper == Float.class;
    }

    if (primitive == double.class) {
      return potentialWrapper == Double.class;
    }

    if (primitive == void.class) {
      return potentialWrapper == Void.class;
    }

    return false;
  }

  public static <T> void setFieldValue(final Class<T> cls, final Object instance, final String fieldName, final String value) {
    // if a setter is present, instance method will use it, otherwise it will
    // directly try to set the field.
    final Field field = ReflectionUtilities.getField(cls, fieldName);
    if (field == null) {
      return;
    }

    try {
      if (field.getType().equals(boolean.class)) {
        ReflectionUtilities.setValue(cls, instance, fieldName, Boolean.parseBoolean(value));
      } else if (field.getType().equals(int.class)) {
        ReflectionUtilities.setValue(cls, instance, fieldName, Integer.parseInt(value));
      } else if (field.getType().equals(float.class)) {
        ReflectionUtilities.setValue(cls, instance, fieldName, Float.parseFloat(value));
      } else if (field.getType().equals(double.class)) {
        ReflectionUtilities.setValue(cls, instance, fieldName, Double.parseDouble(value));
      } else if (field.getType().equals(short.class)) {
        ReflectionUtilities.setValue(cls, instance, fieldName, Short.parseShort(value));
      } else if (field.getType().equals(byte.class)) {
        ReflectionUtilities.setValue(cls, instance, fieldName, Byte.parseByte(value));
      } else if (field.getType().equals(long.class)) {
        ReflectionUtilities.setValue(cls, instance, fieldName, Long.parseLong(value));
      } else if (field.getType().equals(String.class)) {
        ReflectionUtilities.setValue(cls, instance, fieldName, value);
      } else if (field.getType().equals(String[].class)) {
        ReflectionUtilities.setValue(cls, instance, fieldName, value.split(","));
      } else if (field.getType() instanceof Class && field.getType().isEnum()) {
        ReflectionUtilities.setEnumPropertyValue(cls, instance, field, fieldName, value);
      }
    } catch (final NumberFormatException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
    final List<Method> methods = new ArrayList<>();
    Class<?> clazz = type;
    while (clazz != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
      // iterate though the list of methods declared in the class represented by class variable, and add those annotated with the specified annotation
      final List<Method> allMethods = new ArrayList<>(Arrays.asList(clazz.getDeclaredMethods()));
      for (final Method method : allMethods) {
        if (method.isAnnotationPresent(annotation)) {
          methods.add(method);
        }
      }
      // move to the upper class in the hierarchy in search for more methods
      clazz = clazz.getSuperclass();
    }
    return methods;
  }
}
