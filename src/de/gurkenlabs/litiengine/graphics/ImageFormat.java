package de.gurkenlabs.litiengine.graphics;

import java.io.File;
import java.util.ArrayList;

import de.gurkenlabs.litiengine.util.io.FileUtilities;

public enum ImageFormat {
  UNDEFINED, PNG, GIF, BMP, JPG;

  public static ImageFormat get(String imageFormat) {
    if (imageFormat == null || imageFormat.isEmpty()) {
      return UNDEFINED;
    }

    String stripedImageFormat = imageFormat;
    if (stripedImageFormat.startsWith(".")) {
      stripedImageFormat = imageFormat.substring(1);
    }

    for (ImageFormat val : values()) {
      if (stripedImageFormat.equalsIgnoreCase(val.toString())) {
        return val;
      }
    }

    return UNDEFINED;
  }

  public static boolean isSupported(File file) {
    return isSupported(file.getAbsolutePath());
  }

  public static boolean isSupported(String fileName) {
    String extension = FileUtilities.getExtension(fileName);
    if (extension == null || extension.isEmpty()) {
      return false;
    }

    for (String supported : getAllExtensions()) {
      if (extension.equalsIgnoreCase(supported)) {
        return true;
      }
    }

    return false;
  }

  public static String[] getAllExtensions() {
    ArrayList<String> arrList = new ArrayList<>();
    for (ImageFormat format : values()) {
      if (format != ImageFormat.UNDEFINED) {
        arrList.add(format.toString());
      }
    }

    return arrList.toArray(new String[arrList.size()]);
  }

  public String toExtension() {
    return "." + this.name().toLowerCase();
  }

  @Override
  public String toString() {
    return this.name().toLowerCase();
  }
}
