package de.gurkenlabs.litiengine.resources;

import java.io.File;

/**
 * Contains all known image file-formats supported by the engine.
 * 
 * @see SoundFormat
 */
public enum ImageFormat {
  UNSUPPORTED, PNG, GIF, BMP, JPG;

  /**
   * Gets the <code>SoundFormat</code> of the specified format string.
   * 
   * @param format
   *          The format string from which to extract the format.
   * @return The format of the specified string or <code>UNDEFINED</code> if not supported.
   */
  public static ImageFormat get(String imageFormat) {
    return DataFormat.get(imageFormat, values(), UNSUPPORTED);
  }

  /**
   * Determines whether the extension of the specified file is supported by the engine.
   * 
   * @param file
   *          The file to check for.
   * 
   * @return True if the extension is part of this enum; otherwise false.
   */
  public static boolean isSupported(File file) {
    return isSupported(file.toString());
  }

  /**
   * Determines whether the extension of the specified file is supported by the engine.
   * 
   * @param fileName
   *          The name of the file to check for.
   * 
   * @return True if the extension is part of this enum; otherwise false.
   */
  public static boolean isSupported(String fileName) {
    return DataFormat.isSupported(fileName, values(), UNSUPPORTED);
  }

  public static String[] getAllExtensions() {
    return DataFormat.getAllExtensions(values(), UNSUPPORTED);
  }

  /**
   * Converts this format instance to a file format string that can be used as an extension (e.g. .png).<br>
   * It adds a leading '.' to the lower-case string representation of this instance.
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