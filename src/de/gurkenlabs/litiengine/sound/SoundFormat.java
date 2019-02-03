package de.gurkenlabs.litiengine.sound;

import java.io.File;

import de.gurkenlabs.litiengine.resources.DataFormat;

public enum SoundFormat {
  UNDEFINED, OGG, MP3, WAV;
  
  public static SoundFormat get(String format) {
    return DataFormat.get(format, values(), UNDEFINED);
  }

  public static boolean isSupported(File file) {
    return isSupported(file);
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
