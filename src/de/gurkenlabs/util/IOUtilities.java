package de.gurkenlabs.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IOUtilities {
  public static List<String> getFileNames(List<String> fileNames, Path dir, String extension) {
    final String[] blackList = new String[] { "\\bin", "\\screenshots" };
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
      for (Path path : stream) {
        if (path.toFile().isDirectory()) {
          boolean blacklisted = false;
          for (String black : blackList) {
            if (path.toAbsolutePath().toString().contains(black)) {
              blacklisted = true;
              break;
            }
          }

          if (!blacklisted) {
            getFileNames(fileNames, path, extension);
          }

        } else if (path.toAbsolutePath().toString().endsWith(extension)) {
          fileNames.add(path.toAbsolutePath().toString());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return fileNames;
  }
}
