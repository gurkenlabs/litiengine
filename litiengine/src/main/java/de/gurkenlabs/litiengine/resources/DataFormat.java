package de.gurkenlabs.litiengine.resources;

import java.util.ArrayList;

import de.gurkenlabs.litiengine.util.io.FileUtilities;

/**
 * Some common implementations that are used by different kinds of file classes (e.g. {@code SoundFormat},
 * {@code ImageFormat}.
 */
final class DataFormat {
  private DataFormat() {}

  protected static <T extends Enum<T>> T get(String format, T[] values, T defaultValue) {
    if (format == null || format.isEmpty()) {
      return defaultValue;
    }

    String stripedImageFormat = format;
    if (stripedImageFormat.startsWith(".")) {
      stripedImageFormat = format.substring(1);
    }

    for (T val : values) {
      if (stripedImageFormat.equalsIgnoreCase(val.toString())) {
        return val;
      }
    }

    return defaultValue;
  }

  protected static <T extends Enum<T>> boolean isSupported(String fileName, T[] values, T defaultValue) {
    String extension = FileUtilities.getExtension(fileName);
    if (extension == null || extension.isEmpty()) {
      return false;
    }

    for (String supported : getAllExtensions(values, defaultValue)) {
      if (extension.equalsIgnoreCase(supported)) {
        return true;
      }
    }

    return false;
  }

  protected static <T extends Enum<T>> String[] getAllExtensions(T[] values, T defaultValue) {
    ArrayList<String> arrList = new ArrayList<>();
    for (T format : values) {
      if (format != defaultValue) {
        arrList.add(format.toString());
      }
    }

    return arrList.toArray(new String[arrList.size()]);
  }
}
