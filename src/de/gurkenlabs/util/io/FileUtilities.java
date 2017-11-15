package de.gurkenlabs.util.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FileUtilities {
  private static final Logger log = Logger.getLogger(FileUtilities.class.getName());
  private static final String[] DIR_BLACKLIST = new String[] { "\\bin", "\\screenshots" };

  private FileUtilities() {
  }

  public static boolean deleteDir(final File dir) {
    if (dir.isDirectory()) {
      final String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        final boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }

    try {
      Files.delete(dir.toPath().toAbsolutePath());
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return false;
    }

    return true;
  }

  public static List<String> findFiles(final List<String> fileNames, final Path dir, final String extension) {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
      for (final Path path : stream) {
        if (path.toFile().isDirectory()) {
          if (isBlackListedDirectory(path)) {
            continue;
          }

          findFiles(fileNames, path, extension);
        } else if (path.toAbsolutePath().toString().endsWith(extension)) {
          fileNames.add(path.toAbsolutePath().toString());
        }
      }
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return fileNames;
  }

  public static List<String> findFiles(final List<String> fileNames, final Path dir, final String... files) {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
      for (final Path path : stream) {
        if (path.toFile().isDirectory()) {
          if (isBlackListedDirectory(path)) {
            continue;
          }

          findFiles(fileNames, path, files);
        } else {
          for (final String file : files) {
            if (path.toAbsolutePath().toString().endsWith(file)) {

              fileNames.add(path.toAbsolutePath().toString());
            }
          }
        }
      }
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return fileNames;
  }

  private static boolean isBlackListedDirectory(Path path) {
    for (final String black : DIR_BLACKLIST) {
      if (path.toAbsolutePath().toString().contains(black)) {
        return true;
      }
    }

    return false;
  }

  public static String getExtension(final File file) {
    return getExtension(file.getAbsolutePath());
  }

  public static String getExtension(final String fileName) {
    try {
      return fileName.substring(fileName.lastIndexOf('.') + 1);
    } catch (final Exception e) {
      return "";
    }
  }

  public static String getFileName(final String path) {
    if (path == null || path.isEmpty()) {
      return "";
    }

    String name = path;
    final int pos = name.lastIndexOf('.');
    if (pos > 0) {
      name = name.substring(0, pos);
    }

    final int lastBackslash = name.lastIndexOf('/');
    if (lastBackslash != -1) {
      name = name.substring(lastBackslash + 1, name.length());
    } else {
      final int lastForwardSlash = name.lastIndexOf('\\');
      if (lastForwardSlash != -1) {
        name = name.substring(lastForwardSlash + 1, name.length());
      }
    }

    return name;
  }

  /**
   * Gets the specified file from either a resource folder or the file system.
   * 
   * @param file
   * @return
   */
  public static InputStream getGameResource(final String file) {
    try {
      InputStream resourceStream = ClassLoader.getSystemResourceAsStream(file);
      if (resourceStream != null) {
        return new BufferedInputStream(resourceStream);
      }

      resourceStream = FileUtilities.class.getResourceAsStream(file);
      if (resourceStream != null) {
        return new BufferedInputStream(resourceStream);
      }

      File f = new File(file);
      if (f.exists()) {
        resourceStream = new FileInputStream(file);
        return new BufferedInputStream(resourceStream);
      } else {
        log.log(Level.INFO, "{0} could not be found.", file);
        return null;
      }
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }

  public static String getParentDirPath(final String fileOrDirPath) {
    if (fileOrDirPath.contains(File.separator)) {
      return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf(File.separatorChar, fileOrDirPath.length()));
    } else if (fileOrDirPath.contains("/")) {
      return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf('/') + 1);
    }

    return "";
  }
}
