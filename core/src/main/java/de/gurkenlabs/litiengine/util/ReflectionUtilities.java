package de.gurkenlabs.litiengine.util;

import de.gurkenlabs.litiengine.entities.Material;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ReflectionUtilities {
  private static final Logger log = Logger.getLogger(ReflectionUtilities.class.getName());

  private ReflectionUtilities() {
    throw new UnsupportedOperationException();
  }

  public static <T> Field getField(Class<T> cls, final String fieldName) {
    return getField(cls, fieldName, true);
  }

  public static <T> Field getField(Class<T> cls, final String fieldName, boolean recursive) {
    for (final Field field : cls.getDeclaredFields()) {
      if (field.getName().equalsIgnoreCase(fieldName)) {
        return field;
      }
    }

    if (recursive && cls.getSuperclass() != null && !cls.getSuperclass().equals(Object.class)) {
      Field f = getField(cls.getSuperclass(), fieldName, true);
      if (f != null) {
        return f;
      }
    }

    log.log(
        Level.WARNING,
        "Could not find field [{0}] on class [{1}] or its parents.",
        new Object[] {fieldName, cls});
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <V> V getStaticValue(Class<?> cls, String fieldName) {
    Field keyField = ReflectionUtilities.getField(cls, fieldName);
    if (keyField == null) {
      return null;
    }

    try {
      return (V) keyField.get(null);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Recursively gets all fields of the specified type, respecting parent classes.
   *
   * @param fields The list containing all fields.
   * @param type The type to retrieve the fields from.
   * @return All fields of the specified type, including the fields of the parent classes.
   */
  public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
    fields.addAll(Arrays.asList(type.getDeclaredFields()));

    if (type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)) {
      getAllFields(fields, type.getSuperclass());
    }

    return fields;
  }

  /**
   * Recursively gets a method by the the specified name respecting the parent classes and the
   * parameters of the declaration.
   *
   * @param name The name of the method.
   * @param type The type on which to search for the method.
   * @param parameterTypes The types of the parameters defined by the method declaration.
   * @return The found method or null if no such method exists.
   */
  public static Method getMethod(String name, Class<?> type, Class<?>... parameterTypes) {
    Method method = null;
    try {
      method = type.getDeclaredMethod(name, parameterTypes);
    } catch (NoSuchMethodException e) {
      if (type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)) {
        return getMethod(name, type.getSuperclass(), parameterTypes);
      }
    }

    return method;
  }

  public static <T, C> boolean setValue(
      Class<C> cls, Object instance, final String fieldName, final T value) {
    try {
      final Method method = getSetter(cls, fieldName);
      if (method != null) {
        // set the new value with the setter
        method.invoke(instance, value);
        return true;
      } else {
        // if no setter is present, try to set the field directly
        for (final Field field : cls.getDeclaredFields()) {
          if (field.getName().equals(fieldName)
              && (field.getType() == value.getClass()
                  || isWrapperType(field.getType(), value.getClass())
                  || isWrapperType(value.getClass(), field.getType()))) {
            if (!field.isAccessible()) {
              field.setAccessible(true);
            }

            field.set(instance, value);
            return true;
          }
        }
      }
    } catch (final SecurityException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException e) {
      log.log(Level.SEVERE, String.format("%s (%s-%s)", e.getMessage(), fieldName, value), e);
    }

    return false;
  }

  public static <T> boolean setEnumPropertyValue(
      Class<T> cls, Object instance, final Field field, String propertyName, String value) {
    final Object[] enumArray = field.getType().getEnumConstants();

    for (final Object enumConst : enumArray) {
      if (enumConst != null && enumConst.toString().equalsIgnoreCase(value)) {
        return ReflectionUtilities.setValue(
            cls, instance, propertyName, field.getType().cast(enumConst));
      }
    }

    return false;
  }

  public static <T> Method getSetter(Class<T> cls, final String fieldName) {
    for (final Method method : getSetters(cls)) {
      if (method.getName().equalsIgnoreCase("set" + fieldName)) {
        return method;
      }
    }

    return null;
  }

  public static <T> Collection<Method> getSetters(Class<T> cls) {
    Collection<Method> methods = new ArrayList<>();

    for (final Method method : cls.getMethods()) {
      // method must start with "set" and have only one parameter, matching the
      // specified fieldType
      if (method.getName().toLowerCase().startsWith("set") && method.getParameters().length == 1) {
        if (!method.isAccessible()) {
          try {
            method.setAccessible(true);
          } catch (SecurityException e) {
            continue;
          }
        }

        methods.add(method);
      }
    }

    return Collections.unmodifiableCollection(methods);
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

  public static <T> boolean setFieldValue(
      final Class<T> cls, final Object instance, final String fieldName, final String value) {
    // if a setter is present, instance method will use it, otherwise it will
    // directly try to set the field.
    final Field field = getField(cls, fieldName);
    if (field == null) {
      return false;
    }

    // final fields cannot be set
    if (Modifier.isFinal(field.getModifiers())) {
      return false;
    }

    try {
      if (field.getType().equals(boolean.class)) {
        return setValue(cls, instance, fieldName, Boolean.parseBoolean(value));
      } else if (field.getType().equals(int.class)) {
        return setValue(cls, instance, fieldName, Integer.parseInt(value));
      } else if (field.getType().equals(float.class)) {
        return setValue(cls, instance, fieldName, Float.parseFloat(value));
      } else if (field.getType().equals(double.class)) {
        return setValue(cls, instance, fieldName, Double.parseDouble(value));
      } else if (field.getType().equals(short.class)) {
        return setValue(cls, instance, fieldName, Short.parseShort(value));
      } else if (field.getType().equals(byte.class)) {
        return setValue(cls, instance, fieldName, Byte.parseByte(value));
      } else if (field.getType().equals(long.class)) {
        return setValue(cls, instance, fieldName, Long.parseLong(value));
      } else if (field.getType().equals(String.class)) {
        return setValue(cls, instance, fieldName, value);
      } else if (field.getType().equals(String[].class)) {
        return setValue(cls, instance, fieldName, value.split(","));
      } else if (field.getType().equals(int[].class)) {
        return setValue(cls, instance, fieldName, ArrayUtilities.splitInt(value, ","));
      } else if (field.getType().equals(double[].class)) {
        return setValue(cls, instance, fieldName, ArrayUtilities.splitDouble(value, ","));
      }else if (field.getType() instanceof Class && field.getType().isEnum()) {
        return setEnumPropertyValue(cls, instance, field, fieldName, value);
      } else if (field.getType().equals(Material.class)) {
        return setValue(cls, instance, fieldName, Material.get(value));
      }
      // TODO: implement support for Attribute and RangeAttribute fields
    } catch (final NumberFormatException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return false;
  }

  public static List<Method> getMethodsAnnotatedWith(
      final Class<?> type, final Class<? extends Annotation> annotation) {
    final List<Method> methods = new ArrayList<>();
    Class<?> clazz = type;
    while (clazz
        != Object
            .class) { // need to iterated thought hierarchy in order to retrieve methods from above
                      // the current instance
      // iterate though the list of methods declared in the class represented by class variable, and
      // add those annotated with the specified annotation
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

  /**
   * Gets the events for the specified type.
   *
   * <p>This will search for all methods that have a parameter of type {@code EventListener} and
   * match the LITIENGINE's naming conventions for event subscription (i.e. the method name starts
   * with one of the prefixes "add" or "on".
   *
   * @param type The type to inspect the events on.
   * @return All methods on the specified type that are considered to be events.
   * @see EventListener
   */
  public static Collection<Method> getEvents(final Class<?> type) {
    final String eventAddPrefix = "add";
    final String eventOnPrefix = "on";

    final List<Method> events = new ArrayList<>();
    Class<?> clazz = type;
    while (clazz
        != Object
            .class) { // need to iterated thought hierarchy in order to retrieve methods from above
                      // the current instance
      // iterate though the list of methods declared in the class represented by class variable, and
      // add those annotated with the specified annotation
      final List<Method> allMethods = new ArrayList<>(Arrays.asList(clazz.getDeclaredMethods()));
      for (final Method method : allMethods) {
        for (Class<?> paramtype : method.getParameterTypes()) {
          if (EventListener.class.isAssignableFrom(paramtype)
              && (method.getName().startsWith(eventAddPrefix)
                  || method.getName().startsWith(eventOnPrefix))) {
            events.add(method);
          }
        }
      }

      // move to the upper class in the hierarchy in search for more methods
      clazz = clazz.getSuperclass();
    }
    return events;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getDefaultValue(Class<T> clazz) {
    return (T) Array.get(Array.newInstance(clazz, 1), 0);
  }
}
