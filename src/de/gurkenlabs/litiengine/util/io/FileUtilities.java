package de.gurkenlabs.litiengine.util.io;

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
  private static final String FILE_SEPARATOR_WIN = "\\";
  private static final String FILE_SEPARATOR_LINUX = "/";

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

  public static List<String> findFilesByExtension(final List<String> fileNames, final Path dir, final String extension) {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
      for (final Path path : stream) {
        if (path.toFile().isDirectory()) {
          if (isBlackListedDirectory(path)) {
            continue;
          }

          findFilesByExtension(fileNames, path, extension);
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

  public static String getExtension(final String path) {
    final String fileName = getFileName(path, true);
    if (!fileName.contains(".")) {
      return "";
    }
    try {
      return fileName.substring(fileName.lastIndexOf('.') + 1);
    } catch (final Exception e) {
      return "";
    }
  }

  public static String getFileName(final String path) {
    return getFileName(path, false);
  }

  public static String getFileName(final String path, boolean extension) {
    if (path == null || path.isEmpty() || path.endsWith(FILE_SEPARATOR_WIN) || path.endsWith(FILE_SEPARATOR_LINUX)) {
      return "";
    }

    String name = path;

    if (!extension) {
      final int pos = name.lastIndexOf('.');
      if (pos > 0) {
        name = name.substring(0, pos);
      }
    }

    final int lastBackslash = name.lastIndexOf(FILE_SEPARATOR_LINUX);
    if (lastBackslash != -1) {
      name = name.substring(lastBackslash + 1, name.length());
    } else {
      final int lastForwardSlash = name.lastIndexOf(FILE_SEPARATOR_WIN);
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
   *          The path to the file.
   * @return The contents of the specified file as {@link InputStream}.
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
    if (fileOrDirPath.contains(FILE_SEPARATOR_WIN)) {
      return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf(FILE_SEPARATOR_WIN) + 1);
    } else if (fileOrDirPath.contains(FILE_SEPARATOR_LINUX)) {
      return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf(FILE_SEPARATOR_LINUX) + 1);
    }

    return "";
  }

  /**
   * This method combines the specified basepath with the parts provided as
   * arguments. The output will use the path separator of the current system;
   * 
   * @param basePath
   *          The base path for the combined path.
   * @param paths
   *          The parts of the path to be constructed.
   * @return The combined path.
   */
  public static String combine(final String basePath, final String... paths) {
    String combined = ensurePathSeparator(new File(basePath).toPath().normalize().toString(), Character.toString(File.separatorChar));
    for (String path : paths) {
      if (path == null) {
        continue;
      }

      combined = ensurePathSeparator(new File(combined).toPath().resolve(path).normalize().toString(), Character.toString(File.separatorChar));
    }

    return combined;
  }

  public static String ensurePathSeparator(final String path, final String separator) {
    final String separatorToReplace = separator == FILE_SEPARATOR_LINUX ? FILE_SEPARATOR_WIN : FILE_SEPARATOR_LINUX;

    return path.replace(separatorToReplace, separator);
  }

  public static String removeTrailingSeparator(final String path) {
    if (path.endsWith(FILE_SEPARATOR_WIN) || path.endsWith(FILE_SEPARATOR_LINUX)) {
      return path.substring(0, path.length() - 1);
    }

    return path;
  }

  public static String removeLeadingSeparator(final String path) {
    if (path.startsWith(FILE_SEPARATOR_WIN) || path.startsWith(FILE_SEPARATOR_LINUX)) {
      return path.substring(1);
    }

    return path;
  }
}
