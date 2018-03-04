package de.gurkenlabs.litiengine;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.util.ImageProcessing;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

public final class Resources {
  public static final String LOCALIZATION_RESOURCE_FOLDER = "localization/";
  public static final String DEFAULT_BUNDLE = "strings";
  public static final String ENCODING_ISO_8859_1 = "ISO-8859-1";
  public static final String ENCODING_UTF_8 = "UTF-8";

  private static String encoding = ENCODING_ISO_8859_1;
  private static final Logger log = Logger.getLogger(Resources.class.getName());
  static {
    Locale.setDefault(new Locale("en", "US"));
  }

  private Resources() {
  }

  public static void setEncoding(String newEncoding) {
    if (newEncoding == null || newEncoding.isEmpty()) {
      throw new IllegalArgumentException("The encoding must not be null or empty.");
    }

    encoding = newEncoding;
  }

  public static String get(final String key) {
    if (key == null) {
      return null;
    }

    return getFrom(DEFAULT_BUNDLE, key);
  }

  public static String get(final String key, Object... args) {
    if (key == null) {
      return null;
    }

    return getFrom(DEFAULT_BUNDLE, key, args);
  }

  public static String getFrom(final String bundleName, final String key, Object... args) {
    if (key == bundleName) {
      return null;
    }
    try {
      final ResourceBundle defaultBundle = ResourceBundle.getBundle(LOCALIZATION_RESOURCE_FOLDER + bundleName, Game.getConfiguration().client().getLocale());

      String value = defaultBundle.getString(key);

      String decodedValue = encoding.equals(ENCODING_ISO_8859_1) ? value : new String(value.getBytes(ENCODING_ISO_8859_1), encoding);
      if (args.length > 0) {
        return MessageFormat.format(decodedValue, args);
      }

      return decodedValue;
    } catch (final MissingResourceException | UnsupportedEncodingException me) {
      final StringWriter sw = new StringWriter();
      me.printStackTrace(new PrintWriter(sw));
      final String stacktrace = sw.toString();
      log.severe(stacktrace);
    }

    return key;
  }

  public static BufferedImage getImage(final String absolutPath) {
    return getImage(absolutPath, false);
  }

  /**
   * Gets the image by the specified relative path. This method supports both,
   * loading images from a folder and loading them from the resources.
   *
   * @param absolutPath
   *          The path to the image.
   * @param forceLoad
   *          Forces the image to be reloaded. Cached values will be ignored.
   * @return the image
   */
  public static BufferedImage getImage(final String absolutPath, final boolean forceLoad) {
    if (absolutPath == null || absolutPath.isEmpty()) {
      return null;
    }

    final String cacheKey = Integer.toString(absolutPath.hashCode());
    if (!forceLoad && ImageCache.IMAGES.containsKey(cacheKey)) {
      return ImageCache.IMAGES.get(cacheKey);
    }

    // try to get image from resource folder first and as a fallback get it from
    // a normal folder
    BufferedImage img = null;
    final InputStream imageFile = FileUtilities.getGameResource(absolutPath);
    if (imageFile != null) {
      try {
        img = ImageIO.read(imageFile);
      } catch (final IOException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
        return null;
      }
    }

    if (img == null) {
      return null;
    }

    final BufferedImage compatibleImg = ImageProcessing.getCompatibleImage(img.getWidth(), img.getHeight());
    compatibleImg.createGraphics().drawImage(img, 0, 0, null);

    ImageCache.IMAGES.put(cacheKey, compatibleImg);
    return compatibleImg;
  }

  /**
   * Get a list of strings from the specified raw text files. Strings are
   * separated by a new line. <br>
   * <b>This method is not cached. Ever call will open up a new
   * {@link InputStream} to read the strings from the textfile.</b>
   * 
   * @param textFile
   * @return A list with all strings that are contained by the textfile.
   */
  public static String[] getStringList(String textFile) {
    if (textFile == null || textFile.isEmpty()) {
      return new String[0];
    }

    try (InputStream is = FileUtilities.getGameResource(textFile)) {
      if (is == null) {
        return new String[0];
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      final List<String> strings = new ArrayList<>();
      String str = "";
      while ((str = reader.readLine()) != null) {
        strings.add(str);
      }

      String[] stockArr = new String[strings.size()];
      return strings.toArray(stockArr);
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return new String[0];
  }
}
