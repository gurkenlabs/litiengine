package de.gurkenlabs.litiengine.util.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FileUtilities {
  private static final Logger log = Logger.getLogger(FileUtilities.class.getName());
  private static final String[] DIR_BLACKLIST = new String[] {"\\bin", "\\screenshots"};
  private static final String FILE_SEPARATOR_WIN = "\\";
  private static final String FILE_SEPARATOR = "/";

  private FileUtilities() {
    throw new UnsupportedOperationException();
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

  public static List<String> findFilesByExtension(
      final List<String> fileNames, final Path dir, final String extension) {
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

  public static List<String> findFiles(
      final List<String> fileNames, final Path dir, final String... files) {
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

  public static String getFileName(URL path) {
    return getFileName(path.getPath());
  }

  public static String getFileName(final String path) {
    return getFileName(path, false);
  }

  public static String getFileName(final String path, boolean extension) {
    if (path == null
        || path.isEmpty()
        || path.endsWith(FILE_SEPARATOR_WIN)
        || path.endsWith(FILE_SEPARATOR)) {
      return "";
    }

    String name = path;

    if (!extension) {
      final int pos = name.lastIndexOf('.');
      if (pos > 0) {
        name = name.substring(0, pos);
      }
    }

    final int lastBackslash = name.lastIndexOf(FILE_SEPARATOR);
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

  public static String getParentDirPath(final String uri) {
    if (uri == null || uri.isEmpty()) {
      return uri;
    }

    try {
      return getParentDirPath(new URI(uri));
    } catch (URISyntaxException e) {
      String parent = new File(uri).getParent();
      parent += File.separator;
      return parent;
    }
  }

  public static String getParentDirPath(final URI uri) {
    URI parent = uri.getPath().endsWith(FILE_SEPARATOR) ? uri.resolve("..") : uri.resolve(".");
    return parent.toString();
  }

  /**
   * This method combines the specified basepath with the parts provided as arguments. The output
   * will use the path separator of the current system;
   *
   * @param basePath The base path for the combined path.
   * @param paths The parts of the path to be constructed.
   * @return The combined path.
   */
  public static String combine(String basePath, final String... paths) {
    basePath = basePath.replace(FILE_SEPARATOR_WIN, FILE_SEPARATOR);
    try {

      URI uri = new URI(basePath.replace(" ", "%20"));

      for (String path : paths) {
        if (path == null) {
          continue;
        }

        path = path.replace(FILE_SEPARATOR_WIN, FILE_SEPARATOR);

        uri = uri.resolve(path.replace(" ", "%20"));
      }

      return uri.toString().replace("%20", " ");
    } catch (URISyntaxException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return basePath;
    }
  }

  private static boolean isBlackListedDirectory(Path path) {
    for (final String black : DIR_BLACKLIST) {
      if (path.toAbsolutePath().toString().contains(black)) {
        return true;
      }
    }

    return false;
  }

  public static String humanReadableByteCount(long bytes) {
    return humanReadableByteCount(bytes, false);
  }

  public static String humanReadableByteCount(long bytes, boolean decimal) {
    int unit = decimal ? 1000 : 1024;
    if (bytes < unit) return bytes + " bytes";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = new String[] {"K", "M", "G", "T", "P", "E"}[exp - 1];
    pre = decimal ? pre : pre + "i";
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }
}
