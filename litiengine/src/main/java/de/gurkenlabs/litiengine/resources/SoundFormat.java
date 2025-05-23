package de.gurkenlabs.litiengine.resources;

import java.nio.file.Path;

/**
 * Contains all known audio file-formats supported by the engine.
 *
 * @see ImageFormat
 */
public enum SoundFormat {
  UNSUPPORTED,
  OGG,
  MP3,
  WAV;

  /**
   * Gets the {@code SoundFormat} of the specified format string.
   *
   * @param format The format string from which to extract the format.
   * @return The format of the specified string or {@code UNDEFINED} if not supported.
   */
  public static SoundFormat get(String format) {
    return DataFormat.get(format, values(), UNSUPPORTED);
  }

  /**
   * Determines whether the extension of the specified file is supported by the engine.
   *
   * @param file The file to check for.
   * @return True if the extension is part of this enum; otherwise false.
   */
  public static boolean isSupported(Path file) {
    return isSupported(file.getFileName().toString());
  }

  /**
   * Determines whether the extension of the specified file is supported by the engine.
   *
   * @param fileName The file name to check for.
   * @return True if the extension is part of this enum; otherwise false.
   */
  public static boolean isSupported(String fileName) {
    return DataFormat.isSupported(fileName, values(), UNSUPPORTED);
  }

  /**
   * Gets all the file extensions supported by the engine.
   *
   * @return An array of strings representing all supported file extensions.
   */
  public static String[] getAllExtensions() {
    return DataFormat.getAllExtensions(values(), UNSUPPORTED);
  }

  /**
   * Converts this format instance to a file format string that can be used as an extension (e.g. .ogg).<br> It adds a leading '.' to the lower-case
   * string representation of this instance.
   *
   * @return The file extension string for this instance.
   */
  public String toFileExtension() {
    return "." + this.name().toLowerCase();
  }

  @Override
  public String toString() {
    return this.name().toLowerCase();
  }
}
