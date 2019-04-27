package de.gurkenlabs.litiengine.graphics;

import java.io.File;

import de.gurkenlabs.litiengine.resources.DataFormat;

public enum ImageFormat {
  UNDEFINED, PNG, GIF, BMP, JPG;

  public static ImageFormat get(String imageFormat) {
    return DataFormat.get(imageFormat, values(), UNDEFINED);
  }

  public static boolean isSupported(File file) {
    return isSupported(file.toString());
  }

  public static boolean isSupported(String fileName) {
    return DataFormat.isSupported(fileName, values(), UNDEFINED);
  }

  public static String[] getAllExtensions() {
    return DataFormat.getAllExtensions(values(), UNDEFINED);
  }

  public String toExtension() {
    return "." + this.name().toLowerCase();
  }

  @Override
  public String toString() {
    return this.name().toLowerCase();
  }
}