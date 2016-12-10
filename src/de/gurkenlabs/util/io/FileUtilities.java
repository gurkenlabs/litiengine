package de.gurkenlabs.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtilities {
  public static InputStream getGameFile(String file) {
    try {
      final InputStream resourceStream = ClassLoader.getSystemResourceAsStream(file);
      if (resourceStream != null) {
        return resourceStream;
      }

      final InputStream fileStream = new FileInputStream(file);
      return fileStream;
    } catch (final IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String getParentDirPath(String fileOrDirPath) {
    if (fileOrDirPath.contains(File.separator)) {
      return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf(File.separatorChar, fileOrDirPath.length()));
    }
    else if(fileOrDirPath.contains("/")) {
      return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf("/") + 1);
    }
    
    return "";
  }
}
