package de.gurkenlabs.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileUtilities {
  public static InputStream getGameResource(String file) {
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

  public static String getFileName(String path) {
    if (path == null || path.isEmpty()) {
      return "";
    }

    String name = path;
    final int pos = name.lastIndexOf(".");
    if (pos > 0) {
      name = name.substring(0, pos);
    }

    int lastBackslash = name.lastIndexOf("/");
    if (lastBackslash != -1) {
      name = name.substring(lastBackslash + 1, name.length());
    } else {
      int lastForwardSlash = name.lastIndexOf("\\");
      if (lastForwardSlash != -1) {
        name = name.substring(lastForwardSlash + 1, name.length());
      }
    }

    return name;
  }

  public static String getParentDirPath(String fileOrDirPath) {
    if (fileOrDirPath.contains(File.separator)) {
      return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf(File.separatorChar, fileOrDirPath.length()));
    } else if (fileOrDirPath.contains("/")) {
      return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf("/") + 1);
    }

    return "";
  }

  public static String getExtension(String fileName) {
    try {
      return fileName.substring(fileName.lastIndexOf(".") + 1);
    } catch (Exception e) {
      return "";
    }
  }

  public static boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }

    return dir.delete(); // The directory is empty now and can be deleted.
  }
  
  public static List<String> findFiles(List<String> fileNames, Path dir, String extension) {
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
            findFiles(fileNames, path, extension);
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

  public static List<String> findFiles(List<String> fileNames, Path dir, String... files) {
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
            findFiles(fileNames, path, files);
          }

        } else {
          for (String file : files) {
            if (path.toAbsolutePath().toString().endsWith(file)) {

              fileNames.add(path.toAbsolutePath().toString());
            }
          }

        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return fileNames;
  }
}
