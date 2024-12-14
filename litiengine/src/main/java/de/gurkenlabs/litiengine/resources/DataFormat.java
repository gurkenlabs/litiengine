package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.util.io.FileUtilities;
import java.util.ArrayList;

/**
 * Some common implementations that are used by different kinds of file classes (e.g. {@code SoundFormat}, {@code ImageFormat}.
 */
final class DataFormat {
  private DataFormat() {
  }

  /**
   * Retrieves the enum value corresponding to the given format string.
   *
   * @param <T>          The type of the enum.
   * @param format       The format string to match.
   * @param values       The array of enum values to search.
   * @param defaultValue The default value to return if no match is found.
   * @return The matching enum value, or the default value if no match is found.
   */
  static <T extends Enum<T>> T get(String format, T[] values, T defaultValue) {
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

  /**
   * Checks if the given file name is supported by the specified enum values.
   *
   * @param <T>          The type of the enum.
   * @param fileName     The name of the file to check.
   * @param values       The array of enum values to search.
   * @param defaultValue The default value to use for comparison.
   * @return true if the file is supported, false otherwise.
   */
  static <T extends Enum<T>> boolean isSupported(String fileName, T[] values, T defaultValue) {
    String extension = FileUtilities.getExtension(fileName);
    if (extension.isEmpty()) {
      return false;
    }

    for (String supported : getAllExtensions(values, defaultValue)) {
      if (extension.equalsIgnoreCase(supported)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Retrieves all extensions for the specified enum values.
   *
   * @param <T>          The type of the enum.
   * @param values       The array of enum values to search.
   * @param defaultValue The default value to exclude from the result.
   * @return An array of strings representing all extensions.
   */
  static <T extends Enum<T>> String[] getAllExtensions(T[] values, T defaultValue) {
    ArrayList<String> arrList = new ArrayList<>();
    for (T format : values) {
      if (format != defaultValue) {
        arrList.add(format.toString());
      }
    }

    return arrList.toArray(new String[0]);
  }
}
